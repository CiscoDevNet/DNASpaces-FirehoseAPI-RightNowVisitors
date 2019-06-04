package com.cisco.dnaspaces.db;

import com.cisco.dnaspaces.utils.ConfigUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class RedshiftConnectionPool {

    private static Properties config = ConfigUtil.getConfig();
    private static final String SET_PATH_FORMAT = "BEGIN TRANSACTION READ WRITE;SET search_path TO %s;COMMIT TRANSACTION;";
    private static final String LOOKUP_SCHEMA = config.getProperty("redshift.db.lookupSchemas");
    private static final String SET_PATH = String.format(SET_PATH_FORMAT, LOOKUP_SCHEMA);

    private static final String jdbcURL = config.getProperty("redshift.db.jdbcURL");
    private static final String user = config.getProperty("redshift.db.user");
    private static final String password = config.getProperty("redshift.db.password");

    private static final int redshiftConnectionPoolSize = Integer.parseInt(config.getProperty("redshift.db.connection.poolSize"));

    private static final Logger log = LogManager.getLogger(RedshiftConnectionPool.class);
    private RedshiftConnection[] initialConnections = new RedshiftConnection[redshiftConnectionPoolSize];

    public class RedshiftConnection {
        private AtomicBoolean inUse = new AtomicBoolean();
        private Connection conn;

        public Connection getConnection() {
            try {
                if (conn != null && !conn.isClosed() && conn.isValid(5))
                    return conn;
            } catch (SQLException e1) {

            }
            try {
                Class.forName("org.postgresql.Driver");
                conn = DriverManager.getConnection(jdbcURL, user, password);
                log.info("Postgresql initialized with connectionUrl : " + jdbcURL);
                Statement setPathStatment = conn.createStatement();
                boolean result = setPathStatment.execute(SET_PATH);
                log.info("Postgresql connection initiated and the set path result : " + result);
            } catch (Exception e) {
                log.error("Postgresql connection error", e);
            }
            return conn;
        }

        public void reconnect() {
            try {
                Class.forName("org.postgresql.Driver");
                conn = DriverManager.getConnection(jdbcURL, user, password);
                Statement setPathStatment = conn.createStatement();
                boolean result = setPathStatment.execute(SET_PATH);
                log.info("Postgresql connection initiated and the set path result : " + result);
                log.info("Postgresql initialized with connectionUrl : " + jdbcURL);
            } catch (Exception e) {
                log.error("Postgresql connection error", e);
            }
        }

        public void release() {
            inUse.set(false);
        }

    }

    public RedshiftConnectionPool() {
        for (int i = 0; i < initialConnections.length; i++)
            initialConnections[i] = new RedshiftConnection();
    }

    public RedshiftConnection get() {
        for (RedshiftConnection redshiftConnection : initialConnections) {
            if (!redshiftConnection.inUse.get())
                if (redshiftConnection.inUse.compareAndSet(false, true))
                    return redshiftConnection;
        }
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        } catch (InterruptedException e) {

        }
        return get();
    }
}
