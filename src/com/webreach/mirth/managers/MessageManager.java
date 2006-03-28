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

import com.webreach.mirth.managers.types.MirthMessage;

public class MessageManager {
	protected transient Log logger = LogFactory.getLog(MessageManager.class);
	private boolean initialized = false;
	
	// singleton pattern
	private static MessageManager instance = null;

	private MessageManager() {}

	public static MessageManager getInstance() {
		synchronized (MessageManager.class) {
			if (instance == null)
				instance = new MessageManager();

			return instance;
		}
	}

	/**
	 * Initializes the MessageManager.
	 * 
	 */
	public void initialize() {
		if (initialized)
			return;

		// initialization code

		initialized = true;
	}

	public void addMessage(MirthMessage message) {
		logger.debug("adding message to message list");

		Date today = new Date();
		Timestamp now = new Timestamp(today.getTime());
		
		try {
			Database database = new Database("mirth");
			
			StringBuffer query = new StringBuffer();
			query.append("INSERT INTO messages(channel, tstamp, source, event, msgid, bytesize, content, contentxml) VALUES('");
			query.append(message.getChannel());
			query.append("', '");
			
			query.append(now.toString());
			query.append("', '");

			query.append(message.getSendingFacility());
			query.append("', '");

			query.append(message.getEvent());
			query.append("', '");

			query.append(message.getControlId());
			query.append("', '");
			
			query.append(message.getSize());
			query.append("', '");

			query.append(message.getContent());
			query.append("', '");

			query.append(message.getContentXml());
			query.append("');");

			database.update(query.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<MirthMessage> getChannelMessages(String name) {
		ArrayList<MirthMessage> channelMessages = new ArrayList<MirthMessage>();
		
		try {
			Database database = new Database("mirth");
			
			StringBuffer query = new StringBuffer();
			query.append("SELECT id, tstamp, source, event, msgid, bytesize, content, contentxml FROM messages WHERE channel='");
			query.append(name);
			query.append("';");
			
			ResultSet result = database.query(query.toString());
			
			while (result.next()) {
				MirthMessage mirthMessage = new MirthMessage();
				
				mirthMessage.setId(result.getString(1));
				mirthMessage.setDate(result.getString(2));
				mirthMessage.setSendingFacility(result.getString(3));
				mirthMessage.setEvent(result.getString(4));
				mirthMessage.setControlId(result.getString(5));
				mirthMessage.setSize(result.getString(6));
				mirthMessage.setContent(result.getString(7));
				mirthMessage.setContentXml(result.getString(8));
				
				channelMessages.add(mirthMessage);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return channelMessages;
	}
	
	public void clearMessages(String name) {
		try {
			Database database = new Database("mirth");
			
			StringBuffer query = new StringBuffer();
			query.append("DELETE FROM messages WHERE channel='");
			query.append(name);
			query.append("';");
			
			database.update(query.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getMessageContent(String id) {
		try {
			Database database = new Database("mirth");
			
			StringBuffer query = new StringBuffer();
			query.append("SELECT content FROM messages WHERE id='");
			query.append(id);
			query.append("';");
			
			ResultSet result = database.query(query.toString());
			
			String message = null;
			
			while (result.next()) {
				message = result.getString(1);
			}
			
			return message;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public String getMessageContentXml(String id) {
		try {
			Database database = new Database("mirth");
			
			StringBuffer query = new StringBuffer();
			query.append("SELECT contentxml FROM messages WHERE id='");
			query.append(id);
			query.append("';");
			
			ResultSet result = database.query(query.toString());
			
			String message = null;
			while (result.next()) {
				message = result.getString(1);
			}
			
			return replaceSpecialCharacters(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String replaceSpecialCharacters(String xml) {
		String clean = xml;
		clean = clean.replaceAll("<", "&lt;");
		clean = clean.replaceAll(">", "&gt;");
		return clean;
	}
}
