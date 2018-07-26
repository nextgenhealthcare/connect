/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.mirth.connect.donkey.model.DatabaseConstants;
import com.mirth.connect.donkey.server.data.jdbc.ConnectionPool;
import com.mirth.connect.donkey.server.data.jdbc.DBCPConnectionPool;
import com.mirth.connect.donkey.server.data.jdbc.HikariConnectionPool;

public class DonkeyConnectionPools {

    @Inject
    private static volatile DonkeyConnectionPools instance = null;

    private ConnectionPool connectionPool;
    private ConnectionPool readOnlyConnectionPool;

    public static DonkeyConnectionPools getInstance() {
        DonkeyConnectionPools connectionPools = instance;

        if (connectionPools == null) {
            synchronized (DonkeyConnectionPools.class) {
                connectionPools = instance;
                if (connectionPools == null) {
                    instance = connectionPools = new DonkeyConnectionPools();
                }
            }
        }

        return connectionPools;
    }

    DonkeyConnectionPools() {}

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    public ConnectionPool getReadOnlyConnectionPool() {
        return readOnlyConnectionPool;
    }

    public void init(Properties dbProperties) {
        try {
            String database = dbProperties.getProperty(DatabaseConstants.DATABASE);
            String driver = dbProperties.getProperty(DatabaseConstants.DATABASE_DRIVER);
            String url = dbProperties.getProperty(DatabaseConstants.DATABASE_URL);
            String username = dbProperties.getProperty(DatabaseConstants.DATABASE_USERNAME);
            String password = dbProperties.getProperty(DatabaseConstants.DATABASE_PASSWORD);
            String pool = dbProperties.getProperty(DatabaseConstants.DATABASE_POOL);
            boolean jdbc4 = Boolean.parseBoolean(dbProperties.getProperty(DatabaseConstants.DATABASE_JDBC4));
            String testQuery = dbProperties.getProperty(DatabaseConstants.DATABASE_TEST_QUERY);
            int maxConnections;

            try {
                maxConnections = Integer.parseInt(dbProperties.getProperty(DatabaseConstants.DATABASE_MAX_CONNECTIONS));
            } catch (NumberFormatException e) {
                throw new Exception("Failed to read the " + DatabaseConstants.DATABASE_MAX_CONNECTIONS + " configuration property");
            }

            connectionPool = createConnectionPool(database, driver, url, username, password, pool, jdbc4, testQuery, maxConnections, false);

            boolean splitReadWrite = Boolean.parseBoolean(dbProperties.getProperty(DatabaseConstants.DATABASE_ENABLE_READ_WRITE_SPLIT));

            if (splitReadWrite) {
                String readOnlyDatabase = dbProperties.getProperty(DatabaseConstants.DATABASE_READONLY, database);
                String readOnlyDriver = dbProperties.getProperty(DatabaseConstants.DATABASE_READONLY_DRIVER, driver);
                String readOnlyUrl = dbProperties.getProperty(DatabaseConstants.DATABASE_READONLY_URL, url);
                String readOnlyUsername = dbProperties.getProperty(DatabaseConstants.DATABASE_READONLY_USERNAME, username);
                String readOnlyPassword = dbProperties.getProperty(DatabaseConstants.DATABASE_READONLY_PASSWORD, password);
                String readOnlyPool = dbProperties.getProperty(DatabaseConstants.DATABASE_READONLY_POOL, pool);
                boolean readOnlyJdbc4 = Boolean.parseBoolean(dbProperties.getProperty(DatabaseConstants.DATABASE_READONLY_JDBC4, dbProperties.getProperty(DatabaseConstants.DATABASE_JDBC4)));
                String readOnlyTestQuery = dbProperties.getProperty(DatabaseConstants.DATABASE_READONLY_TEST_QUERY, testQuery);
                int readOnlyMaxConnections;

                try {
                    readOnlyMaxConnections = Integer.parseInt(dbProperties.getProperty(DatabaseConstants.DATABASE_READONLY_MAX_CONNECTIONS, dbProperties.getProperty(DatabaseConstants.DATABASE_MAX_CONNECTIONS)));
                } catch (NumberFormatException e) {
                    throw new Exception("Failed to read the " + DatabaseConstants.DATABASE_READONLY_MAX_CONNECTIONS + " configuration property");
                }

                readOnlyConnectionPool = createConnectionPool(readOnlyDatabase, readOnlyDriver, readOnlyUrl, readOnlyUsername, readOnlyPassword, readOnlyPool, readOnlyJdbc4, readOnlyTestQuery, readOnlyMaxConnections, true);
            } else {
                readOnlyConnectionPool = connectionPool;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ConnectionPool createConnectionPool(String database, String driver, String url, String username, String password, String pool, boolean jdbc4, String testQuery, int maxConnections, boolean readOnly) throws Exception {
        if (driver != null) {
            Class.forName(driver);
        }

        if (StringUtils.equalsIgnoreCase(pool, "DBCP")) {
            return new DBCPConnectionPool(url, username, password, maxConnections, readOnly);
        } else {
            return new HikariConnectionPool(driver, url, username, password, maxConnections, jdbc4, testQuery, readOnly);
        }
    }
}
