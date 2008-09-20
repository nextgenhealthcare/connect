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

import java.net.Socket;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.UnsupportedDataTypeException;

import org.apache.log4j.Logger;
import org.mule.umo.UMOEvent;

import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;
import com.webreach.mirth.model.Attachment;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.Response;
import com.webreach.mirth.model.MessageObject.Status;
import com.webreach.mirth.model.converters.ObjectCloner;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import com.webreach.mirth.server.builders.ErrorMessageBuilder;
import com.webreach.mirth.server.util.DICOMUtil;
import com.webreach.mirth.server.util.DatabaseUtil;
import com.webreach.mirth.server.util.SqlConfig;
import com.webreach.mirth.server.util.UUIDGenerator;
import com.webreach.mirth.server.util.VMRouter;
import com.webreach.mirth.util.Encrypter;
import com.webreach.mirth.util.EncryptionException;
import com.webreach.mirth.util.QueueUtil;

public class DefaultMessageObjectController implements MessageObjectController {
    private static final String RECEIVE_SOCKET = "receiverSocket";
    private Logger logger = Logger.getLogger(this.getClass());
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private ChannelStatisticsController statisticsController = ControllerFactory.getFactory().createChannelStatisticsController();
    private String lineSeperator = System.getProperty("line.separator");
    private ErrorMessageBuilder errorBuilder = new ErrorMessageBuilder();

    private static DefaultMessageObjectController instance = null;

    private DefaultMessageObjectController() {

    }

    public static MessageObjectController getInstance() {
        synchronized (DefaultMessageObjectController.class) {
            if (instance == null) {
                instance = new DefaultMessageObjectController();
            }

            return instance;
        }
    }

    public void removeAllFilterTables() {
        Connection conn = null;
        ResultSet resultSet = null;

        try {
            conn = SqlConfig.getSqlMapClient().getDataSource().getConnection();
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
        try {
            // Check if we have a socket. We need to replace with a string
            // because
            // Sockets are not serializable and we want to retain the socket
            if (messageObject.getChannelMap().containsKey(RECEIVE_SOCKET)) {
                Object socketObj = messageObject.getChannelMap().get(RECEIVE_SOCKET);
                if (socketObj instanceof Socket) {
                    socket = (Socket) socketObj;
                    messageObject.getChannelMap().put(RECEIVE_SOCKET, socket.toString());
                } else {
                    messageObject.getChannelMap().put(RECEIVE_SOCKET, socketObj.toString());
                }

            }
        } catch (Exception e) {
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
        HashMap<String, Channel> channelCache = ControllerFactory.getFactory().createChannelController().getChannelCache();

        // Check the cache for the channel
        if (channelCache != null && channelCache.containsKey(channelId)) {
            Channel channel = channelCache.get(channelId);

            if (channel.getProperties().containsKey("store_messages")) {
                if (channel.getProperties().get("store_messages").equals("false") || (channel.getProperties().get("store_messages").equals("true") && channel.getProperties().get("error_messages_only").equals("true") && !messageObject.getStatus().equals(MessageObject.Status.ERROR)) || (channel.getProperties().get("store_messages").equals("true") && channel.getProperties().get("dont_store_filtered").equals("true") && messageObject.getStatus().equals(MessageObject.Status.FILTERED))) {
                    logger.debug("message is not stored");
                    return;
                } else if (channel.getProperties().getProperty("encryptData").equals("true")) {
                    try {
                        encryptMessageData(messageObject);
                    } catch (EncryptionException e) {
                        logger.error("message logging halted. could not encrypt message. id=" + messageObject.getId(), e);
                    }
                }
            }
        }

        writeMessageToDatabase(messageObject, checkIfMessageExists);

        if (socket != null) {
            messageObject.getChannelMap().put(RECEIVE_SOCKET, socket);
        }
    }

    public void importMessage(MessageObject messageObject) {
        writeMessageToDatabase(messageObject, true);
    }

    private void writeMessageToDatabase(MessageObject messageObject, boolean checkIfMessageExists) {
        try {
            if (checkIfMessageExists) {
                int count = (Integer) SqlConfig.getSqlMapClient().queryForObject("getMessageCount", messageObject.getId());

                if (count == 0) {
                    logger.debug("adding message: id=" + messageObject.getId());
                    SqlConfig.getSqlMapClient().insert("insertMessage", messageObject);
                } else {
                    logger.debug("updating message: id=" + messageObject.getId());
                    SqlConfig.getSqlMapClient().update("updateMessage", messageObject);
                }
            } else {
                logger.debug("adding message (not checking for message): id=" + messageObject.getId());
                SqlConfig.getSqlMapClient().insert("insertMessage", messageObject);
            }
        } catch (SQLException e) {
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
                SqlConfig.getSqlMapClient().update("createTempMessageTableSequence", uid);
            }

            SqlConfig.getSqlMapClient().update("createTempMessageTable", uid);
            SqlConfig.getSqlMapClient().update("createTempMessageTableIndex", uid);
            return SqlConfig.getSqlMapClient().update("populateTempMessageTable", getFilterMap(filter, uid));
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

            List<MessageObject> messages = SqlConfig.getSqlMapClient().queryForList("getMessageByPageLimit", parameterMap);

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
    public List<MessageObject> getMessagesByPage(int page, int pageSize, int maxMessages, String uid, boolean descending) throws ControllerException {
        logger.debug("retrieving messages by page: page=" + page);

        try {
            Map parameterMap = new HashMap();
            parameterMap.put("uid", uid);
            
            // Use descending for most queries, use ascending for
            // reprocessing messages in the correct order.
            if (descending) {
            	parameterMap.put("order", "DESC");
            } else {
            	parameterMap.put("order", "ASC");
            }

            if ((page != -1) && (pageSize != -1)) {
                int last = maxMessages - (page * pageSize);
                int first = last - pageSize + 1;
                parameterMap.put("first", first);
                parameterMap.put("last", last);
            }

            List<MessageObject> messages = SqlConfig.getSqlMapClient().queryForList("getMessageByPage", parameterMap);

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
            removeMessagesFromQueue(filter);
            int rowCount = SqlConfig.getSqlMapClient().delete("deleteMessage", getFilterMap(filter, null));
            SqlConfig.getSqlMapClient().delete("deleteUnusedAttachments");
            return rowCount;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    private void removeMessagesFromQueue(MessageObjectFilter filter) throws Exception {
        String uid = System.currentTimeMillis() + "";
        // clone the filter so that we don't modify the original
        MessageObjectFilter queueFilter = (MessageObjectFilter) ObjectCloner.deepCopy(filter);
        queueFilter.setStatus(Status.QUEUED);
        int size = createMessagesTempTable(queueFilter, uid, true);
        int page = 0;
        int interval = 10;

        while ((page * interval) < size) {
            for (MessageObject message : getMessagesByPage(page, interval, size, uid, true)) {
                String connectorId = ControllerFactory.getFactory().createChannelController().getConnectorId(message.getChannelId(), message.getConnectorName());
                String queueName = QueueUtil.getInstance().getQueueName(message.getChannelId(), connectorId);
                QueueUtil.getInstance().removeMessageFromQueue(queueName, message.getId());
                ControllerFactory.getFactory().createChannelStatisticsController().decrementQueuedCount(message.getChannelId());
            }

            page++;
        }

        removeFilterTable(uid);
    }

    public void removeFilterTable(String uid) {
        logger.debug("Removing temporary message table: uid=" + uid);

        try {
            if (statementExists("dropTempMessageTableSequence")) {
                SqlConfig.getSqlMapClient().update("dropTempMessageTableSequence", uid);
            }
        } catch (SQLException e) {
            // supress any warnings about the sequence not existing
            logger.debug(e);
        }

        try {
            if (statementExists("deleteTempMessageTableIndex")) {
                SqlConfig.getSqlMapClient().update("deleteTempMessageTableIndex", uid);
            }
        } catch (SQLException e) {
            // supress any warnings about the index not existing
            logger.debug(e);
        }

        try {
            SqlConfig.getSqlMapClient().update("dropTempMessageTable", uid);
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
            SqlConfig.getSqlMapClient().delete("deleteMessage", parameterMap);
            SqlConfig.getSqlMapClient().delete("deleteUnusedAttachments");

            Channel filterChannel = new Channel();
            filterChannel.setId(channelId);
            Channel channel = ControllerFactory.getFactory().createChannelController().getChannel(filterChannel).get(0);
            QueueUtil.getInstance().removeAllQueuesForChannel(channel);
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public void reprocessMessages(final MessageObjectFilter filter, final boolean replace, final List<String> destinations) throws ControllerException {
        try {
            // since get message by page expects a session, we'll make up a
            // session ID using a the current system time
            final String sessionId = String.valueOf(System.currentTimeMillis());
            final int size = createMessagesTempTable(filter, sessionId, true);

            Thread reprocessThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        int page = 0;
                        int interval = 10;
                        VMRouter router = new VMRouter();

                        while ((page * interval) < size) {
                            List<MessageObject> messages = getMessagesByPage(page, interval, size, sessionId, false);

                            try {
                                for (MessageObject message : messages) {
                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException ie) {
                                        logger.debug(ie);
                                    }

                                    // get attachment for old message
                                    if (message.isAttachment()) {
                                        String rawData = DICOMUtil.getDICOMRawData(message);
                                        message.setRawData(rawData);
                                    }

                                    if (replace) {
                                        message.getContext().put("replace", "true");
                                        message.getContext().put("messageId", message.getId());
                                    }

                                    message.getContext().put("destinations", destinations);
                                    router.routeMessageByChannelId(message.getChannelId(), message, true, true);
                                }
                            } catch (Exception e) {
                                throw new ControllerException("could not reprocess message", e);
                            }

                            page++;
                        }
                    } catch (Exception e) {
                        logger.error(e);
                    } finally {
                        // remove any temp tables we created
                        removeFilterTable(sessionId);
                    }
                }
            });

            reprocessThread.start();
        } catch (ControllerException e) {
            throw new ControllerException(e);
        }
    }

    public void processMessage(MessageObject message) throws ControllerException {
        try {
            VMRouter router = new VMRouter();
            router.routeMessageByChannelId(message.getChannelId(), message, true, true);
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
        parameterMap.put("quickSearch", filter.getQuickSearch());

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

        // look up ID based on connector name + correlation ID
        String existingId = null;

        if ((messageObject.getContext().get("replace") != null) && messageObject.getContext().get("replace").equals("true")) {
            clone.getContext().put("replace", "true");

            try {
                existingId = lookupMessageId(messageObject.getId(), connectorName);
            } catch (Exception e) {
                logger.error("Could not locate existing message", e);
            }
        }

        if (existingId != null) {
            clone.setId(existingId);
        } else {
            clone.setId(UUIDGenerator.getUUID());
        }

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

    private String lookupMessageId(String correlationId, String destinationId) throws SQLException {
        Map params = new HashMap();
        params.put("correlationId", correlationId);
        params.put("connectorName", destinationId);
        return (String) SqlConfig.getSqlMapClient().queryForObject("lookupMessageId", params);
    }

    private String lookupMessageStatus(String messageId) throws SQLException {
        return (String) SqlConfig.getSqlMapClient().queryForObject("lookupMessageStatus", messageId);
    }

    public MessageObject getMessageObjectFromEvent(UMOEvent event) throws Exception {
        MessageObject messageObject = null;
        Object incomingData = event.getTransformedMessage();

        if ((incomingData == null) || !(incomingData instanceof MessageObject)) {
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

    public void setTransformed(MessageObject messageObject) {
        setStatus(messageObject, MessageObject.Status.TRANSFORMED, Response.Status.SUCCESS, new String());
    }

    public void setQueued(MessageObject messageObject, String responseMessage) {
        // queued messages are stored into a persistence media, so their socket
        // element should be removed
        if (messageObject.getChannelMap().containsKey(RECEIVE_SOCKET)) {
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
            MessageObject.Status oldStatus = MessageObject.Status.valueOf(messageObject.getStatus().toString());
            messageObject.setStatus(newStatus);

            if (oldStatus.equals(MessageObject.Status.QUEUED) && !newStatus.equals(MessageObject.Status.QUEUED)) {
                statisticsController.decrementQueuedCount(messageObject.getChannelId());
            }

            boolean replace = oldStatus.equals(MessageObject.Status.QUEUED) || ((messageObject.getContext().get("replace") != null) && messageObject.getContext().get("replace").equals("true"));

            if (replace) {
                if ((messageObject.getContext().get("replace") != null) && messageObject.getContext().get("replace").equals("true")) {
                    try {
                        String existingId = null;

                        if (!messageObject.getConnectorName().toLowerCase().equals("source")) {
                            existingId = lookupMessageId(messageObject.getCorrelationId(), messageObject.getConnectorName());
                        } else {
                            existingId = messageObject.getId();
                        }

                        if (existingId != null) {
                            oldStatus = MessageObject.Status.valueOf(lookupMessageStatus(existingId));
                        }
                    } catch (Exception e) {
                        logger.error("Could not locate existing message", e);
                    }
                }

                if (oldStatus.equals(MessageObject.Status.ERROR)) {
                    statisticsController.decrementErrorCount(messageObject.getChannelId());
                } else if (oldStatus.equals(MessageObject.Status.FILTERED)) {
                    statisticsController.decrementFilteredCount(messageObject.getChannelId());
                } else if (oldStatus.equals(MessageObject.Status.TRANSFORMED)) {
                    statisticsController.decrementReceivedCount(messageObject.getChannelId());
                } else if (oldStatus.equals(MessageObject.Status.SENT)) {
                    statisticsController.decrementSentCount(messageObject.getChannelId());
                }
            }

            updateMessage(messageObject, replace);
        }
    }

    public void resetQueuedStatus(MessageObject messageObject) {

        if (messageObject != null) {
        	Map<String, Response> responseMap = messageObject.getResponseMap();
        	if (responseMap != null && responseMap.get(messageObject.getConnectorName()) != null) {
                Response response = (Response) responseMap.get(messageObject.getConnectorName());
                response.setStatus(Response.Status.QUEUED);
            }
        	
            messageObject.setStatus(Status.QUEUED);
            updateMessage(messageObject, true);
            statisticsController.decrementErrorCount(messageObject.getChannelId());
        }
    }

    private boolean statementExists(String statement) {
        try {
            SqlMapExecutorDelegate delegate = ((ExtendedSqlMapClient) SqlConfig.getSqlMapClient()).getDelegate();
            delegate.getMappedStatement(statement);
        } catch (SqlMapException sme) {
            // The statement does not exist
            return false;
        }

        return true;
    }

    public Attachment getAttachment(String attachmentId) throws ControllerException {
        try {
            return (Attachment) SqlConfig.getSqlMapClient().queryForObject("getAttachment", attachmentId);
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public List<Attachment> getAttachmentsByMessageId(String messageId) throws ControllerException {
        try {
            return SqlConfig.getSqlMapClient().queryForList("getAttachmentsByMessageId", messageId);
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public List<Attachment> getAttachmentIdsByMessageId(String messageId) throws ControllerException {
        try {
            return SqlConfig.getSqlMapClient().queryForList("getAttachmentIdsByMessageId", messageId);
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public void insertAttachment(Attachment attachment) {
        try {
            SqlConfig.getSqlMapClient().insert("insertAttachment", attachment);
        } catch (SQLException e) {
            logger.error("could not insert attachment: id=" + attachment.getAttachmentId(), e);
        }
    }

    public void deleteAttachments(MessageObject message) {
        try {
            SqlConfig.getSqlMapClient().delete("deleteAttachments", message);
        } catch (SQLException e) {
            logger.error("could not delete attachment: message id=" + message.getId(), e);
        }
    }

    public void deleteUnusedAttachments() {
        try {
            SqlConfig.getSqlMapClient().delete("deleteUnusedAttachments");
        } catch (SQLException e) {
            logger.error("problem deleting unused attachments", e);
        }
    }

    public Attachment createAttachment(Object data, String type) throws UnsupportedDataTypeException {
        byte[] byteData;

        if (data instanceof byte[]) {
            byteData = (byte[]) data;
        } else if (data instanceof String) {
            byteData = ((String) data).getBytes();
        } else {
            throw new UnsupportedDataTypeException("Attachment can be of type String or byte[]");
        }

        Attachment attachment = new Attachment();
        attachment.setAttachmentId(UUIDGenerator.getUUID());
        attachment.setData(byteData);
        attachment.setSize(byteData.length);
        attachment.setType(type);
        return attachment;
    }

    public Attachment createAttachment(Object data, String type, MessageObject messageObject) throws UnsupportedDataTypeException {
        Attachment attachment = createAttachment(data, type);
        attachment.setMessageId(messageObject.getId());
        return attachment;
    }
}
