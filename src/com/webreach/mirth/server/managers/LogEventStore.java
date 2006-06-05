package com.webreach.mirth.server.managers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.LogEvent;
import com.webreach.mirth.server.core.util.DatabaseConnection;
import com.webreach.mirth.server.core.util.DatabaseUtil;

public class LogEventStore {
	private Logger logger = Logger.getLogger(LogEventStore.class);
	private DatabaseConnection dbConnection;
	
	/**
	 * Adds a new event to the database.
	 * 
	 * @param logEvent
	 * @throws ManagerException
	 */
	public void addEvent(LogEvent logEvent) throws ManagerException {
		logger.debug("adding log message to channel " + logEvent.getChannelId());
		
		try {
			dbConnection = new DatabaseConnection();	
			StringBuffer insert = new StringBuffer();
			insert.append("INSERT INTO EVENTS (CHANNEL_ID, DATE_CREATED, EVENT, EVENT_LEVEL) VALUES(");
			insert.append(logEvent.getChannelId() + ", ");
			insert.append("'" + DatabaseUtil.getNowTimestamp() + "', ");
			insert.append("'" + logEvent.getEvent() + "', ");
			insert.append(logEvent.getLevel() + ";");
			dbConnection.update(insert.toString());
		} catch (Exception e) {
			throw new ManagerException("Could not add log for channel " + logEvent.getChannelId(), e);
		}
	}
	
	/**
	 * Returns a List of all events.
	 * 
	 * @param channelId
	 * @return
	 * @throws ManagerException
	 */
	public List<LogEvent> getEvents(int channelId) throws ManagerException {
		logger.debug("retrieving log list");
		
		ArrayList<LogEvent> logEvents = new ArrayList<LogEvent>();
		ResultSet result = null;
		
		try {
			dbConnection = new DatabaseConnection();
			StringBuffer query = new StringBuffer();
			query.append("SELECT ID, CHANNEL_ID, DATE_CREATED, EVENT, EVENT_LEVEL FROM EVENTS");
			query.append(" WHERE CHANNEL_ID = " + channelId + ";");
			result = dbConnection.query(query.toString());

			while (result.next()) {
				LogEvent logEvent = new LogEvent();
				logEvent.setId(result.getInt("ID"));
				logEvent.setChannelId(result.getInt("CHANNEL_ID"));
				logEvent.setDate(result.getTimestamp("DATE_CREATED"));
				logEvent.setEvent(result.getString("EVENT"));
				logEvent.setLevel(result.getInt("EVENT_LEVEL"));
				logEvents.add(logEvent);
			}

			return logEvents;
		} catch (SQLException e) {
			throw new ManagerException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}		
	}
}
