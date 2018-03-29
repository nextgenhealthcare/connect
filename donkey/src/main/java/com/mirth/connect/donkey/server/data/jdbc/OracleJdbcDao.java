package com.mirth.connect.donkey.server.data.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.donkey.server.data.StatisticsUpdater;
import com.mirth.connect.donkey.util.SerializerProvider;

public class OracleJdbcDao extends JdbcDao {

	public OracleJdbcDao(Donkey donkey, Connection connection, QuerySource querySource,
			PreparedStatementSource statementSource, SerializerProvider serializerProvider, boolean encryptData,
			boolean decryptData, StatisticsUpdater statisticsUpdater, Statistics currentStats, Statistics totalStats,
			String statsServerId) {
		super(donkey, connection, querySource, statementSource, serializerProvider, encryptData, decryptData,
				statisticsUpdater, currentStats, totalStats, statsServerId);
	}
	
	@Override
	protected void closeDatabaseObjectIfNeeded(AutoCloseable dbObject) {
		if (dbObject instanceof Statement) {
			close((Statement) dbObject);
		} else if (dbObject instanceof ResultSet) {
			close((ResultSet) dbObject);
		}
	}
	
	@Override
	protected void closeDatabaseObjectsIfNeeded(List<AutoCloseable> dbObjects) {
		for (AutoCloseable obj : dbObjects) {
			closeDatabaseObjectIfNeeded(obj);
		}
    }

}
