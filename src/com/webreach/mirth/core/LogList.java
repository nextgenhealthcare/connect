package com.webreach.mirth.core;

import com.webreach.mirth.core.handlers.LogListHandler;
import com.webreach.mirth.core.util.DatabaseConnection;

public class LogList {
	private Channel channel;
	private String table = "LOGS";
	
	public LogList(Channel channel) {
		this.channel = channel;
	}
	
	// add a log to the log list
	public void add(Log log) {
		DatabaseConnection dbConnection = new DatabaseConnection();
		StringBuffer insert = new StringBuffer();

		insert.append("INSERT INTO " + table + " (CHANNEL_NAME, DATE_CREATED, EVENT_LEVEL, EVENT) VALUES (");
		insert.append("'" + channel.getName() + "', ");
		insert.append("'" + log.getDate() + "', ");
		insert.append("'" + log.getLevel() + "', ");
		insert.append("'" + log.getEvent() + "');");
		
		try {
			dbConnection.update(insert.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbConnection.close();
		}
	}
	
	// return all logs
	public LogListHandler getMessages() {
		StringBuffer query = new StringBuffer();
		query.append("SELECT * FROM " + table + " WHERE CHANNEL_NAME='" + channel.getName() + "';");
		return new LogListHandler(query.toString());	
	}

	// return logs by id range
	public LogListHandler getLogsByIdRange(int min, int max) {
		StringBuffer query = new StringBuffer();
		query.append("SELECT * FROM " + table + " WHERE ID >= " + min + " AND ID <= " + max + " AND CHANNEL_NAME='" + channel.getName() + "';");
		return new LogListHandler(query.toString());	
	}

	// return logs by date range
	public LogListHandler getLogsByDateRange(String min, String max) {
		StringBuffer query = new StringBuffer();
		query.append("SELECT * FROM " + table + " WHERE DATE_CREATED >= '" + min + "' AND DATE_CREATED <= '" + max + "' AND CHANNEL_NAME='" + channel.getName() + "';");
		return new LogListHandler(query.toString());	
	}

}
