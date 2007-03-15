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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mule.umo.UMOEvent;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.Response;
import com.webreach.mirth.model.converters.ObjectClonerException;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import com.webreach.mirth.server.builders.ErrorMessageBuilder;
import com.webreach.mirth.server.util.SqlConfig;
import com.webreach.mirth.server.util.UUIDGenerator;
import com.webreach.mirth.server.util.VMRouter;
import com.webreach.mirth.util.Encrypter;

public class MessageObjectController {
	private Logger logger = Logger.getLogger(this.getClass());
	private SqlMapClient sqlMap = SqlConfig.getSqlMapInstance();
	private static final String MESSAGE_NO_DATA_STORE = "No data stored for this channel.";
	private ConfigurationController configurationController = new ConfigurationController();
	private String lineSeperator = System.getProperty("line.separator");
	private ErrorMessageBuilder errorBuilder = new ErrorMessageBuilder();

	public void updateMessage(MessageObject messageObject) {
		try {
			String channelId = messageObject.getChannelId();
			HashMap<String, Channel> channelCache = ChannelController.getChannelCache();

			// Check the cache for the channel
			if (channelCache != null && channelCache.containsKey(channelId)) {
				Channel channel = channelCache.get(channelId);

				if (channel.getProperties().containsKey("store_messages")) {
					if (channel.getProperties().get("store_messages").equals("false") || (channel.getProperties().get("store_messages").equals("true") && channel.getProperties().get("error_messages_only").equals("true") && !messageObject.getStatus().equals(MessageObject.Status.ERROR)) || (channel.getProperties().get("store_messages").equals("true") && channel.getProperties().get("dont_store_filtered").equals("true") && messageObject.getStatus().equals(MessageObject.Status.FILTERED))) {
						// If we don't want to store messages, then lets
						// sanitize the data in a clone
						// TODO: Check if pass by value
						messageObject = (MessageObject) messageObject.clone();
						messageObject.setRawData(MESSAGE_NO_DATA_STORE);
						messageObject.setEncodedData(MESSAGE_NO_DATA_STORE);
						messageObject.setTransformedData(MESSAGE_NO_DATA_STORE);
						messageObject.setConnectorMap(new HashMap());
					} else if (channel.getProperties().getProperty("encryptData").equals("true")) {
						encryptMessageData(messageObject);
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

	private void encryptMessageData(MessageObject messageObject) {
		Encrypter encrypter = new Encrypter(configurationController.getEncryptionKey());

		String encryptedRawData = encrypter.encrypt(messageObject.getRawData());
		String encryptedTransformedData = encrypter.encrypt(messageObject.getTransformedData());
		String encryptedEncodedData = encrypter.encrypt(messageObject.getEncodedData());

		messageObject.setRawData(encryptedRawData);
		messageObject.setTransformedData(encryptedTransformedData);
		messageObject.setEncodedData(encryptedEncodedData);

		messageObject.setEncrypted(true);
	}

	private void decryptMessageData(MessageObject messageObject) {
		if (messageObject.isEncrypted()) {
			Encrypter encrypter = new Encrypter(configurationController.getEncryptionKey());

			String decryptedRawData = encrypter.decrypt(messageObject.getRawData());
			String decryptedTransformedData = encrypter.decrypt(messageObject.getTransformedData());
			String decryptedEncodedData = encrypter.decrypt(messageObject.getEncodedData());

			messageObject.setRawData(decryptedRawData);
			messageObject.setTransformedData(decryptedTransformedData);
			messageObject.setEncodedData(decryptedEncodedData);
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

			List<MessageObject> messages = sqlMap.queryForList("getMessageByPage", parameterMap);

			for (Iterator iter = messages.iterator(); iter.hasNext();) {
				MessageObject messageObject = (MessageObject) iter.next();
				decryptMessageData(messageObject);
			}

			return messages;
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
			VMRouter router = new VMRouter();

			for (Iterator iter = messages.iterator(); iter.hasNext();) {
				MessageObject message = (MessageObject) iter.next();
				router.routeMessageByChannelId(message.getChannelId(), message.getRawData(), true);
			}
		} catch (Exception e) {
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
		parameterMap.put("protocol", filter.getProtocol());

		if (filter.getStartDate() != null) {
			parameterMap.put("startDate", String.format("%1$tY-%1$tm-%1$td 00:00:00", filter.getStartDate()));
		}

		if (filter.getEndDate() != null) {
			parameterMap.put("endDate", String.format("%1$tY-%1$tm-%1$td 23:59:59", filter.getEndDate()));
		}

		return parameterMap;
	}

	public MessageObject cloneMessageObjectForBroadcast(MessageObject messageObject, String connectorName) throws ObjectClonerException {
		MessageObject clone = (MessageObject) messageObject.clone();
		// We could use deep copy here, but see the notes below
		clone.setId(UUIDGenerator.getUUID());
		clone.setDateCreated(Calendar.getInstance());
		clone.setCorrelationId(messageObject.getId());
		clone.setConnectorName(connectorName);
		// We don't want to clone the maps from the original message...
		clone.setConnectorMap(new HashMap()); // the var map is local
		// ...or do we?
		// This works depending on clone or deepCopy.
		// If we deep copy, we need to set the response and context maps
		// If we clone, the clone is just setting references for us
		// Some might call that a bug, but we use it as a feature...
		// At least we're documenting it here.
		// clone.setResponseMap(new HashMap()); //maybe null???
		// clone.setContextMap(messageObject.getContextMap());
		return clone;
	}

	public MessageObject getMessageObjectFromEvent(UMOEvent event) throws Exception {
		MessageObject messageObject = null;
		Object incomingData = incomingData = event.getTransformedMessage();
		
		if (incomingData == null || !(incomingData instanceof MessageObject)) {
			logger.warn("received data is not of expected type");
			return null;
		}
		
		messageObject = (MessageObject) incomingData;
		
		if (messageObject.getStatus().equals(MessageObject.Status.FILTERED)) {
			return null;
		}
		
		return messageObject;
	}

	public void setError(MessageObject messageObject, String errorType, String errorMessage, Throwable e) {
		String fullErrorMessage = errorBuilder.buildErrorMessage(errorType, errorMessage, e);
		// send alert

		// Set the errors on the MO
		if (messageObject != null) {
			messageObject.setErrors(messageObject.getErrors() != null ? messageObject.getErrors() + lineSeperator + lineSeperator + fullErrorMessage : fullErrorMessage);
		}
		// Set the response error
		String responseException = new String();
		if (e != null) {
			responseException = "\t" + e.getClass().getSimpleName() + "\t" + e.getMessage();
		}
		setStatus(messageObject, MessageObject.Status.ERROR, Response.Status.FAILURE, errorMessage + responseException);
	}

	public void setSuccess(MessageObject messageObject, String responseMessage) {
		setStatus(messageObject, MessageObject.Status.SENT, Response.Status.SUCCESS, responseMessage);
	}

	public void setQueued(MessageObject messageObject, String responseMessage) {
		setStatus(messageObject, MessageObject.Status.QUEUED, Response.Status.QUEUED, responseMessage);
	}

	public void setFiltered(MessageObject messageObject, String responseMessage) {
		setStatus(messageObject, MessageObject.Status.FILTERED, Response.Status.FILTERED, responseMessage);
	}

	private void setStatus(MessageObject messageObject, MessageObject.Status status, Response.Status responseStatus, String responseMessage) {
		if (messageObject != null) {
			messageObject.setStatus(status);
			updateMessage(messageObject);
		}
		
		if (messageObject.getResponseMap() != null) {
			Response response = new Response(responseStatus, responseMessage);
			messageObject.getResponseMap().put(messageObject.getConnectorName(), response);
		}
	}
}
