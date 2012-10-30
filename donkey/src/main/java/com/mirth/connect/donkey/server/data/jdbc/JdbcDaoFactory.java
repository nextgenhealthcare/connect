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

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.server.Serializer;
import com.mirth.connect.donkey.server.data.DonkeyDaoException;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;

public class JdbcDaoFactory implements DonkeyDaoFactory {
    private ConnectionPool connectionPool;
    private QuerySource querySource;
    private Serializer serializer;
    private Map<Connection, PreparedStatementSource> statementSources = new ConcurrentHashMap<Connection, PreparedStatementSource>();
    private Logger logger = Logger.getLogger(getClass());
    
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

    public Map<Connection, PreparedStatementSource> getStatementSources() {
        return statementSources;
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
        Connection internalConnection = pooledConnection.getInternalConnection();
        statementSource = statementSources.get(internalConnection);

        if (statementSource == null) {
            statementSource = new CachedPreparedStatementSource(internalConnection, querySource);
            statementSources.put(internalConnection, statementSource);
            
            Integer maxConnections = connectionPool.getMaxConnections();
            
            // TODO: find a more efficient way of cleaning up old connections
            if (maxConnections == null || statementSources.size() > maxConnections) {
                logger.debug("cleaning up prepared statement cache");
                
                try {
                    for (Connection currentConnection : statementSources.keySet()) {
                        if (currentConnection.isClosed()) {
                            statementSources.remove(currentConnection);
                        }
                    }
                } catch (SQLException e) {
                    throw new DonkeyDaoException(e);
                }
            }
        }

        return new JdbcDao(connection, querySource, statementSource, serializer);
    }
}
