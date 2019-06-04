package com.cisco.dnaspaces.utils;

import com.cisco.dnaspaces.exceptions.FireHoseAPIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

public class RedshiftHelper {

    private static final Logger log = LogManager.getLogger(RedshiftHelper.class);
    private static String s3AccessKey;
    private static String s3SecretKey;

    static {
        init();
    }

    private static void init() {
        Properties props = ConfigUtil.getConfig();
        s3AccessKey = props.getProperty("s3.accesskey");
        s3SecretKey = props.getProperty("s3.secretkey");
    }

    public static String getCopyCommand(String eventType, String s3Url) throws FireHoseAPIException {
        StringBuilder queryBuilder = RedshiftHelper.getEventTableCopyCommand(eventType);
        queryBuilder
                .append(" FROM '" + s3Url + "' ")
                .append("credentials 'aws_access_key_id=")
                .append(RedshiftHelper.s3AccessKey)
                .append(";aws_secret_access_key=")
                .append(RedshiftHelper.s3SecretKey)
                .append("' ")
                .append("region 'us-east-1' ")
                .append("COMPUPDATE OFF ")
                .append("STATUPDATE OFF ")
                .append("delimiter ',' ")
                .append("maxerror 2500");
        return queryBuilder.toString();
    }

    private static StringBuilder getEventTableCopyCommand(String eventType) throws FireHoseAPIException {
        StringBuilder queryBuilder = new StringBuilder();
        switch (eventType) {
            case "DEVICE_LOCATION_UPDATE":
                queryBuilder.append("COPY dev_net.device_location_update (")
                        .append(" longitude, ")
                        .append(" device_id, ")
                        .append(" partner_tenant_id, ")
                        .append(" y_pos, ")
                        .append(" mac_address, ")
                        .append(" x_pos, ")
                        .append(" ssid, ")
                        .append(" latitude, ")
                        .append(" manufacturer, ")
                        .append(" record_uid, ")
                        .append(" location_type, ")
                        .append(" record_timestamp, ")
                        .append(" last_seen, ")
                        .append(" visit_id, ")
                        .append(" location_id, ")
                        .append(" raw_user_id, ")
                        .append(" spaces_tenant_id, ")
                        .append(" device_classification, ")
                        .append(" location_name, ")
                        .append(" spaces_tenant_name ")
                        .append(" ) ");
                break;
            case "DEVICE_ENTRY":
                queryBuilder.append("COPY dev_net.device_entry (")
                        .append(" partner_tenant_id, ")
                        .append(" entry_datetime, ")
                        .append(" record_uid, ")
                        .append(" device_id, ")
                        .append(" record_timestamp, ")
                        .append(" visit_id, ")
                        .append(" days_since_last_visit, ")
                        .append(" location_type, ")
                        .append(" spaces_tenant_id, ")
                        .append(" location_name, ")
                        .append(" entry_timezone, ")
                        .append(" mac_address, ")
                        .append(" location_id, ")
                        .append(" spaces_tenant_name, ")
                        .append(" entry_timestamp")
                        .append(" ) ");
                break;
            case "DEVICE_EXIT":
                queryBuilder.append("COPY dev_net.device_exit (")
                        .append(" partner_tenant_id, ")
                        .append(" entry_timestamp, ")
                        .append(" record_uid, ")
                        .append(" location_name, ")
                        .append(" device_id, ")
                        .append(" record_timestamp, ")
                        .append(" timezone, ")
                        .append(" visit_id, ")
                        .append(" spaces_tenant_id, ")
                        .append(" visit_duration_minutes, ")
                        .append(" user_id, ")
                        .append(" location_type, ")
                        .append(" mac_address, ")
                        .append(" exit_datetime, ")
                        .append(" exit_timestamp, ")
                        .append(" entry_datetime, ")
                        .append(" location_id, ")
                        .append(" spaces_tenant_name")
                        .append(" ) ");
                break;
            case "PROFILE_UPDATE":
                queryBuilder.append("COPY dev_net.profile_update (")
                        .append(" partner_tenant_id, ")
                        .append(" record_uid, ")
                        .append(" gender, ")
                        .append(" last_name, ")
                        .append(" email, ")
                        .append(" mac_address, ")
                        .append(" user_id, ")
                        .append(" record_timestamp, ")
                        .append(" device_id, ")
                        .append(" mobile, ")
                        .append(" spaces_tenant_id, ")
                        .append(" first_name, ")
                        .append(" spaces_tenant_name")
                        .append(" ) ");
                break;

            default:
                throw new FireHoseAPIException("Couldn't find 'COPY' command for eventtype :: " + eventType);
        }
        return queryBuilder;
    }
}
