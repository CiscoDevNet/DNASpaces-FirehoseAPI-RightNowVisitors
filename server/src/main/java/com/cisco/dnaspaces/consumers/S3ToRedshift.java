package com.cisco.dnaspaces.consumers;

import com.cisco.dnaspaces.db.Redshift;
import com.cisco.dnaspaces.exceptions.FireHoseAPIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class S3ToRedshift {
    public static final Logger log = LogManager.getLogger(S3ToRedshift.class);

    public Boolean copyDataToRedshift(String s3Url,String eventType){
        try {
            return Redshift.load(s3Url, eventType);
        }catch (FireHoseAPIException e){
            log.error(e.getMessage(), e);
        }
        return false;
    }
}
