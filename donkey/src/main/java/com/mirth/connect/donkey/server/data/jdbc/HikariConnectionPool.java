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
import java.sql.SQLException;

import com.zaxxer.hikari.HikariDataSource;

public class HikariConnectionPool implements ConnectionPool {
    private HikariDataSource dataSource;
    private int maxConnections;

    public HikariConnectionPool(String driver, String url, String username, String password, int maxConnections, boolean jdbc4, String testQuery) {
        this.maxConnections = maxConnections;

        dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setConnectionTimeout(0);
        dataSource.setAutoCommit(false);
        dataSource.setMaximumPoolSize(maxConnections);
        dataSource.setMinimumIdle(0);

        if (!jdbc4) {
            dataSource.setConnectionTestQuery(testQuery);
        }
    }

    @Override
    public PooledConnection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        return new PooledConnection(connection, connection.unwrap(Connection.class));
    }

    @Override
    public Integer getMaxConnections() {
        return maxConnections;
    }
}
