package com.webreach.mirth.server.managers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.LogEntry;
import com.webreach.mirth.server.core.util.DatabaseConnection;
import com.webreach.mirth.server.core.util.DatabaseUtil;

public class LogEntryManager {
	private Logger logger = Logger.getLogger(LogEntryManager.class);
	private DatabaseConnection dbConnection;
	
	/**
	 * Adds a new log message to the database.
	 * 
	 * @param logEntry
	 * @throws ManagerException
	 */
	public void addLogEntry(LogEntry logEntry) throws ManagerException {
		logger.debug("adding log message to channel " + logEntry.getChannelId());
		
		try {
			dbConnection = new DatabaseConnection();	
			StringBuffer insert = new StringBuffer();
			insert.append("INSERT INTO LOGS (CHANNEL_ID, DATE_CREATED, EVENT, EVENT_LEVEL) VALUES(");
			insert.append(logEntry.getChannelId() + ", ");
			insert.append("'" + DatabaseUtil.getNowTimestamp() + "', ");
			insert.append("'" + logEntry.getEvent() + "', ");
			insert.append(logEntry.getLevel() + ";");
			dbConnection.update(insert.toString());
		} catch (Exception e) {
			throw new ManagerException("Could not add log for channel " + logEntry.getChannelId(), e);
		}
	}
	
	/**
	 * Returns a List of all log messages.
	 * 
	 * @param channelId
	 * @return
	 * @throws ManagerException
	 */
	public List<LogEntry> getLogEntries(int channelId) throws ManagerException {
		logger.debug("retrieving log list");
		
		ArrayList<LogEntry> logEntries = new ArrayList<LogEntry>();
		ResultSet result = null;
		
		try {
			dbConnection = new DatabaseConnection();
			StringBuffer query = new StringBuffer();
			query.append("SELECT ID, CHANNEL_ID, DATE_CREATED, EVENT, EVENT_LEVEL FROM LOGS");
			query.append(" WHERE CHANNEL_ID = " + channelId + ";");
			result = dbConnection.query(query.toString());

			while (result.next()) {
				LogEntry logEntry = new LogEntry();
				logEntry.setId(result.getInt("ID"));
				logEntry.setChannelId(result.getInt("CHANNEL_ID"));
				logEntry.setDate(result.getTimestamp("DATE_CREATED"));
				logEntry.setEvent(result.getString("EVENT"));
				logEntry.setLevel(result.getInt("EVENT_LEVEL"));
				logEntries.add(logEntry);
			}

			return logEntries;
		} catch (SQLException e) {
			throw new ManagerException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}		
	}
}
