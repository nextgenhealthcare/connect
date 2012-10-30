/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.server.data.DonkeyDaoException;
import com.mirth.connect.donkey.server.data.jdbc.CachedPreparedStatementSource;
import com.mirth.connect.donkey.server.data.jdbc.ConnectionPool;
import com.mirth.connect.donkey.server.data.jdbc.JdbcDao;
import com.mirth.connect.donkey.server.data.jdbc.JdbcDaoFactory;
import com.mirth.connect.donkey.server.data.jdbc.PooledConnection;
import com.mirth.connect.donkey.server.data.jdbc.PreparedStatementSource;

public class TestDaoFactory extends JdbcDaoFactory {
    private Logger logger = Logger.getLogger(getClass());
    private Map<Connection, PreparedStatementSource> statementSources = new ConcurrentHashMap<Connection, PreparedStatementSource>();
    private int errorPct = 0;
    private int hangPct = 0;
    private int hangMillis = 0;

    public int getErrorPct() {
        return errorPct;
    }

    public void setErrorPct(int errorPct) {
        this.errorPct = errorPct;
    }

    public int getHangPct() {
        return hangPct;
    }

    public void setHangPct(int hangPct) {
        this.hangPct = hangPct;
    }

    public int getHangMillis() {
        return hangMillis;
    }

    public void setHangMillis(int hangMillis) {
        this.hangMillis = hangMillis;
    }
    
    @Override
    public JdbcDao getDao() {
        ConnectionPool connectionPool = getConnectionPool();
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
            statementSource = new CachedPreparedStatementSource(internalConnection, getQuerySource());
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

        return new TestDao(connection, getQuerySource(), statementSource, getSerializer(), errorPct, hangPct, hangMillis);
    }
}
