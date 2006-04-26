package com.webreach.mirth.core;

import com.webreach.mirth.core.handlers.MessageListHandler;
import com.webreach.mirth.core.util.DatabaseConnection;

public class MessageList {
	private Channel channel;
	private String table = "MESSAGES";
	
	public MessageList(Channel channel) {
		this.channel = channel;
	}
	
	// add a message to the message list
	public void add(Message message) {
		DatabaseConnection dbConnection = new DatabaseConnection();
		StringBuffer insert = new StringBuffer();

		insert.append("INSERT INTO " + table + " (CHANNEL_NAME, DATE_CREATED, SENDING_FACILITY, EVENT, CONTROL_ID, MESSAGE) VALUES (");
		insert.append("'" + channel.getName() + "', ");
		insert.append("'" + message.getDate() + "', ");
		insert.append("'" + message.getSendingFacility() + "', ");
		insert.append("'" + message.getEvent() + "', ");
		insert.append("'" + message.getControlId() + "', ");
		insert.append("'" + message.getMessage() + "');");
		
		try {
			dbConnection.update(insert.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbConnection.close();
		}
	}
	
	// return all messages
	public MessageListHandler getMessages() {
		StringBuffer query = new StringBuffer();
		query.append("SELECT * FROM " + table + " WHERE CHANNEL_NAME='" + channel.getName() + "';");
		return new MessageListHandler(query.toString());	
	}

	// return messages by id range
	public MessageListHandler getMessagesByIdRange(int min, int max) {
		StringBuffer query = new StringBuffer();
		query.append("SELECT * FROM " + table + " WHERE ID >= " + min + " AND ID <= " + max + " AND CHANNEL_NAME='" + channel.getName() + "';");
		return new MessageListHandler(query.toString());	
	}

	// return messages by date range
	public MessageListHandler getMessagesByDateRange(String min, String max) {
		StringBuffer query = new StringBuffer();
		query.append("SELECT * FROM " + table + " WHERE DATE_CREATED >= " + min + " AND DATE_CREATED <= " + max + " AND CHANNEL_NAME='" + channel.getName() + "';");
		return new MessageListHandler(query.toString());	
	}

}
