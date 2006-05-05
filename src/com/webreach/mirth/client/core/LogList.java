/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.client.core;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Log;
import com.webreach.mirth.server.core.handlers.LogListHandler;
import com.webreach.mirth.server.core.util.DatabaseConnection;

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
