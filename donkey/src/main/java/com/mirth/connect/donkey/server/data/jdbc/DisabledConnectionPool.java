/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

public class DisabledConnectionPool implements ConnectionPool {
    private String url;
    private String username;
    private String password;
    private boolean readOnly;

    public DisabledConnectionPool(String url, String username, String password, boolean readOnly) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.readOnly = readOnly;
    }

    @Override
    public PooledConnection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url, username, password);
        connection.setAutoCommit(false);
        if (readOnly) {
            connection.setReadOnly(true);
        }
        return new PooledConnection(connection, connection);
    }

    @Override
    public Integer getMaxConnections() {
        return null;
    }

    @Override
    public DataSource getDataSource() {
        return null;
    }
}
