package com.cisco.dnaspaces.db;

import com.cisco.dnaspaces.exceptions.FireHoseAPIException;
import com.cisco.dnaspaces.utils.RedshiftHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class Redshift {
    private static final Logger log = LogManager.getLogger(Redshift.class);
    private static final int RETRY_CNT_FOR_CONNECTION_FAILURE = 2;
    private static final String ABORT_TRANSACTION = "ABORT TRANSACTION;";
    private static final String COMMIT_TRANSACTION = "COMMIT TRANSACTION;";
    private static RedshiftConnectionPool connectionPool = new RedshiftConnectionPool();

    public static void init() {
        if (connectionPool == null)
            connectionPool = new RedshiftConnectionPool();
    }

    public static void query(String query, Map<String,Object> params, int tryCount) throws FireHoseAPIException {
        init();
        Statement stmt = null;
        long start = System.currentTimeMillis();
        RedshiftConnectionPool.RedshiftConnection redshiftConnection = connectionPool.get();
        try {
            stmt = redshiftConnection.getConnection().createStatement();
            ResultSet resultSet = stmt.executeQuery(query);
            long queryTime = System.currentTimeMillis() - start;

        }catch (SQLException e){
            log.error(e.getMessage());
            throw new FireHoseAPIException(e);
        }finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (redshiftConnection != null)
                    redshiftConnection.release();
            } catch (Exception ex) {
            }
        }
    }


    public static Boolean load(String s3Url, String eventType) throws FireHoseAPIException{
        init();
        Statement stmt = null;
        int result = -1;
        long start = System.currentTimeMillis();
        RedshiftConnectionPool.RedshiftConnection redshiftConnection = connectionPool.get();
        try {
            String query = RedshiftHelper.getCopyCommand(eventType, s3Url);
            stmt = redshiftConnection.getConnection().createStatement();
//            stmt.executeUpdate(ABORT_TRANSACTION);
            result = stmt.executeUpdate(query);
//            stmt.executeUpdate(COMMIT_TRANSACTION);
            long queryTime = System.currentTimeMillis() - start;
            return true;
        }catch (SQLException e){
            log.error(e.getMessage());
            throw new FireHoseAPIException(e);
        }finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (redshiftConnection != null)
                    redshiftConnection.release();
            } catch (Exception ex) {
            }
        }
    }


    public static void main(String[] args){
        try {
            Redshift.query("SELECT * FROM dev_net.device_entry", new HashMap<>(), 3);
            Redshift.load("s3://app-dev-for-cl//projects/temp/csvfiles/DEVICE_LOCATION_UPDATE/1559533254013.csv","DEVICE_LOCATION_UPDATE");
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

}
