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

package com.webreach.mirth.server.controllers;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mule.extras.client.MuleClient;
import org.mule.umo.UMOException;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import com.webreach.mirth.server.util.SqlConfig;

public class MessageObjectController {
	private Logger logger = Logger.getLogger(this.getClass());
	private SqlMapClient sqlMap = SqlConfig.getSqlMapInstance();

	public void updateMessage(MessageObject messageObject) {
		try {
			String channelId = messageObject.getChannelId();
			HashMap<String,Channel> channelCache = ChannelController.getChannelCache();
			//Check the cache for the channel
			if (channelCache != null && channelCache.containsKey(channelId)){
				Channel channel = channelCache.get(channelId);
				if (channel.getProperties().containsKey("store_messages")){
					if (channel.getProperties().get("store_messages").equals("false")){
						//If we don't want to store messages, then lets sanitize the data in a clone
						//TODO: Check if pass by value
						messageObject = (MessageObject)messageObject.clone();
						messageObject.setRawData(new String());
						messageObject.setEncodedData(new String());
						messageObject.setTransformedData(new String());
						messageObject.setRawData(new String());
						messageObject.setVariableMap(new HashMap());
					}else if(channel.getProperties().get("store_messages").equals("true") && channel.getProperties().get("error_messages_only").equals("true")){
						//Check if it's not an error message and sanitize
						if (!messageObject.getStatus().equals(MessageObject.Status.ERROR)){
							messageObject = (MessageObject)messageObject.clone();
							messageObject.setRawData(new String());
							messageObject.setEncodedData(new String());
							messageObject.setTransformedData(new String());
							messageObject.setRawData(new String());
							messageObject.setVariableMap(new HashMap());
						}
					}
				}

			}
			int count = (Integer) sqlMap.queryForObject("getMessageCount", messageObject.getId());
			
			if (count == 0) {
				logger.debug("adding message: id=" + messageObject.getId());
				sqlMap.insert("insertMessage", messageObject);
			} else {
				logger.debug("updating message: id=" + messageObject.getId());
				sqlMap.update("updateMessage", messageObject);
			}
		} catch (Exception e) {
			logger.error("could not log message: id=" + messageObject.getId(), e);
		}
	}

	public int createMessagesTempTable(MessageObjectFilter filter, String uid) throws ControllerException {
		logger.debug("creating temporary message table: filter=" + filter.toString());

		try {
			sqlMap.update("dropTempMessageTable", uid);
		} catch (SQLException e) {
			// supress any warnings about the table not existing
			logger.debug(e);
		}
		
		try {
			sqlMap.update("createTempMessageTable", uid);
			sqlMap.update("createTempMessageTableIndex", uid);
			return sqlMap.update("populateTempMessageTable", getFilterMap(filter, uid));
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public List<MessageObject> getMessagesByPage(int page, int pageSize, String uid) throws ControllerException {
		logger.debug("retrieving messages by page: page=" + page);

		try {
			Map parameterMap = new HashMap();
			parameterMap.put("uid", uid);

			if ((page != -1) && (pageSize != -1)) {
				int first = (page * pageSize) + 1;
				int last = (first + pageSize) - 1;
				parameterMap.put("first", first);
				parameterMap.put("last", last);
			}
			
			return sqlMap.queryForList("getMessageByPage", parameterMap);
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public void removeMessages(MessageObjectFilter filter) throws ControllerException {
		logger.debug("removing messages: filter=" + filter.toString());

		try {
			sqlMap.delete("deleteMessage", getFilterMap(filter, null));
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public void clearMessages(String channelId) throws ControllerException {
		logger.debug("clearing messages: channelId=" + channelId);

		try {
			Map parameterMap = new HashMap();
			parameterMap.put("channelId", channelId);
			sqlMap.delete("deleteMessage", parameterMap);
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public void reprocessMessages(MessageObjectFilter filter, String uid) throws ControllerException {
		createMessagesTempTable(filter, uid);
		List<MessageObject> messages = getMessagesByPage(-1, -1, uid);

		try {
			MuleClient client = new MuleClient();

			for (Iterator iter = messages.iterator(); iter.hasNext();) {
				MessageObject message = (MessageObject) iter.next();
				client.dispatch("vm://" + message.getChannelId(), message.getRawData(), null);
			}
		} catch (UMOException e) {
			throw new ControllerException("could not reprocess message", e);
		}
	}
	
	private Map getFilterMap(MessageObjectFilter filter, String uid) {
		Map parameterMap = new HashMap();
		
		if (uid != null) {
			parameterMap.put("uid", uid);	
		}
		
		parameterMap.put("id", filter.getId());
		parameterMap.put("channelId", filter.getChannelId());
		parameterMap.put("status", filter.getStatus());
		parameterMap.put("type", filter.getType());
		parameterMap.put("status", filter.getStatus());
		parameterMap.put("connectorName", filter.getConnectorName());

		if (filter.getStartDate() != null) {
			parameterMap.put("startDate", String.format("%1$tY-%1$tm-%1$td 00:00:00", filter.getStartDate()));	
		}
		
		if (filter.getEndDate() != null) {
			parameterMap.put("endDate", String.format("%1$tY-%1$tm-%1$td 23:59:59", filter.getEndDate()));	
		}

		return parameterMap;
	}
}
