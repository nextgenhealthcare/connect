package com.webreach.mirth.server.managers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.server.core.util.DatabaseConnection;
import com.webreach.mirth.server.core.util.DatabaseUtil;

public class SystemLogger {
	private Logger logger = Logger.getLogger(SystemLogger.class);
	private DatabaseConnection dbConnection;
	
	/**
	 * Adds a new system event.
	 * 
	 * @param systemEvent
	 * @throws ControllerException
	 */
	public void logSystemEvent(SystemEvent systemEvent) throws ControllerException {
		logger.debug("adding log event: " + systemEvent.getChannelId());
		
		try {
			dbConnection = new DatabaseConnection();	
			StringBuffer insert = new StringBuffer();
			insert.append("INSERT INTO EVENTS (CHANNEL_ID, DATE_CREATED, EVENT, EVENT_LEVEL) VALUES(");
			insert.append(systemEvent.getChannelId() + ", ");
			insert.append("'" + DatabaseUtil.getNowTimestamp() + "', ");
			insert.append("'" + systemEvent.getDescription() + "', ");
			insert.append(systemEvent.getLevel() + ";");
			dbConnection.update(insert.toString());
		} catch (Exception e) {
			throw new ControllerException("Could not add system event for channel " + systemEvent.getChannelId(), e);
		}
	}
	
	/**
	 * Returns a List of all system events.
	 * 
	 * @param channelId
	 * @return
	 * @throws ControllerException
	 */
	public List<SystemEvent> getSystemEvents(int channelId) throws ControllerException {
		logger.debug("retrieving log event list: " + channelId);
		
		ArrayList<SystemEvent> logEvents = new ArrayList<SystemEvent>();
		ResultSet result = null;
		
		try {
			dbConnection = new DatabaseConnection();
			StringBuffer query = new StringBuffer();
			query.append("SELECT ID, CHANNEL_ID, DATE_CREATED, EVENT, EVENT_LEVEL FROM EVENTS");
			query.append(" WHERE CHANNEL_ID = " + channelId + ";");
			result = dbConnection.query(query.toString());

			while (result.next()) {
				SystemEvent logEvent = new SystemEvent();
				logEvent.setId(result.getInt("ID"));
				logEvent.setChannelId(result.getInt("CHANNEL_ID"));
				logEvent.setDate(result.getTimestamp("DATE_CREATED"));
				logEvent.setDescription(result.getString("EVENT"));
				logEvent.setLevel(result.getInt("EVENT_LEVEL"));
				logEvents.add(logEvent);
			}

			return logEvents;
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}		
	}
}
