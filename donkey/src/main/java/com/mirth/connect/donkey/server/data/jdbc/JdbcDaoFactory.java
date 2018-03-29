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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.data.DonkeyDaoException;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.data.StatisticsUpdater;
import com.mirth.connect.donkey.util.SerializerProvider;

public class JdbcDaoFactory implements DonkeyDaoFactory {
    public static JdbcDaoFactory getInstance() {
        return getInstance(null);
    }

    public static JdbcDaoFactory getInstance(String database) {
        if (database == null) {
            return new JdbcDaoFactory();
        } else if (database.equals("postgres")) {
            return new PostgresqlDaoFactory();
        } else if (database.equals("oracle")) {
            return new OracleDaoFactory();
        } else if (database.equals("mysql")) {
            return new MysqlDaoFactory();
        } else if (database.equals("sqlserver")) {
            return new SqlServerDaoFactory();
        }

        return new JdbcDaoFactory();
    }

    private Donkey donkey;
    private ChannelController channelController;
    private String statsServerId;
    private ConnectionPool connectionPool;
    private QuerySource querySource;
    private SerializerProvider serializerProvider;
    private StatisticsUpdater statisticsUpdater;
    private boolean encryptData = false;
    private boolean decryptData = true;
    private Map<Connection, PreparedStatementSource> statementSources = new ConcurrentHashMap<Connection, PreparedStatementSource>();
    private Logger logger = Logger.getLogger(getClass());

    protected JdbcDaoFactory() {
        donkey = Donkey.getInstance();
        channelController = ChannelController.getInstance();
    }

    public String getStatsServerId() {
        return statsServerId;
    }

    public void setStatsServerId(String serverId) {
        statsServerId = serverId;
    }

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

    public void setSerializerProvider(SerializerProvider serializerProvider) {
        this.serializerProvider = serializerProvider;
    }

    public Map<Connection, PreparedStatementSource> getStatementSources() {
        return statementSources;
    }

    @Override
    public void setEncryptData(boolean encryptData) {
        this.encryptData = encryptData;
    }

    @Override
    public void setDecryptData(boolean decryptData) {
        this.decryptData = decryptData;
    }

    @Override
    public void setStatisticsUpdater(StatisticsUpdater statisticsUpdater) {
        this.statisticsUpdater = statisticsUpdater;
    }

    @Override
    public JdbcDao getDao() {
        return getDao(serializerProvider);
    }

    @Override
    public JdbcDao getDao(SerializerProvider serializerProvider) {
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

        return getDao(donkey, connection, querySource, statementSource, serializerProvider, encryptData, decryptData, statisticsUpdater, channelController.getStatistics(), channelController.getTotalStatistics(), statsServerId);
    }
    
	protected JdbcDao getDao(Donkey donkey, Connection connection, QuerySource querySource,
			PreparedStatementSource statementSource, SerializerProvider serializerProvider, boolean encryptData,
			boolean decryptData, StatisticsUpdater statisticsUpdater, Statistics currentStats, Statistics totalStats,
			String statsServerId) {
		return new JdbcDao(donkey, connection, querySource, statementSource, serializerProvider, encryptData, decryptData, statisticsUpdater, currentStats, totalStats, statsServerId);
	}
}
