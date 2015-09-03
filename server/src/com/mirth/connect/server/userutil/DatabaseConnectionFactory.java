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
import java.util.Properties;

import org.apache.log4j.Logger;

import com.mirth.connect.connectors.jdbc.CustomDriver;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

/**
 * Used to create database connection objects.
 */
public class DatabaseConnectionFactory {

    private MirthContextFactory contextFactory;
    private boolean customDriverAttempted;
    private CustomDriver customDriver;
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
        try {
            initializeDriver(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Properties info = new Properties();
        info.setProperty("user", username);
        info.setProperty("password", password);
        if (customDriver != null) {
            return new DatabaseConnection(customDriver, address, info);
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
        try {
            initializeDriver(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (customDriver != null) {
            return new DatabaseConnection(customDriver, address);
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
        try {
            initializeDriver(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Properties info = new Properties();
        info.setProperty("user", username);
        info.setProperty("password", password);

        if (customDriver != null) {
            return customDriver.connect(address, info);
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
        if (!customDriverAttempted) {
            try {
                ClassLoader isolatedClassLoader = contextFactory.getIsolatedClassLoader();
                if (isolatedClassLoader != null) {
                    customDriver = new CustomDriver(isolatedClassLoader, driver);
                    logger.debug("Custom driver created: " + customDriver.toString() + ", Version " + customDriver.getMajorVersion() + "." + customDriver.getMinorVersion());
                } else {
                    logger.debug("Custom classloader is not being used, defaulting to DriverManager.");
                }
            } catch (Exception e) {
                logger.debug("Error creating custom driver, defaulting to DriverManager.", e);
            }

            customDriverAttempted = true;
        }

        // If a custom driver was not created, use the default one
        if (customDriver == null) {
            Class.forName(driver, true, Thread.currentThread().getContextClassLoader());
        }
    }
}
