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
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.donkey.server.data.DonkeyDaoException;
import com.mirth.connect.donkey.server.data.StatisticsUpdater;
import com.mirth.connect.donkey.util.SerializerProvider;

public class OracleDaoFactory extends JdbcDaoFactory {
    private final static String COMMAND = "COMMIT WRITE BATCH NOWAIT";
    private final static int REQUIRED_MAJOR_VERSION = 10;
    private final static int REQUIRED_MINOR_VERSION = 2;

    private Boolean supported;
    private Logger logger = Logger.getLogger(this.getClass());

    @Override
    public JdbcDao getDao(SerializerProvider serializerProvider) {
        JdbcDao dao = super.getDao(serializerProvider);
        dao.setAsyncCommitCommand(getAsyncCommitCommand(dao));
        return dao;
    }

    @Override
    protected JdbcDao getDao(Donkey donkey, Connection connection, QuerySource querySource,
    		PreparedStatementSource statementSource, SerializerProvider serializerProvider, boolean encryptData,
    		boolean decryptData, StatisticsUpdater statisticsUpdater, Statistics currentStats, Statistics totalStats,
    		String statsServerId) {
    	return new OracleJdbcDao(donkey, connection, querySource, statementSource, serializerProvider, encryptData, decryptData, statisticsUpdater, currentStats, totalStats, statsServerId);
    }

    private String getAsyncCommitCommand(JdbcDao dao) {
        if (supported == null) {
            try {
                DatabaseMetaData metaData = dao.getConnection().getMetaData();
                int majorVersion = metaData.getDatabaseMajorVersion();
                int minorVersion = metaData.getDatabaseMinorVersion();

                if (majorVersion > REQUIRED_MAJOR_VERSION || (majorVersion == REQUIRED_MAJOR_VERSION && minorVersion >= REQUIRED_MINOR_VERSION)) {
                    supported = true;
                    logger.debug("Detected support for asynchronous commits in Oracle");
                } else {
                    supported = false;
                    logger.debug("Asynchronous commits are not supported by the current Oracle connection");
                }
            } catch (SQLException e) {
                throw new DonkeyDaoException(e);
            }
        }

        return (supported) ? COMMAND : null;
    }
}
