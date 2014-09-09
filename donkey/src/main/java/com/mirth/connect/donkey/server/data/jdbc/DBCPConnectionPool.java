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

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

public class DBCPConnectionPool implements ConnectionPool {
    private PoolingDataSource dataSource;
    private int maxConnections;

    public DBCPConnectionPool(String url, String username, String password, int maxConnections) {
        this.maxConnections = maxConnections;
        GenericObjectPool connectionPool = new GenericObjectPool(null);
        connectionPool.setMaxActive(maxConnections);
        connectionPool.setMaxIdle(maxConnections);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, username, password);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, false);
        dataSource = new PoolingDataSource(connectionPool);
        dataSource.setAccessToUnderlyingConnectionAllowed(true);
    }

    @Override
    public PooledConnection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        return new PooledConnection(connection, ((DelegatingConnection) connection).getInnermostDelegate());
    }

    @Override
    public Integer getMaxConnections() {
        return maxConnections;
    }
}
