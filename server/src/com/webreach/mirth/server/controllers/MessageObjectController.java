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
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.Response;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import com.webreach.mirth.server.builders.ErrorMessageBuilder;
import com.webreach.mirth.server.util.SqlConfig;
import com.webreach.mirth.server.util.UUIDGenerator;
import com.webreach.mirth.server.util.VMRouter;
import com.webreach.mirth.util.Encrypter;
import com.webreach.mirth.util.EncryptionException;

public class MessageObjectController {
	private Logger logger = Logger.getLogger(this.getClass());
	private SqlMapClient sqlMap = SqlConfig.getSqlMapInstance();
	private static final String MESSAGE_NO_DATA_STORE = "No data stored for this message.";
	private ConfigurationController configurationController = new ConfigurationController();
	private ChannelStatisticsController statisticsController = new ChannelStatisticsController();
	private String lineSeperator = System.getProperty("line.separator");
	private ErrorMessageBuilder errorBuilder = new ErrorMessageBuilder();

	public void updateMessage(MessageObject incomingMessageObject, boolean checkIfMessageExists) {
		try {
			MessageObject messageObject = (MessageObject) incomingMessageObject.clone();
			
			// update the stats counts
			if (messageObject.getStatus().equals(MessageObject.Status.TRANSFORMED)) {
				statisticsController.incrementReceivedCount(messageObject.getChannelId());
			} else if (messageObject.getStatus().equals(MessageObject.Status.FILTERED)) {
				statisticsController.incrementFilteredCount(messageObject.getChannelId());
			} else if (messageObject.getStatus().equals(MessageObject.Status.ERROR)) {
				statisticsController.incrementErrorCount(messageObject.getChannelId());
			} else if (messageObject.getStatus().equals(MessageObject.Status.SENT)) {
				statisticsController.incrementSentCount(messageObject.getChannelId());
			} else if (messageObject.getStatus().equals(MessageObject.Status.QUEUED)) {
				statisticsController.incrementQueuedCount(messageObject.getChannelId());
			}
			
			String channelId = messageObject.getChannelId();
			HashMap<String, Channel> channelCache = ChannelController.getChannelCache();

			// Check the cache for the channel
			if (channelCache != null && channelCache.containsKey(channelId)) {
				Channel channel = channelCache.get(channelId);

				if (channel.getProperties().containsKey("store_messages")) {
					if (channel.getProperties().get("store_messages").equals("false") || (channel.getProperties().get("store_messages").equals("true") && channel.getProperties().get("error_messages_only").equals("true") && !messageObject.getStatus().equals(MessageObject.Status.ERROR)) || (channel.getProperties().get("store_messages").equals("true") && channel.getProperties().get("dont_store_filtered").equals("true") && messageObject.getStatus().equals(MessageObject.Status.FILTERED))) {
						// If we don't want to store messages, then lets
						// sanitize the data in a clone
						messageObject.setRawData(MESSAGE_NO_DATA_STORE);
						messageObject.setEncodedData(MESSAGE_NO_DATA_STORE);
						messageObject.setTransformedData(MESSAGE_NO_DATA_STORE);
						messageObject.setConnectorMap(new HashMap());
						messageObject.setChannelMap(new HashMap());
						messageObject.setResponseMap(new HashMap());
					} else if (channel.getProperties().getProperty("encryptData").equals("true")) {
						encryptMessageData(messageObject);
					}
				}
			}

			if (checkIfMessageExists) {
				int count = (Integer) sqlMap.queryForObject("getMessageCount", messageObject.getId());

				if (count == 0) {
					logger.debug("adding message: id=" + messageObject.getId());
					sqlMap.insert("insertMessage", messageObject);
				} else {
					logger.debug("updating message: id=" + messageObject.getId());
					sqlMap.update("updateMessage", messageObject);
				}
			} else {
				logger.debug("adding message (not checking for message): id=" + messageObject.getId());
				sqlMap.insert("insertMessage", messageObject);
			}
		} catch (Exception e) {
			logger.error("could not log message: id=" + incomingMessageObject.getId(), e);
		}
	}

	private void encryptMessageData(MessageObject messageObject) throws EncryptionException {
		Encrypter encrypter = new Encrypter(configurationController.getEncryptionKey());

		if (messageObject.getRawData() != null) {
			String encryptedRawData = encrypter.encrypt(messageObject.getRawData());
			messageObject.setRawData(encryptedRawData);
		}

		if (messageObject.getTransformedData() != null) {
			String encryptedTransformedData = encrypter.encrypt(messageObject.getTransformedData());
			messageObject.setTransformedData(encryptedTransformedData);
		}

		if (messageObject.getEncodedData() != null) {
			String encryptedEncodedData = encrypter.encrypt(messageObject.getEncodedData());
			messageObject.setEncodedData(encryptedEncodedData);
		}

		messageObject.setEncrypted(true);
	}

	private void decryptMessageData(MessageObject messageObject) throws EncryptionException {
		if (messageObject.isEncrypted()) {
			Encrypter encrypter = new Encrypter(configurationController.getEncryptionKey());

			if (messageObject.getRawData() != null) {
				String decryptedRawData = encrypter.decrypt(messageObject.getRawData());
				messageObject.setRawData(decryptedRawData);
			}

			if (messageObject.getTransformedData() != null) {
				String decryptedTransformedData = encrypter.decrypt(messageObject.getTransformedData());
				messageObject.setTransformedData(decryptedTransformedData);
			}

			if (messageObject.getEncodedData() != null) {
				String decryptedEncodedData = encrypter.decrypt(messageObject.getEncodedData());
				messageObject.setEncodedData(decryptedEncodedData);
			}
		}
	}

	public int createMessagesTempTable(MessageObjectFilter filter, String uid, boolean overrideLimit) throws ControllerException {
		logger.debug("creating temporary message table: filter=" + filter.toString());
		
		if (!overrideLimit && statementExists("getMessageByPageLimit")){
			return -1;
		}
		removeFilterTables(uid);

		try {
			if (statementExists("createTempMessageTableSequence")) {
				sqlMap.update("createTempMessageTableSequence", uid);
			}
				
			sqlMap.update("createTempMessageTable", uid);
			sqlMap.update("createTempMessageTableIndex", uid);
			return sqlMap.update("populateTempMessageTable", getFilterMap(filter, uid));
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}
	// ast: allow ordering with derby
	public List<MessageObject> getMessagesByPageLimit(int page, int pageSize, int maxMessages, String uid, MessageObjectFilter filter) throws ControllerException {
		logger.debug("retrieving messages by page: page=" + page);
		
		try {
			Map parameterMap = new HashMap();
			parameterMap.put("uid", uid);
			int offset = page * pageSize;
			
			parameterMap.put("offset", offset);
			parameterMap.put("limit", pageSize);
			
			parameterMap.putAll(getFilterMap(filter, uid));
			
			List<MessageObject> messages = sqlMap.queryForList("getMessageByPageLimit", parameterMap);

			for (Iterator iter = messages.iterator(); iter.hasNext();) {
				MessageObject messageObject = (MessageObject) iter.next();
				decryptMessageData(messageObject);
			}

			return messages;
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}
	// ast: allow ordering with derby
	public List<MessageObject> getMessagesByPage(int page, int pageSize, int maxMessages, String uid) throws ControllerException {
		logger.debug("retrieving messages by page: page=" + page);
		
		try {
			Map parameterMap = new HashMap();
			parameterMap.put("uid", uid);

			if ((page != -1) && (pageSize != -1)) {
				int last = maxMessages - (page * pageSize);
				int first = last - pageSize + 1;
				parameterMap.put("first", first);
				parameterMap.put("last", last);
			}

			List<MessageObject> messages = sqlMap.queryForList("getMessageByPage", parameterMap);

			for (Iterator iter = messages.iterator(); iter.hasNext();) {
				MessageObject messageObject = (MessageObject) iter.next();
				decryptMessageData(messageObject);
			}

			return messages;
		} catch (Exception e) {
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

	public void removeFilterTables(String uid) {
		try {
			if (statementExists("dropTempMessageTableSequence")) {
				sqlMap.update("dropTempMessageTableSequence", uid);
			}
			if (statementExists("deleteTempMessageTableIndex")) {
				sqlMap.update("deleteTempMessageTableIndex", uid);
			}
			sqlMap.update("dropTempMessageTable", uid);
		} catch (SQLException e) {
			// supress any warnings about the table not existing
			logger.debug(e);
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

	public void reprocessMessages(final MessageObjectFilter filter) throws ControllerException {
		Thread reprocessThread = new Thread(new Runnable() {
			public void run() {
				//Create a unique id, however get rid of the dashes
				String uid = UUIDGenerator.getUUID().replaceAll("-", "");
				try {
					int size = createMessagesTempTable(filter, uid, true);
					int page = 0;
					int interval = 10;

					while ((page * interval) < size) {

						List<MessageObject> messages = getMessagesByPage(page, interval, size, uid);

						try {
							VMRouter router = new VMRouter();

							for (Iterator<MessageObject> iter = messages.iterator(); iter.hasNext();) {
								try {
									Thread.sleep(100);
								} catch (InterruptedException ie) {
									logger.debug(ie);
								}
								
								MessageObject message = iter.next();
								router.routeMessageByChannelId(message.getChannelId(), message.getRawData(), true);
							}
						} catch (Exception e) {
							throw new ControllerException("could not reprocess message", e);
						}

						page++;
					}
				} catch (Exception e) {
					logger.error(e);
					
				}finally{
					//Remove any temp tables we created
					removeFilterTables(uid);
				}

			}
		});
		reprocessThread.start();
		return;
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
		parameterMap.put("source", filter.getSource());

		if (filter.getStartDate() != null) {
			parameterMap.put("startDate", String.format("%1$tY-%1$tm-%1$td 00:00:00", filter.getStartDate()));
		}

		if (filter.getEndDate() != null) {
			parameterMap.put("endDate", String.format("%1$tY-%1$tm-%1$td 23:59:59", filter.getEndDate()));
		}

		return parameterMap;
	}

	public MessageObject cloneMessageObjectForBroadcast(MessageObject messageObject, String connectorName) {
		MessageObject clone = new MessageObject();
		// We could use deep copy here, but see the notes below
		clone.setId(UUIDGenerator.getUUID());
		clone.setDateCreated(Calendar.getInstance());
		clone.setCorrelationId(messageObject.getId());
		clone.setConnectorName(connectorName);
		clone.setRawData(messageObject.getEncodedData());
		clone.setResponseMap(messageObject.getResponseMap());
		clone.setChannelMap(messageObject.getChannelMap());
		clone.setChannelId(messageObject.getChannelId());
		return clone;
	}

	public MessageObject getMessageObjectFromEvent(UMOEvent event) throws Exception {
		MessageObject messageObject = null;
		Object incomingData = event.getTransformedMessage();

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

	private void setStatus(MessageObject messageObject, MessageObject.Status newStatus, Response.Status responseStatus, String responseMessage) {
		if (messageObject.getResponseMap() != null) {
			Response response = new Response(responseStatus, responseMessage);
			messageObject.getResponseMap().put(messageObject.getConnectorName(), response);
		}

		if (messageObject != null) {
			MessageObject.Status oldStatus = messageObject.getStatus();
			messageObject.setStatus(newStatus);

			if (oldStatus.equals(MessageObject.Status.QUEUED) && newStatus.equals(MessageObject.Status.SENT)) {
				
				statisticsController.decrementQueuedCount(messageObject.getChannelId());	
			}

			updateMessage(messageObject, oldStatus.equals(MessageObject.Status.QUEUED));
		}
	}

	private boolean statementExists(String statement) {
		try {
			SqlMapExecutorDelegate delegate = ((ExtendedSqlMapClient) sqlMap).getDelegate();
			delegate.getMappedStatement(statement);
		} catch (SqlMapException sme) {
			// The statement does not exist
			return false;
		}
		
		return true;
	}
}
