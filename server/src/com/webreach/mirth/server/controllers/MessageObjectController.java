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

import java.io.File;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.UnsupportedDataTypeException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mule.umo.UMOEvent;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;
import com.webreach.mirth.model.Attachment;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.Response;
import com.webreach.mirth.model.MessageObject.Status;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import com.webreach.mirth.server.builders.ErrorMessageBuilder;
import com.webreach.mirth.server.util.DICOMUtil;
import com.webreach.mirth.server.util.DatabaseUtil;
import com.webreach.mirth.server.util.SqlConfig;
import com.webreach.mirth.server.util.UUIDGenerator;
import com.webreach.mirth.server.util.VMRouter;
import com.webreach.mirth.util.Encrypter;
import com.webreach.mirth.util.EncryptionException;
import com.webreach.mirth.util.PropertyLoader;

public class MessageObjectController {
	private static final String RECEIVE_SOCKET = "receiverSocket";
	private Logger logger = Logger.getLogger(this.getClass());
	private SqlMapClient sqlMap = SqlConfig.getSqlMapInstance();
	private ConfigurationController configurationController = ConfigurationController.getInstance();
	private ChannelStatisticsController statisticsController = ChannelStatisticsController.getInstance();
	private String lineSeperator = System.getProperty("line.separator");
	private ErrorMessageBuilder errorBuilder = new ErrorMessageBuilder();

	private static MessageObjectController instance = null;

	private MessageObjectController() {
		
	}
	
	public static MessageObjectController getInstance() {
		synchronized (MessageObjectController.class) {
			if (instance == null) {
				instance = new MessageObjectController();
			}
			
			return instance;
		}
	}

	public void initialize() {
		removeAllFilterTables();
	}

	public void removeAllFilterTables() {
		Connection conn = null;
		ResultSet resultSet = null;

		try {
			conn = sqlMap.getDataSource().getConnection();
			// Gets the database metadata
			DatabaseMetaData dbmd = conn.getMetaData();

			// Specify the type of object; in this case we want tables
			String[] types = { "TABLE" };
			String tablePattern = "MSG_TMP_%";
			resultSet = dbmd.getTables(null, null, tablePattern, types);
			
			boolean resultFound = resultSet.next();
			
			// Some databases only accept lowercase table names
			if (!resultFound) {
				resultSet = dbmd.getTables(null, null, tablePattern.toLowerCase(), types);
				resultFound = resultSet.next();
			}

			while (resultFound) {
				// Get the table name
				String tableName = resultSet.getString(3);
				// Get the uid and remove its filter tables/indexes/sequences
				removeFilterTable(tableName.substring(8));
				resultFound = resultSet.next();
			}

		} catch (SQLException e) {
			logger.error(e);
		} finally {
			DatabaseUtil.close(resultSet);
			DatabaseUtil.close(conn);
		}
	}

	public void updateMessage(MessageObject incomingMessageObject, boolean checkIfMessageExists) {
			MessageObject messageObject = (MessageObject) incomingMessageObject.clone();
			Socket socket = null;
			try{
				//Check if we have a socket. We need to replace with a string because
				//Sockets are not serializable and we want to retain the socket
				if (messageObject.getChannelMap().containsKey(RECEIVE_SOCKET)){
					Object socketObj = messageObject.getChannelMap().get(RECEIVE_SOCKET);
					if (socketObj instanceof Socket){
						socket = (Socket) socketObj;
						messageObject.getChannelMap().put(RECEIVE_SOCKET, socket.toString());
					}else{
						messageObject.getChannelMap().put(RECEIVE_SOCKET, socketObj.toString());
					}
					
				}
			}catch (Exception e){
				logger.error(e);
			}
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
						logger.debug("message is not stored");
						return;
					} else if (channel.getProperties().getProperty("encryptData").equals("true")) {
						try{
                            encryptMessageData(messageObject);
                        }
                        catch (EncryptionException e) {
                            logger.error("message logging halted. could not encrypt message. id=" + messageObject.getId(), e);
                        }
					}
				}
			}

            writeMessageToDatabase(messageObject, checkIfMessageExists);
            if (socket != null){
            	messageObject.getChannelMap().put(RECEIVE_SOCKET, socket);
            }
	}
    
    public void importMessage(MessageObject messageObject)
    {
        writeMessageToDatabase(messageObject, true);
    }
    
    private void writeMessageToDatabase(MessageObject messageObject, boolean checkIfMessageExists)
    {
        try {
            if (checkIfMessageExists) {
                int count = (Integer) sqlMap.queryForObject("getMessageCount", messageObject.getId());
    
                if (count == 0) {
                    logger.debug("adding message: id=" + messageObject.getId());
                    sqlMap.insert("insertMessage", messageObject);
    
                } else {
                    logger.debug("updating message: id=" + messageObject.getId());
                    sqlMap.update("updateMessage", messageObject);
                }
            }
            else {
                logger.debug("adding message (not checking for message): id=" + messageObject.getId());
                sqlMap.insert("insertMessage", messageObject);
            }
        }
        catch (SQLException e) {
            logger.error("could not log message: id=" + messageObject.getId(), e);
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

	public int createMessagesTempTable(MessageObjectFilter filter, String uid, boolean forceTemp) throws ControllerException {
		logger.debug("creating temporary message table: filter=" + filter.toString());

		if (!forceTemp && statementExists("getMessageByPageLimit")) {
			return -1;
		}
		// If it's not forcing temp tables (export or reprocessing),
		// then it's reusing the same ones, so remove them.
		if (!forceTemp) {
			removeFilterTable(uid);
		}

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

	public int removeMessages(MessageObjectFilter filter) throws ControllerException {
		logger.debug("removing messages: filter=" + filter.toString());

		try {
            removeMessageFromQueue(filter);
			int rowCount =  sqlMap.delete("deleteMessage", getFilterMap(filter, null));
            sqlMap.delete("deleteUnusedAttachments");
            return rowCount;
        } catch (Exception e) {
			throw new ControllerException(e);
		}
	}
	
	private void removeMessageFromQueue(MessageObjectFilter filter) throws Exception {
		File queuestoreDir = new File(ConfigurationController.getInstance().getQueuestorePath());
		String uid = System.currentTimeMillis() + "";
		filter.setStatus(Status.QUEUED);
		int size = createMessagesTempTable(filter, uid, true);
		int page = 0;
		int interval = 10;

		while ((page * interval) < size) {
			List<MessageObject> messages = getMessagesByPage(page, interval, size, uid);
			
			for (Iterator iterator = messages.iterator(); iterator.hasNext();) {
				MessageObject messageObject = (MessageObject) iterator.next();
				
				if (queuestoreDir.exists()) {
					String messageId = messageObject.getId();
					String channelId = messageObject.getChannelId();
					IOFileFilter fileFilter = new WildcardFileFilter(messageId + ".*");
					IOFileFilter dirFilter = new WildcardFileFilter(channelId + "*");
					Collection files = FileUtils.listFiles(queuestoreDir, fileFilter, dirFilter);
					
					for (Iterator fileIterator = files.iterator(); fileIterator.hasNext();) {
						File file = (File) fileIterator.next();
						FileUtils.forceDelete(file);
					}
				}
			}
			
			page++;
		}
		
		removeFilterTable(uid);
	}

	public void removeFilterTable(String uid) {
		logger.debug("Removing temporary message table: uid=" + uid);
		try {
			if (statementExists("dropTempMessageTableSequence")) {
				sqlMap.update("dropTempMessageTableSequence", uid);
			}
		} catch (SQLException e) {
			// supress any warnings about the sequence not existing
			logger.debug(e);
		}
		try {
			if (statementExists("deleteTempMessageTableIndex")) {
				sqlMap.update("deleteTempMessageTableIndex", uid);
			}
		} catch (SQLException e) {
			// supress any warnings about the index not existing
			logger.debug(e);
		}
		try {
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
            sqlMap.delete("deleteUnusedAttachments");
        } catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public void reprocessMessages(final MessageObjectFilter filter) throws ControllerException {
		try {
			final String uid = System.currentTimeMillis() + "";
			final int size = createMessagesTempTable(filter, uid, true);

			Thread reprocessThread = new Thread(new Runnable() {
				public void run() {
					try {
						int page = 0;
						int interval = 10;

						while ((page * interval) < size) {
							List<MessageObject> messages = getMessagesByPage(page, interval, size, uid);

							try {
								VMRouter router = new VMRouter();

								for (Iterator<MessageObject> iter = messages.iterator(); iter.hasNext();) {
									try {
										Thread.sleep(10);
									} catch (InterruptedException ie) {
										logger.debug(ie);
									}

									MessageObject message = iter.next();
                                    // get attachment for old message
                                    if(message.isAttachment()){
                                        String rawData = DICOMUtil.getDICOMRawData(message);
                                        message.setRawData(rawData);
                                    }
									router.routeMessageByChannelId(message.getChannelId(), message.getRawData(), true, true);
								}
							} catch (Exception e) {
								throw new ControllerException("could not reprocess message", e);
							}

							page++;
						}
					} catch (Exception e) {
						logger.error(e);

					} finally {
						// Remove any temp tables we created
						removeFilterTable(uid);
					}

				}
			});
			
			reprocessThread.start();
		} catch (ControllerException e) {
			throw new ControllerException(e);
		}
	}
    
    public void processMessage(MessageObject message) throws ControllerException
    {
        try {
            VMRouter router = new VMRouter();
            router.routeMessageByChannelId(message.getChannelId(), message.getRawData(), true, true);
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
		parameterMap.put("source", filter.getSource());
		parameterMap.put("searchCriteria", filter.getSearchCriteria());
		parameterMap.put("searchRawData", filter.isSearchRawData());
		parameterMap.put("searchTransformedData", filter.isSearchTransformedData());
		parameterMap.put("searchEncodedData", filter.isSearchEncodedData());

		if (filter.getStartDate() != null) {
			parameterMap.put("startDate", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", filter.getStartDate()));
		}

		if (filter.getEndDate() != null) {
			parameterMap.put("endDate", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", filter.getEndDate()));
		}

		return parameterMap;
	}

	public MessageObject cloneMessageObjectForBroadcast(MessageObject messageObject, String connectorName) {
		MessageObject clone = new MessageObject();
		// We could use deep copy here, but see the notes below
		clone.setId(UUIDGenerator.getUUID());
		clone.setServerId(configurationController.getServerId());
		clone.setDateCreated(Calendar.getInstance());
		clone.setCorrelationId(messageObject.getId());
		clone.setConnectorName(connectorName);
		clone.setRawData(messageObject.getEncodedData());
		clone.setResponseMap(messageObject.getResponseMap());
		clone.setChannelMap(messageObject.getChannelMap());
		clone.setChannelId(messageObject.getChannelId());
        clone.setAttachment(messageObject.isAttachment());
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
		// queued messages are stored into a persistence media, so their socket element should be removed
		if (messageObject.getChannelMap().containsKey(RECEIVE_SOCKET)){
			Object socketObj = messageObject.getChannelMap().get(RECEIVE_SOCKET);
			messageObject.getChannelMap().put(RECEIVE_SOCKET, socketObj.toString());						
		}
		
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

			if (oldStatus.equals(MessageObject.Status.QUEUED) && !newStatus.equals(MessageObject.Status.QUEUED)) {
				statisticsController.decrementQueuedCount(messageObject.getChannelId());
			}
            updateMessage(messageObject, oldStatus.equals(MessageObject.Status.QUEUED));
            
		}
	}
	public void resetQueuedStatus(MessageObject messageObject) {

		if (messageObject != null) {
			messageObject.setStatus(Status.QUEUED);
			updateMessage(messageObject, true);
			statisticsController.decrementErrorCount(messageObject.getChannelId());
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
    
	public Attachment getAttachment(String attachmentId) throws ControllerException {
		try {
			return (Attachment) sqlMap.queryForObject("getAttachment", attachmentId);
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}    
    
	public List<Attachment> getAttachmentsByMessageId(String messageId) throws ControllerException {
		try {
			return sqlMap.queryForList("getAttachmentsByMessageId", messageId);
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	} 
	public List<Attachment> getAttachmentIdsByMessageId(String messageId) throws ControllerException {
		try {
			return sqlMap.queryForList("getAttachmentIdsByMessageId", messageId);
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}     
    public void insertAttachment(Attachment attachment) {
        try {
            sqlMap.insert("insertAttachment", attachment);
        }
        catch (SQLException e) {
            logger.error("could not insert attachment: id=" + attachment.getAttachmentId(), e);
        }        
    }
    public void deleteAttachments(MessageObject message) {
        try {
            sqlMap.delete("deleteAttachments", message);
        }
        catch (SQLException e) {
            logger.error("could not delete attachment: message id=" + message.getId(), e);
        }           
    }
    public void deleteUnusedAttachments() {
        try {
            sqlMap.delete("deleteUnusedAttachments");
        }
        catch (SQLException e) {
            logger.error("problem deleting unused attachments", e);
        }           
    }    
    public Attachment createAttachment(Object data, String type) throws UnsupportedDataTypeException{
    	byte[] byteData;
    	if (data instanceof byte[]){
    		byteData = (byte[])data;
    	}else if (data instanceof String){
    		byteData = ((String)data).getBytes();
    	}else{
    		throw new UnsupportedDataTypeException("Attachment can be of type String or byte[]");
    	}
        Attachment attachment = new Attachment();
        attachment.setAttachmentId(UUIDGenerator.getUUID());
        attachment.setData(byteData);
        attachment.setSize(byteData.length);
        attachment.setType(type);
        return attachment;
    }
    public Attachment createAttachment(Object data, String type, MessageObject messageObject) throws UnsupportedDataTypeException{
    	Attachment attachment = createAttachment(data, type);
    	attachment.setMessageId(messageObject.getId());
    	return attachment;
    }
}

