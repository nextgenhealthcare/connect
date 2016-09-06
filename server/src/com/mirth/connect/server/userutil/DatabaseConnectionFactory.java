/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.mirth.connect.connectors.jdbc.CustomDriver;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

/**
 * Used to create database connection objects.
 */
public class DatabaseConnectionFactory {

    private MirthContextFactory contextFactory;
    private Map<String, CustomDriverInfo> customDriverInfoMap;
    private Logger logger = Logger.getLogger(getClass());

    public DatabaseConnectionFactory(MirthContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    /**
     * Instantiates and returns a new DatabaseConnection object with the given connection
     * parameters.
     * 
     * @param driver
     *            The JDBC driver class (as a string) to use to create the connection with.
     * @param address
     *            The server address to connect to.
     * @param username
     *            The username to connect with.
     * @param password
     *            The password to connect with.
     * @return The created DatabaseConnection object.
     * @throws SQLException
     */
    public DatabaseConnection createDatabaseConnection(String driver, String address, String username, String password) throws SQLException {
        CustomDriverInfo customDriverInfo = getCustomDriverInfo(driver);

        Properties info = new Properties();
        info.setProperty("user", username);
        info.setProperty("password", password);
        if (customDriverInfo != null && customDriverInfo.customDriver != null) {
            return new DatabaseConnection(customDriverInfo.customDriver, address, info);
        } else {
            return new DatabaseConnection(address, info);
        }
    }

    /**
     * Instantiates and returns a new DatabaseConnection object with the given connection
     * parameters.
     * 
     * @param driver
     *            The JDBC driver class (as a string) to use to create the connection with.
     * @param address
     *            The server address to connect to.
     * @return The created DatabaseConnection object.
     * @throws SQLException
     */
    public DatabaseConnection createDatabaseConnection(String driver, String address) throws SQLException {
        CustomDriverInfo customDriverInfo = getCustomDriverInfo(driver);

        if (customDriverInfo != null && customDriverInfo.customDriver != null) {
            return new DatabaseConnection(customDriverInfo.customDriver, address);
        } else {
            return new DatabaseConnection(address);
        }
    }

    /**
     * Instantiates and returns a new java.sql.Connection object with the given connection
     * parameters.
     * 
     * @param driver
     *            The JDBC driver class (as a string) to use to create the connection with.
     * @param address
     *            The server address to connect to.
     * @param username
     *            The username to connect with.
     * @param password
     *            The password to connect with.
     * @return The created DatabaseConnection object.
     * @throws SQLException
     */
    public Connection createConnection(String driver, String address, String username, String password) throws SQLException {
        CustomDriverInfo customDriverInfo = getCustomDriverInfo(driver);

        Properties info = new Properties();
        info.setProperty("user", username);
        info.setProperty("password", password);

        if (customDriverInfo != null && customDriverInfo.customDriver != null) {
            return customDriverInfo.customDriver.connect(address, info);
        } else {
            return DriverManager.getConnection(address, info);
        }
    }

    /**
     * Initializes the specified JDBC driver. This can be used in JavaScript contexts where
     * "Class.forName" can't be called directly.
     * 
     * @param driver
     *            The JDBC driver class (as a string) to initialize.
     * @throws Exception
     */
    public void initializeDriver(String driver) throws Exception {
        initializeDriverAndGetInfo(driver);
    }

    private CustomDriverInfo initializeDriverAndGetInfo(String driver) throws Exception {
        if (customDriverInfoMap == null) {
            customDriverInfoMap = new HashMap<String, CustomDriverInfo>();
        }
        CustomDriverInfo customDriverInfo = customDriverInfoMap.get(driver);
        if (customDriverInfo == null) {
            customDriverInfo = new CustomDriverInfo();
            customDriverInfoMap.put(driver, customDriverInfo);
        }

        if (!customDriverInfo.customDriverAttempted) {
            try {
                ClassLoader isolatedClassLoader = contextFactory.getIsolatedClassLoader();
                if (isolatedClassLoader != null) {
                    customDriverInfo.customDriver = new CustomDriver(isolatedClassLoader, driver);
                    logger.debug("Custom driver created: " + customDriverInfo.customDriver.toString() + ", Version " + customDriverInfo.customDriver.getMajorVersion() + "." + customDriverInfo.customDriver.getMinorVersion());
                } else {
                    logger.debug("Custom classloader is not being used, defaulting to DriverManager.");
                }
            } catch (Exception e) {
                logger.debug("Error creating custom driver, defaulting to DriverManager.", e);
            }

            customDriverInfo.customDriverAttempted = true;
        }

        // If a custom driver was not created, use the default one
        if (customDriverInfo.customDriver == null) {
            Class.forName(driver, true, Thread.currentThread().getContextClassLoader());
        }

        return customDriverInfo;
    }

    private CustomDriverInfo getCustomDriverInfo(String driver) {
        try {
            return initializeDriverAndGetInfo(driver);
        } catch (Exception e) {
            logger.error("Error initializing DatabaseConnectionFactory driver: " + driver, e);
        }
        return null;
    }

    private class CustomDriverInfo {
        public boolean customDriverAttempted;
        public CustomDriver customDriver;
    }
}
