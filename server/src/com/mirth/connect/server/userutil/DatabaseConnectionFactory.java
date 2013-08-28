/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnectionFactory {

    private DatabaseConnectionFactory() {}

    /**
     * Instantiates and returns a new DatabaseConnection object with the given
     * connection parameters.
     * 
     * @param driver
     *            - The JDBC driver class (as a string) to use to create the
     *            connection with.
     * @param address
     *            - The server address to connect to.
     * @param username
     *            - The username to connect with.
     * @param password
     *            - The password to connect with.
     * @return The created DatabaseConnection object.
     * @throws SQLException
     */
    public static DatabaseConnection createDatabaseConnection(String driver, String address, String username, String password) throws SQLException {
        try {
            Class.forName(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Properties info = new Properties();
        info.setProperty("user", username);
        info.setProperty("password", password);
        return new DatabaseConnection(address, info);
    }

    /**
     * Instantiates and returns a new DatabaseConnection object with the given
     * connection parameters.
     * 
     * @param driver
     *            - The JDBC driver class (as a string) to use to create the
     *            connection with.
     * @param address
     *            - The server address to connect to.
     * @return The created DatabaseConnection object.
     * @throws SQLException
     */
    public static DatabaseConnection createDatabaseConnection(String driver, String address) throws SQLException {
        try {
            Class.forName(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new DatabaseConnection(address);
    }

    /**
     * Instantiates and returns a new java.sql.Connection object with the given
     * connection parameters.
     * 
     * @param driver
     *            - The JDBC driver class (as a string) to use to create the
     *            connection with.
     * @param address
     *            - The server address to connect to.
     * @param username
     *            - The username to connect with.
     * @param password
     *            - The password to connect with.
     * @return The created DatabaseConnection object.
     * @throws SQLException
     */
    public static Connection createConnection(String driver, String address, String username, String password) throws SQLException {
        try {
            Class.forName(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Properties info = new Properties();
        info.setProperty("user", username);
        info.setProperty("password", password);

        return DriverManager.getConnection(address, info);
    }

    /**
     * Initializes the specified JDBC driver. This can be used in JavaScript
     * contexts where "Class.forName" can't be called directly.
     * 
     * @param driver
     *            - The JDBC driver class (as a string) to initialize.
     * @throws Exception
     */
    public static void initializeDriver(String driver) throws Exception {
        Class.forName(driver);
    }
}
