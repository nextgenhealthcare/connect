/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnectionFactory {
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

    public static DatabaseConnection createDatabaseConnection(String driver, String address) throws SQLException {
        try {
            Class.forName(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new DatabaseConnection(address);
    }

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

    // Initializes the specified Driver. This is used for JS function that can't
    // call
    // "Class.forName"
    public static void initializeDriver(String driver) throws Exception {
        Class.forName(driver);
    }
}
