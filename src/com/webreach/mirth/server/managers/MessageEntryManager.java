package com.webreach.mirth.server.managers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.MessageEntry;
import com.webreach.mirth.server.core.util.DatabaseConnection;
import com.webreach.mirth.server.core.util.DatabaseUtil;

public class MessageEntryManager {
	private Logger logger = Logger.getLogger(MessageEntryManager.class);
	private DatabaseConnection dbConnection;
	
	/**
	 * Adds a new message to the database.
	 * 
	 * @param messageEntry
	 * @throws ManagerException
	 */
	public void addMessageEntry(MessageEntry messageEntry) throws ManagerException {
		logger.debug("adding message to channel " + messageEntry.getChannelId());
		
		try {
			dbConnection = new DatabaseConnection();	
			StringBuffer insert = new StringBuffer();
			insert.append("INSERT INTO MESSAGES (CHANNEL_ID, DATE_CREATED, SENDING_FACILITY, EVENT, CONTROL_ID, MESSAGE) VALUES(");
			insert.append(messageEntry.getChannelId() + ", ");
			insert.append("'" + DatabaseUtil.getNowTimestamp() + "', ");
			insert.append("'" + messageEntry.getSendingFacility() + "', ");
			insert.append("'" + messageEntry.getEvent() + "', ");
			insert.append("'" + messageEntry.getControlId() + "', ");
			insert.append("'" + messageEntry.getMessage() + "');");
			dbConnection.update(insert.toString());
		} catch (Exception e) {
			throw new ManagerException("Could not add message for channel " + messageEntry.getChannelId(), e);
		}
	}
	
	/**
	 * Returns a List of all messages.
	 * 
	 * @param channelId
	 * @return
	 * @throws ManagerException
	 */
	public List<MessageEntry> getMessageEntries(int channelId) throws ManagerException {
		logger.debug("retrieving message list");
		
		ArrayList<MessageEntry> messageEntries = new ArrayList<MessageEntry>();
		ResultSet result = null;
		
		try {
			dbConnection = new DatabaseConnection();
			StringBuffer query = new StringBuffer();
			query.append("SELECT ID, CHANNEL_ID, DATE_CREATED, SENDING_FACILITY, EVENT, CONTROL_ID, MESSAGE FROM MESSAGES");
			query.append(" WHERE CHANNEL_ID = " + channelId + ";");
			result = dbConnection.query(query.toString());

			while (result.next()) {
				MessageEntry messageEntry = new MessageEntry();
				messageEntry.setId(result.getInt("ID"));
				messageEntry.setChannelId(result.getInt("CHANNEL_ID"));
				messageEntry.setDate(result.getTimestamp("DATE_CREATED"));
				messageEntry.setSendingFacility(result.getString("SENDING_FACILITY"));
				messageEntry.setEvent(result.getString("EVENT"));
				messageEntry.setControlId(result.getString("CONTROL_ID"));
				messageEntry.setMessage(result.getString("MESSAGE"));
				messageEntries.add(messageEntry);
			}

			return messageEntries;
		} catch (SQLException e) {
			throw new ManagerException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}		
	}

}
