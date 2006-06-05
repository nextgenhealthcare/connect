package com.webreach.mirth.server.managers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.MessageEvent;
import com.webreach.mirth.server.core.util.DatabaseConnection;
import com.webreach.mirth.server.core.util.DatabaseUtil;

public class MessageEventStore {
	private Logger logger = Logger.getLogger(MessageEventStore.class);
	private DatabaseConnection dbConnection;
	
	/**
	 * Adds a new message to the database.
	 * 
	 * @param messageEvent
	 * @throws ManagerException
	 */
	public void addMessageEvent(MessageEvent messageEvent) throws ManagerException {
		logger.debug("adding message to channel " + messageEvent.getChannelId());
		
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
			throw new ManagerException("Could not add message for channel " + messageEvent.getChannelId(), e);
		}
	}
	
	/**
	 * Returns a List of all messages.
	 * 
	 * @param channelId
	 * @return
	 * @throws ManagerException
	 */
	public List<MessageEvent> getMessageEvents(int channelId) throws ManagerException {
		logger.debug("retrieving message list");
		
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
			throw new ManagerException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}		
	}

}
