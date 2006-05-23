package com.webreach.mirth.server.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Log;
import com.webreach.mirth.server.core.util.DatabaseConnection;
import com.webreach.mirth.server.core.util.DatabaseUtil;

public class LogService {
	private Logger logger = Logger.getLogger(LogService.class);
	private DatabaseConnection dbConnection;
	
	/**
	 * Adds a new log message to the database.
	 * 
	 * @param log
	 * @throws ServiceException
	 */
	public void addLog(Log log) throws ServiceException {
		logger.debug("adding log message to channel " + log.getChannel());
		
		try {
			dbConnection = new DatabaseConnection();	
			StringBuffer insert = new StringBuffer();
			insert.append("INSERT INTO LOGS (CHANNEL_ID, DATE_CREATED, EVENT, EVENT_LEVEL) VALUES(");
			insert.append(log.getChannel() + ", ");
			insert.append("'" + DatabaseUtil.getNowTimestamp() + "', ");
			insert.append("'" + log.getEvent() + "', ");
			insert.append(log.getLevel() + ";");
			dbConnection.update(insert.toString());
		} catch (Exception e) {
			throw new ServiceException("Could not add log for channel " + log.getChannel(), e);
		}
	}
	
	/**
	 * Returns a List of all log messages.
	 * 
	 * @param channelId
	 * @return
	 * @throws ServiceException
	 */
	public List<Log> getLogs(int channelId) throws ServiceException {
		logger.debug("retrieving log list");
		
		ArrayList<Log> logs = new ArrayList<Log>();
		ResultSet result = null;
		
		try {
			dbConnection = new DatabaseConnection();
			StringBuffer query = new StringBuffer();
			query.append("SELECT ID, CHANNEL_ID, DATE_CREATED, EVENT, EVENT_LEVEL FROM LOGS");
			query.append(" WHERE CHANNEL_ID = " + channelId + ";");
			result = dbConnection.query(query.toString());

			while (result.next()) {
				Log log = new Log();
				log.setId(result.getInt("ID"));
				log.setChannel(result.getInt("CHANNEL_ID"));
				log.setDate(result.getTimestamp("DATE_CREATED"));
				log.setEvent(result.getString("EVENT"));
				log.setLevel(result.getInt("EVENT_LEVEL"));
				logs.add(log);
			}

			return logs;
		} catch (SQLException e) {
			throw new ServiceException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}		
	}
}
