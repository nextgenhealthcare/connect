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
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mirth.connect.donkey.server.Serializer;
import com.mirth.connect.donkey.server.data.DonkeyDaoException;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;

public class JdbcDaoFactory implements DonkeyDaoFactory {
    public enum CacheMode {
        DISABLED, INSTANCE, CONNECTION
    };

    private ConnectionPool connectionPool;
    private QuerySource querySource;
    private Serializer serializer;
    private CacheMode cacheMode = CacheMode.DISABLED;
    private Map<Connection, PreparedStatementSource> statementSources = new ConcurrentHashMap<Connection, PreparedStatementSource>();

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    public void setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public QuerySource getQuerySource() {
        return querySource;
    }

    public void setQuerySource(QuerySource querySource) {
        this.querySource = querySource;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public CacheMode getCacheMode() {
        return cacheMode;
    }

    public void setCacheMode(CacheMode cacheMode) {
        this.cacheMode = cacheMode;
    }

    @Override
    public JdbcDao getDao() {
        PooledConnection pooledConnection;

        try {
            pooledConnection = connectionPool.getConnection();
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }

        PreparedStatementSource statementSource = null;
        Connection connection = pooledConnection.getConnection();

        switch (cacheMode) {
            case DISABLED:
                statementSource = new DefaultPreparedStatementSource(connection, querySource);
                break;

            case INSTANCE:
                statementSource = new CachedPreparedStatementSource(connection, querySource);
                break;

            case CONNECTION:
                Connection internalConnection = pooledConnection.getInternalConnection();
                statementSource = statementSources.get(internalConnection);

                if (statementSource == null) {
                    statementSource = new CachedPreparedStatementSource(internalConnection, querySource);
                    statementSources.put(internalConnection, statementSource);
                }
                break;

            default:
                throw new DonkeyDaoException("Unrecognized cache mode");
        }

        return new JdbcDao(connection, querySource, statementSource, serializer);
    }
}
