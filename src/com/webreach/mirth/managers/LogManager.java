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


package com.webreach.mirth.managers;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.webreach.mirth.managers.types.LogMessage;

public class LogManager {
	protected transient Log logger = LogFactory.getLog(ChangeManager.class);
	private boolean initialized = false;
	
	// singleton pattern
	private static LogManager instance = null;

	private LogManager() {}

	public static LogManager getInstance() {
		synchronized (LogManager.class) {
			if (instance == null)
				instance = new LogManager();

			return instance;
		}
	}

	public void initialize() {
		if (initialized)
			return;

		// initialization code
		
		initialized = true;
	}

	public void log(String channel, String message) throws ManagerException {
		Date today = new Date();
		Timestamp now = new Timestamp(today.getTime());
		
		try {
			Database database = new Database("mirth");
			
			StringBuffer query = new StringBuffer();
			query.append("INSERT INTO logs(channel, tstamp, message) VALUES('");
			query.append(channel);
			query.append("', '");
			query.append(now.toString());
			query.append("', '");
			query.append(message);
			query.append("');");
			
			database.update(query.toString());
		} catch (Exception e) {
			throw new ManagerException("Could not log message.", e);
		}
	}
	
	public void clearLogs(String name) {
		try {
			Database database = new Database("mirth");
			
			StringBuffer query = new StringBuffer();
			query.append("DELETE FROM logs WHERE channel='");
			query.append(name);
			query.append("';");
			
			database.update(query.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<LogMessage> getChannelLogs(String name) {
		ArrayList<LogMessage> channelLogs = new ArrayList<LogMessage>();

		try {
			Database database = new Database("mirth");
			
			StringBuffer query = new StringBuffer();
			query.append("SELECT id, tstamp, message FROM logs WHERE channel='");
			query.append(name);
			query.append("';");
			
			ResultSet result = database.query(query.toString());
			
			while (result.next()) {
				LogMessage logMessage = new LogMessage();
				logMessage.setId(result.getString(1));
				logMessage.setDate(result.getString(2));
				logMessage.setMessage(result.getString(3));
				channelLogs.add(logMessage);
			}
		} catch (Exception e) {
			throw new ManagerException("Could not log message.", e);
		}
		
		return channelLogs;
	}
}
