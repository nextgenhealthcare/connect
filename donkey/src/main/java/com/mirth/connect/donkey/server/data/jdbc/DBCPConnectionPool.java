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

import javax.sql.DataSource;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DelegatingConnection;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class DBCPConnectionPool implements ConnectionPool {
    private DataSource dataSource;
    private int maxConnections;

    public DBCPConnectionPool(String url, String username, String password, int maxConnections, boolean readOnly) {
        this.maxConnections = maxConnections;

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, username, password);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
        poolableConnectionFactory.setDefaultAutoCommit(false);
        poolableConnectionFactory.setDefaultReadOnly(readOnly);

        GenericObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<PoolableConnection>(poolableConnectionFactory);
        connectionPool.setMaxTotal(maxConnections);
        connectionPool.setMaxIdle(maxConnections);

        poolableConnectionFactory.setPool(connectionPool);

        PoolingDataSource<PoolableConnection> dataSource = new PoolingDataSource<PoolableConnection>(connectionPool);
        dataSource.setAccessToUnderlyingConnectionAllowed(true);

        this.dataSource = dataSource;
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

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }
}
