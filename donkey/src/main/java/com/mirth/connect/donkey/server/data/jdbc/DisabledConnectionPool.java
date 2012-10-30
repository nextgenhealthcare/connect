/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.mirth.connect.donkey.server.Donkey;

public class DisabledConnectionPool implements ConnectionPool {
    private String url;
    private String username;
    private String password;

    public DisabledConnectionPool() throws ClassNotFoundException {
        Properties configuration = Donkey.getInstance().getConfiguration().getDatabaseProperties();
        String driver = configuration.getProperty("database.driver");

        if (driver != null) {
            Class.forName(driver);
        }

        url = configuration.getProperty("database.url");
        username = configuration.getProperty("database.username");
        password = configuration.getProperty("database.password");
    }

    @Override
    public PooledConnection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url, username, password);
        connection.setAutoCommit(false);
        return new PooledConnection(connection, connection);
    }

    @Override
    public Integer getMaxConnections() {
        return null;
    }
}
