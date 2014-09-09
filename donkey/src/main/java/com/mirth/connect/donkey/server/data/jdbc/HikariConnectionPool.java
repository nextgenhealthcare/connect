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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class HikariConnectionPool implements ConnectionPool {
    private HikariDataSource dataSource;
    private int maxConnections;

    public HikariConnectionPool(String driver, String url, String username, String password, int maxConnections, String testQuery) {
        this.maxConnections = maxConnections;

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setConnectionTimeout(0);
        config.setAutoCommit(false);
        config.setMaximumPoolSize(maxConnections);
        config.setMinimumIdle(0);

        if (testQuery != null) {
            config.setJdbc4ConnectionTest(false);
            config.setConnectionTestQuery(testQuery);
        }

        dataSource = new HikariDataSource(config);
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
