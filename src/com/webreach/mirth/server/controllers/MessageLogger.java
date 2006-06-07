package com.webreach.mirth.server.controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.MessageEvent;
import com.webreach.mirth.server.core.util.DatabaseConnection;
import com.webreach.mirth.server.core.util.DatabaseUtil;

/**
 * The MessageLogger is used to store messages as they are processes by a channel.
 * 
 * @author GeraldB
 *
 */
public class MessageLogger {
	private Logger logger = Logger.getLogger(MessageLogger.class);
	private DatabaseConnection dbConnection;
	
	/**
	 * Adds a new message to the database.
	 * 
	 * @param messageEvent
	 * @throws ControllerException
	 */
	public void logMessageEvent(MessageEvent messageEvent) throws ControllerException {
		logger.debug("adding message event: " + messageEvent.getChannelId());
		
		try {
			dbConnection = new DatabaseConnection();	
			StringBuffer insert = new StringBuffer();
			insert.append("INSERT INTO MESSAGES (CHANNEL_ID, DATE_CREATED, SENDING_FACILITY, EVENT, CONTROL_ID, MESSAGE) VALUES(");
			insert.append(messageEvent.getChannelId() + ", ");
			insert.append("'" + DatabaseUtil.getNowTimestamp() + "', ");
			insert.append("'" + messageEvent.getSendingFacility() + "', ");
			insert.append("'" + messageEvent.getEvent() + "', ");
			insert.append("'" + messageEvent.getControlId() + "', ");
			insert.append("'" + messageEvent.getMessage() + "');");
			dbConnection.update(insert.toString());
		} catch (Exception e) {
			throw new ControllerException("Could not add message for channel " + messageEvent.getChannelId(), e);
		}
	}
	
	/**
	 * Returns a List of all messages.
	 * 
	 * @param channelId
	 * @return
	 * @throws ControllerException
	 */
	public List<MessageEvent> getMessageEvents(int channelId) throws ControllerException {
		logger.debug("retrieving message event list: " + channelId);
		
		ArrayList<MessageEvent> messageEvents = new ArrayList<MessageEvent>();
		ResultSet result = null;
		
		try {
			dbConnection = new DatabaseConnection();
			StringBuffer query = new StringBuffer();
			query.append("SELECT ID, CHANNEL_ID, DATE_CREATED, SENDING_FACILITY, EVENT, CONTROL_ID, MESSAGE FROM MESSAGES");
			query.append(" WHERE CHANNEL_ID = " + channelId + ";");
			result = dbConnection.query(query.toString());

			while (result.next()) {
				MessageEvent messageEvent = new MessageEvent();
				messageEvent.setId(result.getInt("ID"));
				messageEvent.setChannelId(result.getInt("CHANNEL_ID"));
				messageEvent.setDate(result.getTimestamp("DATE_CREATED"));
				messageEvent.setSendingFacility(result.getString("SENDING_FACILITY"));
				messageEvent.setEvent(result.getString("EVENT"));
				messageEvent.setControlId(result.getString("CONTROL_ID"));
				messageEvent.setMessage(result.getString("MESSAGE"));
				messageEvents.add(messageEvent);
			}

			return messageEvents;
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}		
	}

}
