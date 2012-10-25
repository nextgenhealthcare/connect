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

import com.mirth.connect.donkey.server.data.DonkeyDaoException;
import com.mirth.connect.donkey.server.data.jdbc.CachedPreparedStatementSource;
import com.mirth.connect.donkey.server.data.jdbc.DefaultPreparedStatementSource;
import com.mirth.connect.donkey.server.data.jdbc.JdbcDao;
import com.mirth.connect.donkey.server.data.jdbc.JdbcDaoFactory;
import com.mirth.connect.donkey.server.data.jdbc.PooledConnection;
import com.mirth.connect.donkey.server.data.jdbc.PreparedStatementSource;

public class TestDaoFactory extends JdbcDaoFactory {
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
        PooledConnection pooledConnection = null;

        try {
            pooledConnection = getConnectionPool().getConnection();
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }

        PreparedStatementSource statementSource = null;

        switch (getCacheMode()) {
            case DISABLED:
                statementSource = new DefaultPreparedStatementSource(pooledConnection.getConnection(), getQuerySource());
                break;

            case INSTANCE:
                statementSource = new CachedPreparedStatementSource(pooledConnection.getConnection(), getQuerySource());
                break;

            case CONNECTION:
                Connection internalConnection = pooledConnection.getInternalConnection();
                statementSource = statementSources.get(internalConnection);

                if (statementSource == null) {
                    statementSource = new CachedPreparedStatementSource(internalConnection, getQuerySource());
                    statementSources.put(internalConnection, statementSource);
                }
                break;

            default:
                throw new DonkeyDaoException("Unrecognized cache mode");
        }

        return new TestDao(pooledConnection.getConnection(), getQuerySource(), statementSource, getSerializer(), errorPct, hangPct, hangMillis);
    }
}
