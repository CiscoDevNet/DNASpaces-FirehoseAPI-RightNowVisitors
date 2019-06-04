package com.cisco.dnaspaces.client;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.cisco.dnaspaces.consumers.CSVWriter;
import com.cisco.dnaspaces.consumers.S3ToRedshift;
import com.cisco.dnaspaces.utils.ConfigUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Map;
import java.util.Properties;

public class S3UploadClient implements Runnable {

    private static final Logger log = LogManager.getLogger(S3UploadClient.class);
    private CSVWriter csvWriter;
    private String bucketName;
    private S3ToRedshift dataMoveHelper;
    private AmazonS3 s3;
    private Boolean removeFilesFromS3 = false;

    public void setCsvWriter(CSVWriter csvWriter) {
        this.csvWriter = csvWriter;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public S3ToRedshift getDataMoveHelper() {
        return dataMoveHelper;
    }

    public void setDataMoveHelper(S3ToRedshift dataMoveHelper) {
        this.dataMoveHelper = dataMoveHelper;
    }

    public void setRemoveFilesFromS3(Boolean removeFilesFromS3) {
        this.removeFilesFromS3 = removeFilesFromS3;
    }

    private void init() {
        if (s3 != null)
            return;
        Properties config = ConfigUtil.getConfig();
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(config.getProperty("s3.accesskey"), config.getProperty("s3.secretkey"));
        s3 = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
        removeFilesFromS3 = Boolean.valueOf(config.getProperty("s3.accesskey"));
    }

    @Override
    public void run() {
        init();
        Map<String, String> rolledOutFiles = this.csvWriter.rollOutFiles();
        log.info("rolledOutFiles :: " + rolledOutFiles);
        for (String eventType : rolledOutFiles.keySet()) {
            try {
                s3.putObject(this.bucketName, rolledOutFiles.get(eventType), new File(rolledOutFiles.get(eventType)));
                String s3Url = "s3://" + this.bucketName + "/" + rolledOutFiles.get(eventType);
                log.info("S3Url is  " + s3Url);
                deleteFileFromDirecotory(rolledOutFiles.get(eventType));
                if (dataMoveHelper.copyDataToRedshift(s3Url, eventType)) {
                    // on success of data movement from s3 to redshift
                    deleteFileFromS3(rolledOutFiles.get(eventType));
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    private Boolean deleteFileFromDirecotory(String filePath) {
        try {
            File f = new File(filePath);
            return f.delete();
        } catch (Exception e) {
            log.error("Couldn't delete file :: " + filePath);
            return false;
        }
    }

    private void deleteFileFromS3(String filePath) {
        if (!removeFilesFromS3)
            return;
        try {
            s3.deleteObject(new DeleteObjectRequest(this.bucketName, filePath));
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            log.error(e.getMessage(), e);
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            log.error(e.getMessage(), e);
        }
    }
}
