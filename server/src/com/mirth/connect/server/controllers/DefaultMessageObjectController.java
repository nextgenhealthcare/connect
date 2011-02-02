/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.net.Socket;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.UnsupportedDataTypeException;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mule.umo.UMOEvent;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapSession;
import com.mirth.connect.model.Attachment;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.MessageObject.Status;
import com.mirth.connect.model.Response;
import com.mirth.connect.model.filters.MessageObjectFilter;
import com.mirth.connect.server.builders.ErrorMessageBuilder;
import com.mirth.connect.server.util.AttachmentUtil;
import com.mirth.connect.server.util.DICOMUtil;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.server.util.UUIDGenerator;
import com.mirth.connect.server.util.VMRouter;
import com.mirth.connect.util.Encrypter;
import com.mirth.connect.util.EncryptionException;
import com.mirth.connect.util.QueueUtil;

public class DefaultMessageObjectController extends MessageObjectController {
    private static final String RECEIVE_SOCKET = "receiverSocket";
    private Logger logger = Logger.getLogger(this.getClass());
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private ChannelStatisticsController statisticsController = ControllerFactory.getFactory().createChannelStatisticsController();
    private ErrorMessageBuilder errorBuilder = new ErrorMessageBuilder();

    private static DefaultMessageObjectController instance = null;

    private DefaultMessageObjectController() {

    }

    public static MessageObjectController create() {
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
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(conn);
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

                // XXX: Aren't these two cases doing the exact same thing??
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

        Channel channel = ControllerFactory.getFactory().createChannelController().getDeployedChannelById(messageObject.getChannelId());

        if (channel != null && channel.getProperties().containsKey("store_messages")) {
            // If replacing messages that were stored on special conditions
            // ("store errored only" or "do not store filtered messages"),
            // then try to remove the old message if the new one succeeded.
            if (checkIfMessageExists) {
                if (channel.getProperties().get("store_messages").equals("true") && channel.getProperties().get("error_messages_only").equals("true") && !messageObject.getStatus().equals(MessageObject.Status.ERROR)) {
                    try {
                        removeMessage(messageObject);
                    } catch (ControllerException e) {
                        logger.error("Could not remove old message: id=" + messageObject.getId(), e);
                    }
                }

                if (channel.getProperties().get("store_messages").equals("true") && channel.getProperties().get("dont_store_filtered").equals("true") && messageObject.getStatus().equals(MessageObject.Status.FILTERED)) {
                    try {
                        removeMessage(messageObject);
                    } catch (ControllerException e) {
                        logger.error("Could not remove old message: id=" + messageObject.getId(), e);
                    }
                }
            }

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

        writeMessageToDatabase(messageObject, checkIfMessageExists);

        if (socket != null) {
            messageObject.getChannelMap().put(RECEIVE_SOCKET, socket);
        }
    }

    public void updateMessageStatus(String channelId, String messageId, MessageObject.Status newStatus) {
        // update the stats counts
        if (newStatus.equals(MessageObject.Status.TRANSFORMED)) {
            statisticsController.incrementReceivedCount(channelId);
        } else if (newStatus.equals(MessageObject.Status.FILTERED)) {
            statisticsController.incrementFilteredCount(channelId);
        } else if (newStatus.equals(MessageObject.Status.ERROR)) {
            statisticsController.incrementErrorCount(channelId);
        } else if (newStatus.equals(MessageObject.Status.SENT)) {
            statisticsController.incrementSentCount(channelId);
        } else if (newStatus.equals(MessageObject.Status.QUEUED)) {
            statisticsController.incrementQueuedCount(channelId);
        }
        Channel channel = ControllerFactory.getFactory().createChannelController().getDeployedChannelById(channelId);
        if (channel == null) {
            logger.warn("Cannot update message " + messageId + " status as the channel " + channelId + " doesn't exists ");
            return;
        }
        if (channel.getProperties().containsKey("store_messages")) {
            if (channel.getProperties().get("store_messages").equals("false") || (channel.getProperties().get("store_messages").equals("true") && channel.getProperties().get("error_messages_only").equals("true") && (newStatus == MessageObject.Status.ERROR)) || (channel.getProperties().get("store_messages").equals("true") && channel.getProperties().get("dont_store_filtered").equals("true") && (newStatus == MessageObject.Status.FILTERED))) {
                logger.debug("message " + messageId + " status is not stored because channel store configuration parameters");
                return;
            }
        }

        try {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("status", newStatus.toString());
            params.put("id", messageId);
            SqlConfig.getSqlMapClient().update("Message.updateMessageStatus", params);
        } catch (SQLException e) {
            logger.error("Error updating message " + messageId + " status due to a database problem", e);
        }
    }

    public void importMessage(MessageObject messageObject) {
        writeMessageToDatabase(messageObject, true);
    }

    private void writeMessageToDatabase(MessageObject messageObject, boolean checkIfMessageExists) {
        try {
            if (checkIfMessageExists) {
                int count = (Integer) SqlConfig.getSqlMapClient().queryForObject("Message.getMessageCount", messageObject.getId());

                if (count == 0) {
                    logger.debug("adding message: id=" + messageObject.getId());
                    SqlConfig.getSqlMapClient().insert("Message.insertMessage", messageObject);
                } else {
                    logger.debug("updating message: id=" + messageObject.getId());
                    SqlConfig.getSqlMapClient().update("Message.updateMessage", messageObject);
                }
            } else {
                logger.debug("adding message (not checking for message): id=" + messageObject.getId());
                SqlConfig.getSqlMapClient().insert("Message.insertMessage", messageObject);
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

        if (!forceTemp && DatabaseUtil.statementExists("Message.getMessageByPageLimit")) {
            return -1;
        }
        // If it's not forcing temp tables (export or reprocessing),
        // then it's reusing the same ones, so remove them.
        if (!forceTemp) {
            removeFilterTable(uid);
        }

        try {
            if (DatabaseUtil.statementExists("Message.createTempMessageTableSequence")) {
                SqlConfig.getSqlMapClient().update("Message.createTempMessageTableSequence", uid);
            }

            SqlConfig.getSqlMapClient().update("Message.createTempMessageTable", uid);
            SqlConfig.getSqlMapClient().update("Message.createTempMessageTableIndex", uid);
            return SqlConfig.getSqlMapClient().update("Message.populateTempMessageTable", getFilterMap(filter, uid));
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    // ast: allow ordering with derby
    public List<MessageObject> getMessagesByPageLimit(int page, int pageSize, int maxMessages, String uid, MessageObjectFilter filter) throws ControllerException {
        logger.debug("retrieving messages by page: page=" + page);

        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("uid", uid);
            int offset = page * pageSize;

            parameterMap.put("offset", offset);
            parameterMap.put("limit", pageSize);
            parameterMap.put("offpluslim", offset + pageSize);

            parameterMap.putAll(getFilterMap(filter, uid));

            List<MessageObject> messages = SqlConfig.getSqlMapClient().queryForList("Message.getMessageByPageLimit", parameterMap);

            for (MessageObject messageObject : messages) {
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

        int last = maxMessages;
        int first = 1;

        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("uid", uid);

            // Use descending for most queries, use ascending for
            // reprocessing messages in the correct order.
            if (descending) {
                parameterMap.put("order", "DESC");
                last = maxMessages - (page * pageSize);
                first = last - pageSize + 1;
            } else {
                parameterMap.put("order", "ASC");
                first = (page * pageSize) + 1;
                last = (page + 1) * pageSize;
            }

            if ((page != -1) && (pageSize != -1)) {
                parameterMap.put("first", first);
                parameterMap.put("last", last);
            }

            List<MessageObject> messages = SqlConfig.getSqlMapClient().queryForList("Message.getMessageByPage", parameterMap);

            for (MessageObject messageObject : messages) {
                decryptMessageData(messageObject);
            }

            return messages;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public void removeMessage(MessageObject messageObject) throws ControllerException {
        logger.debug("removing message: id=" + messageObject.getId());

        try {
            removeMessageFromQueue(messageObject);
            MessageObjectFilter filter = new MessageObjectFilter();
            filter.setId(messageObject.getId());
            SqlConfig.getSqlMapClient().delete("Message.deleteMessage", getFilterMap(filter, null));
            SqlConfig.getSqlMapClient().delete("Message.deleteUnusedAttachments");
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public int removeMessages(MessageObjectFilter filter) throws ControllerException {
        logger.debug("removing messages: filter=" + filter.toString());

        try {
            removeMessagesFromQueue(filter);
            int rowCount = SqlConfig.getSqlMapClient().delete("Message.deleteMessage", getFilterMap(filter, null));
            SqlConfig.getSqlMapClient().delete("Message.deleteUnusedAttachments");
            return rowCount;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    private void vacuumMessageAndAttachmentTable() throws SQLException {
        if (DatabaseUtil.statementExists("Message.vacuumMessageTable")) {
            SqlConfig.getSqlMapClient().update("Message.vacuumMessageTable");
        }

        if (DatabaseUtil.statementExists("Message.vacuumAttachmentTable")) {
            SqlConfig.getSqlMapClient().update("Message.vacuumAttachmentTable");
        }
    }

    public int pruneMessages(MessageObjectFilter filter, int limit) throws ControllerException {
        logger.debug("pruning messages: filter=" + filter.toString());

        try {
            int totalRowCount = 0;
            int rowCount = 0;

            do {
                Map<String, Object> parameterMap = getFilterMap(filter, null);

                if (limit > 0) {
                    parameterMap.put("limit", limit);
                }

                // Retry blocks of pruning if they fail in case of deadlocks
                int retryCount = 0;
                do {
                    Connection connection = null;
                    SqlMapSession session = null;
                    
                    try {
                        SqlMapClient sqlMapClient = SqlConfig.getSqlMapClient();
                        // get a connection from the conection pool
                        connection = sqlMapClient.getDataSource().getConnection();
                        // Must set auto commit to false or the commit will fail
                        connection.setAutoCommit(false);
                        // open a new session with that connection
                        session = sqlMapClient.openSession(connection);

                        // start "ionice" if exists
                        if (DatabaseUtil.statementExists("Message.startPruningTransaction", session)) {
                            session.update("Message.startPruningTransaction");
                        }
                        
                        // delete the messages
                        rowCount = session.delete("Message.pruneMessages", parameterMap);

                        // end "ionice" if exists
                        if (DatabaseUtil.statementExists("Message.endPruningTransaction", session)) {
                            session.update("Message.endPruningTransaction");
                        }
                        
                        connection.commit();
                        retryCount = 0;
                    } catch (Exception e) {
                        if (retryCount < 10) {
                            logger.error("Could not prune messages, retry count: " + retryCount, e);
                            retryCount++;
                        } else {
                            throw e; // Quit trying to prune after 10 failures
                        }
                    } finally {
                        session.close();
                        DbUtils.close(connection);
                    }
                } while (retryCount > 0);

                totalRowCount += rowCount;
                Thread.sleep(100);

                /*
                 * Only run again if the limit was used (limit > 0) and the
                 * number of rows removed was >= limit.
                 */
            } while (rowCount >= limit && limit > 0);

            // Retry attachment pruning if it fails in case of deadlocks
            int retryCount = 0;
            
            do {
                try {
                    SqlConfig.getSqlMapClient().delete("Message.deleteUnusedAttachments");
                    retryCount = 0;
                } catch (Exception e) {
                    if (retryCount < 10) {
                        logger.error("Could not prune attachments, retry count: " + retryCount, e);
                        retryCount++;
                    } else {
                        throw e; // Quit trying to prune after 10 failures
                    }
                }
            } while (retryCount > 0);

            vacuumMessageAndAttachmentTable();
            return totalRowCount;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    private void removeMessagesFromQueue(MessageObjectFilter filter) throws Exception {
        String uid = System.currentTimeMillis() + "";
        // clone the filter so that we don't modify the original
        MessageObjectFilter queueFilter = (MessageObjectFilter) SerializationUtils.clone(filter);
        queueFilter.setStatus(Status.QUEUED);
        int size = createMessagesTempTable(queueFilter, uid, true);
        int page = 0;
        int interval = 10;

        while ((page * interval) < size) {
            for (MessageObject message : getMessagesByPage(page, interval, size, uid, true)) {
                removeMessageFromQueue(message);
            }

            page++;
        }

        removeFilterTable(uid);
    }

    private void removeMessageFromQueue(MessageObject message) throws Exception {
        /*
         * Since removeMessagesFromQueue sets the filter status to QUEUED, we
         * want to apply that same filter logic here when removing a single
         * message.
         */
        if (!Status.QUEUED.equals(message.getStatus())) {
            return;
        }

        String queueName = null;
        String messageId = null;

        if (message.getConnectorMap().get(QueueUtil.QUEUE_NAME) != null) {
            queueName = message.getConnectorMap().get(QueueUtil.QUEUE_NAME).toString();
        }

        if (message.getConnectorMap().get(QueueUtil.MESSAGE_ID) != null) {
            messageId = message.getConnectorMap().get(QueueUtil.MESSAGE_ID).toString();
        }

        if ((queueName == null) || (queueName.length() == 0)) {
            String connectorId = ControllerFactory.getFactory().createChannelController().getDeployedConnectorId(message.getChannelId(), message.getConnectorName());
            queueName = QueueUtil.getInstance().getQueueName(message.getChannelId(), connectorId);
        }

        if ((messageId == null) || (messageId.length() == 0)) {
            messageId = message.getId();
        }

        QueueUtil.getInstance().removeMessageFromQueue(queueName, messageId);
        ControllerFactory.getFactory().createChannelStatisticsController().decrementQueuedCount(message.getChannelId());
    }

    public void removeFilterTable(String uid) {
        logger.debug("Removing temporary message table: uid=" + uid);

        try {
            if (DatabaseUtil.statementExists("Message.dropTempMessageTableSequence")) {
                SqlConfig.getSqlMapClient().update("Message.dropTempMessageTableSequence", uid);
            }
        } catch (SQLException e) {
            // supress any warnings about the sequence not existing
            logger.debug(e);
        }

        try {
            if (DatabaseUtil.statementExists("Message.deleteTempMessageTableIndex")) {
                SqlConfig.getSqlMapClient().update("Message.deleteTempMessageTableIndex", uid);
            }
        } catch (SQLException e) {
            // supress any warnings about the index not existing
            logger.debug(e);
        }

        try {
            SqlConfig.getSqlMapClient().update("Message.dropTempMessageTable", uid);
        } catch (SQLException e) {
            // supress any warnings about the table not existing
            logger.debug(e);
        }
    }

    public void clearMessages(String channelId) throws ControllerException {
        logger.debug("clearing messages: channelId=" + channelId);

        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("channelId", channelId);
            SqlConfig.getSqlMapClient().delete("Message.deleteMessage", parameterMap);
            SqlConfig.getSqlMapClient().delete("Message.deleteUnusedAttachments");

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
                                        if (message.getRawDataProtocol().equals(MessageObject.Protocol.DICOM)) {
                                            String rawData = DICOMUtil.getDICOMRawData(message);
                                            message.setRawData(rawData);
                                        } else {
                                            String rawData = AttachmentUtil.reAttachMessage(message);
                                            message.setRawData(rawData);
                                        }

                                    }

                                    if (replace) {
                                        message.getContext().put("replace", "true");
                                        if (!message.getConnectorName().equalsIgnoreCase("source")) {
                                            message.getContext().put("messageId", message.getCorrelationId());
                                        } else {
                                            message.getContext().put("messageId", message.getId());
                                        }
                                    }

                                    message.getContext().put("destinations", destinations);
                                    
                                    /* Keep the original filename if reprocessing
                                     * See MIRTH-1372 for more details
                                     */
                                    if (message.getChannelMap().containsKey("originalFilename")) {
                                        message.getContext().put("originalFilename", MapUtils.getString(message.getChannelMap(), "originalFilename"));    
                                    }
                                    
                                    router.routeMessageByChannelId(message.getChannelId(), message, true);
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
            router.routeMessageByChannelId(message.getChannelId(), message, true);
        } catch (Exception e) {
            throw new ControllerException("could not reprocess message", e);
        }
    }

    private Map<String, Object> getFilterMap(MessageObjectFilter filter, String uid) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        if (uid != null) {
            parameterMap.put("uid", uid);
        }

        parameterMap.put("id", filter.getId());
        parameterMap.put("correlationId", filter.getCorrelationId());
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
        parameterMap.put("searchErrors", filter.isSearchErrors());
        parameterMap.put("quickSearch", filter.getQuickSearch());
        parameterMap.put("ignoreQueued", filter.isIgnoreQueued());
        parameterMap.put("channelIdList", filter.getChannelIdList());

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
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("correlationId", correlationId);
        params.put("connectorName", destinationId);
        return (String) SqlConfig.getSqlMapClient().queryForObject("Message.lookupMessageId", params);
    }

    private String lookupMessageStatus(String messageId) throws SQLException {
        return (String) SqlConfig.getSqlMapClient().queryForObject("Message.lookupMessageStatus", messageId);
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

    public void setError(MessageObject messageObject, String errorType, String errorMessage, Throwable e, Object payload) {
        String fullErrorMessage = errorBuilder.buildErrorMessage(errorType, errorMessage, e);
        // send alert

        // Set the errors on the MO
        if (messageObject != null) {
            messageObject.setErrors(messageObject.getErrors() != null ? messageObject.getErrors() + System.getProperty("line.separator") + System.getProperty("line.separator") + fullErrorMessage : fullErrorMessage);
        }
        // Set the response error
        String responseException = new String();
        if (e != null) {
            responseException = "\t" + e.getClass().getSimpleName() + "\t" + e.getMessage();
        }
        setStatus(messageObject, MessageObject.Status.ERROR, Response.Status.FAILURE, errorMessage + responseException, payload);
    }

    public void setSuccess(MessageObject messageObject, String responseMessage, Object payload) {
        setStatus(messageObject, MessageObject.Status.SENT, Response.Status.SUCCESS, responseMessage, payload);
    }

    public void setTransformed(MessageObject messageObject, Object payload) {
        setStatus(messageObject, MessageObject.Status.TRANSFORMED, Response.Status.SUCCESS, new String(), payload);
    }

    public void setQueued(MessageObject messageObject, String responseMessage, Object payload) {
        // queued messages are stored into a persistence media, so their socket
        // element should be removed
        if (messageObject.getChannelMap().containsKey(RECEIVE_SOCKET)) {
            Object socketObj = messageObject.getChannelMap().get(RECEIVE_SOCKET);
            messageObject.getChannelMap().put(RECEIVE_SOCKET, socketObj.toString());
        }

        setStatus(messageObject, MessageObject.Status.QUEUED, Response.Status.QUEUED, responseMessage, payload);
    }

    public void setFiltered(MessageObject messageObject, String responseMessage, Object payload) {
        setStatus(messageObject, MessageObject.Status.FILTERED, Response.Status.FILTERED, responseMessage, payload);
    }

    private void setStatus(MessageObject messageObject, MessageObject.Status newStatus, Response.Status responseStatus, String responseMessage, Object payload) {
        if ((messageObject.getResponseMap() != null) && !messageObject.getConnectorName().equalsIgnoreCase("source")) {
            messageObject.getResponseMap().put(messageObject.getConnectorName(), new Response(responseStatus, responseMessage, payload));
        }

        MessageObject.Status oldStatus = MessageObject.Status.valueOf(messageObject.getStatus().toString());
        messageObject.setStatus(newStatus);

        if (oldStatus.equals(MessageObject.Status.QUEUED) && !newStatus.equals(MessageObject.Status.QUEUED)) {
            statisticsController.decrementQueuedCount(messageObject.getChannelId());
        }

        boolean replace = oldStatus.equals(MessageObject.Status.QUEUED) || ((messageObject.getContext().get("replace") != null) && messageObject.getContext().get("replace").equals("true"));

        if (replace) {
            if ((messageObject.getContext() != null && messageObject.getContext().get("replace") != null) && messageObject.getContext().get("replace").equals("true")) {
                try {
                    String existingId = null;

                    if (!messageObject.getConnectorName().equalsIgnoreCase("source")) {
                        existingId = lookupMessageId(messageObject.getCorrelationId(), messageObject.getConnectorName());
                    } else {
                        existingId = messageObject.getId();
                    }

                    if (existingId != null) {
                        String messageStatus = lookupMessageStatus(existingId);

                        // The message status will be null if the destination
                        // message was
                        // reprocessed without the associated source message
                        // being in the database
                        if (messageStatus != null) {
                            oldStatus = MessageObject.Status.valueOf(lookupMessageStatus(existingId));
                        }
                    } else {
                        oldStatus = null;
                    }
                } catch (Exception e) {
                    logger.error("Could not locate existing message", e);
                }
            }

            if (oldStatus != null && oldStatus.equals(MessageObject.Status.ERROR)) {
                statisticsController.decrementErrorCount(messageObject.getChannelId());
            } else if (oldStatus != null && oldStatus.equals(MessageObject.Status.FILTERED)) {
                statisticsController.decrementFilteredCount(messageObject.getChannelId());
            } else if (oldStatus != null && oldStatus.equals(MessageObject.Status.TRANSFORMED)) {
                statisticsController.decrementReceivedCount(messageObject.getChannelId());
            } else if (oldStatus != null && oldStatus.equals(MessageObject.Status.SENT)) {
                statisticsController.decrementSentCount(messageObject.getChannelId());
            }
        }

        updateMessage(messageObject, replace);
    }

    public void resetQueuedStatus(MessageObject messageObject) {
        if (messageObject != null) {
            messageObject.setStatus(Status.QUEUED);
            updateMessage(messageObject, true);
            statisticsController.decrementErrorCount(messageObject.getChannelId());
        }
    }

    public Attachment getAttachment(String attachmentId) throws ControllerException {
        try {
            return (Attachment) SqlConfig.getSqlMapClient().queryForObject("Message.getAttachment", attachmentId);
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Returns messages that match either the message id or message correlation
     * id.
     * 
     * @param message
     * @return
     * @throws ControllerException
     */
    public List<Attachment> getAttachmentsByMessage(MessageObject messageObject) throws ControllerException {
        List<Attachment> attachments = new ArrayList<Attachment>();
        try {
            if (StringUtils.isNotEmpty(messageObject.getCorrelationId())) {
                attachments.addAll(SqlConfig.getSqlMapClient().queryForList("Message.getAttachmentsByMessageId", messageObject.getCorrelationId()));
            }

            attachments.addAll(SqlConfig.getSqlMapClient().queryForList("Message.getAttachmentsByMessageId", messageObject.getId()));
        } catch (Exception e) {
            throw new ControllerException(e);
        }

        return attachments;
    }

    public List<Attachment> getAttachmentsByMessageId(String messageId) throws ControllerException {
        try {
            return SqlConfig.getSqlMapClient().queryForList("Message.getAttachmentsByMessageId", messageId);
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public List<Attachment> getAttachmentIdsByMessageId(String messageId) throws ControllerException {
        try {
            return SqlConfig.getSqlMapClient().queryForList("Message.getAttachmentIdsByMessageId", messageId);
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public void insertAttachment(Attachment attachment) {
        try {
            SqlConfig.getSqlMapClient().insert("Message.insertAttachment", attachment);
        } catch (SQLException e) {
            logger.error("could not insert attachment: id=" + attachment.getAttachmentId(), e);
        }
    }

    public void deleteAttachments(MessageObject message) {
        try {
            SqlConfig.getSqlMapClient().delete("Message.deleteAttachments", message);
        } catch (SQLException e) {
            logger.error("could not delete attachment: message id=" + message.getId(), e);
        }
    }

    public void deleteUnusedAttachments() {
        try {
            SqlConfig.getSqlMapClient().delete("Message.deleteUnusedAttachments");
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
        setAttachmentMessageId(messageObject, attachment);
        return attachment;
    }

    public void setAttachmentMessageId(MessageObject messageObject, Attachment attachment) {
        attachment.setMessageId(messageObject.getId());
        // MIRTH-602 --- The following block of code sets the attachment message
        // ID to be the source message id in case we are not storing messages.
        // This will cause all message attachments to be removed in
        // JavaScriptPostProcessor. Otherwise we will have dangling attachments
        // in the DB .
        if (messageObject.getCorrelationId() != null && !messageObject.getCorrelationId().equals("")) {
            Channel channel = ControllerFactory.getFactory().createChannelController().getDeployedChannelById(messageObject.getChannelId());
            if (channel != null && channel.getProperties().containsKey("store_messages")) {
                if (channel.getProperties().get("store_messages").equals("false") || (channel.getProperties().get("store_messages").equals("true") && channel.getProperties().get("error_messages_only").equals("true") && !messageObject.getStatus().equals(MessageObject.Status.ERROR)) || (channel.getProperties().get("store_messages").equals("true") && channel.getProperties().get("dont_store_filtered").equals("true") && messageObject.getStatus().equals(MessageObject.Status.FILTERED))) {
                    attachment.setMessageId(messageObject.getCorrelationId());
                }
            }
        }

    }
}
