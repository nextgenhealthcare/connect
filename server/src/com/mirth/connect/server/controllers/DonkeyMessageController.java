/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DestinationChain;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.message.DataType;
import com.mirth.connect.donkey.util.MapUtil;
import com.mirth.connect.donkey.util.xstream.SerializerException;
import com.mirth.connect.model.MessageImportResult;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.model.filters.elements.ContentSearchElement;
import com.mirth.connect.model.filters.elements.MetaDataSearchElement;
import com.mirth.connect.server.mybatis.MessageSearchResult;
import com.mirth.connect.server.mybatis.MessageTextResult;
import com.mirth.connect.server.util.DICOMMessageUtil;
import com.mirth.connect.server.util.ListRangeIterator;
import com.mirth.connect.server.util.ListRangeIterator.ListRangeItem;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.MessageEncryptionUtil;
import com.mirth.connect.util.MessageExporter;
import com.mirth.connect.util.MessageExporter.MessageExportException;
import com.mirth.connect.util.MessageImporter;
import com.mirth.connect.util.MessageImporter.MessageImportException;
import com.mirth.connect.util.MessageImporter.MessageImportInvalidPathException;
import com.mirth.connect.util.PaginatedList;
import com.mirth.connect.util.messagewriter.MessageWriter;
import com.mirth.connect.util.messagewriter.MessageWriterException;
import com.mirth.connect.util.messagewriter.MessageWriterFactory;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class DonkeyMessageController extends MessageController {
    private static DonkeyMessageController instance = null;

    public static MessageController create() {
        synchronized (DonkeyMessageController.class) {
            if (instance == null) {
                instance = new DonkeyMessageController();
            }

            return instance;
        }
    }

    private Donkey donkey = Donkey.getInstance();
    private Logger logger = Logger.getLogger(this.getClass());

    private DonkeyMessageController() {}

    private Map<String, Object> getBasicParameters(MessageFilter filter, Long localChannelId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("localChannelId", localChannelId);
        params.put("originalIdLower", filter.getOriginalIdLower());
        params.put("originalIdUpper", filter.getOriginalIdUpper());
        params.put("importIdLower", filter.getImportIdLower());
        params.put("importIdUpper", filter.getImportIdUpper());
        params.put("startDate", filter.getStartDate());
        params.put("endDate", filter.getEndDate());
        params.put("serverId", filter.getServerId());
        params.put("statuses", filter.getStatuses());
        params.put("includedMetaDataIds", filter.getIncludedMetaDataIds());
        params.put("excludedMetaDataIds", filter.getExcludedMetaDataIds());
        params.put("sendAttemptsLower", filter.getSendAttemptsLower());
        params.put("sendAttemptsUpper", filter.getSendAttemptsUpper());
        params.put("attachment", filter.getAttachment());
        params.put("error", filter.getError());
        params.put("textSearch", filter.getTextSearch());

        return params;
    }

    @Override
    public long getMaxMessageId(String channelId) {
        DonkeyDao dao = donkey.getDaoFactory().getDao();

        try {
            return dao.getMaxMessageId(channelId);
        } finally {
            dao.close();
        }
    }

    @Override
    public long getMinMessageId(String channelId) {
        DonkeyDao dao = donkey.getDaoFactory().getDao();

        try {
            return dao.getMinMessageId(channelId);
        } finally {
            dao.close();
        }
    }

    @Override
    public Long getMessageCount(MessageFilter filter, Channel channel) {
        if (filter.getIncludedMetaDataIds() != null && filter.getIncludedMetaDataIds().isEmpty() && filter.getExcludedMetaDataIds() == null) {
            return 0L;
        }

        long startTime = System.currentTimeMillis();

        FilterOptions filterOptions = new FilterOptions(filter, channel.getChannelId());
        long maxMessageId = filterOptions.getMaxMessageId();
        long minMessageId = filterOptions.getMinMessageId();

        Long localChannelId = ChannelController.getInstance().getLocalChannelId(channel.getChannelId());
        Map<String, Object> params = getBasicParameters(filter, localChannelId);

        try {
            SqlSession session = SqlConfig.getSqlSessionManager();

            long count = 0;
            long batchSize = 50000;

            while (maxMessageId >= minMessageId) {
                /*
                 * Search in descending order so that messages will be counted from the greatest to
                 * lowest message id
                 */
                long currentMinMessageId = Math.max(maxMessageId - batchSize + 1, minMessageId);
                params.put("maxMessageId", maxMessageId);
                params.put("minMessageId", currentMinMessageId);
                maxMessageId -= batchSize;

                TreeMap<Long, MessageSearchResult> foundMessages = searchAll(session, params, filter, localChannelId, false, filterOptions);

                count += foundMessages.size();
            }

            return count;
        } finally {
            long endTime = System.currentTimeMillis();
            System.out.println(endTime - startTime + " count improved");
            logger.debug("Count executed in " + (endTime - startTime) + "ms");
        }
    }

    @Override
    public List<Message> getMessages(MessageFilter filter, Channel channel, Boolean includeContent, Integer offset, Integer limit) {
        List<Message> messages = new ArrayList<Message>();
        String channelId = channel.getChannelId();

        if (filter.getIncludedMetaDataIds() != null && filter.getIncludedMetaDataIds().isEmpty() && filter.getExcludedMetaDataIds() == null) {
            return messages;
        }

        List<MessageSearchResult> results = searchMessages(filter, channelId, offset, limit);

        DonkeyDao dao = donkey.getDaoFactory().getDao();

        /*
         * If the content is included, we don't want to decrypt because we may want to use the
         * encrypted content directly. If the content is not included, the setting shouldn't matter,
         * but we will set it to decrypt anyways just to be safe.
         */
        dao.setDecryptData(!includeContent);

        try {
            for (MessageSearchResult result : results) {
                Message message = result.getMessage();
                message.setChannelId(channelId);

                List<ConnectorMessage> connectorMessages = dao.getConnectorMessages(channelId, message.getMessageId(), result.getMetaDataIdSet(), includeContent);

                for (ConnectorMessage connectorMessage : connectorMessages) {
                    message.getConnectorMessages().put(connectorMessage.getMetaDataId(), connectorMessage);
                }

                messages.add(message);
            }
        } finally {
            dao.close();
        }

        return messages;
    }

    @Override
    public Message getMessageContent(String channelId, Long messageId) {
        DonkeyDao dao = donkey.getDaoFactory().getDao();

        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("localChannelId", ChannelController.getInstance().getLocalChannelId(channelId));
            params.put("messageId", messageId);

            Message message = SqlConfig.getSqlSessionManager().selectOne("Message.selectMessageById", params);

            if (message != null) {
                message.setChannelId(channelId);
            }

            Map<Integer, ConnectorMessage> connectorMessages = dao.getConnectorMessages(channelId, messageId);

            for (Entry<Integer, ConnectorMessage> connectorMessageEntry : connectorMessages.entrySet()) {
                Integer metaDataId = connectorMessageEntry.getKey();
                ConnectorMessage connectorMessage = connectorMessageEntry.getValue();

                message.getConnectorMessages().put(metaDataId, connectorMessage);
            }

            return message;
        } finally {
            dao.close();
        }
    }

    @Override
    public List<Attachment> getMessageAttachmentIds(String channelId, Long messageId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("localChannelId", ChannelController.getInstance().getLocalChannelId(channelId));
        params.put("messageId", messageId);

        return SqlConfig.getSqlSessionManager().selectList("Message.selectMessageAttachmentIds", params);
    }

    @Override
    public Attachment getMessageAttachment(String channelId, String attachmentId) {
        DonkeyDao dao = donkey.getDaoFactory().getDao();

        try {
            return dao.getMessageAttachment(channelId, attachmentId);
        } finally {
            dao.close();
        }
    }

    @Override
    public List<Attachment> getMessageAttachment(String channelId, Long messageId) {
        DonkeyDao dao = donkey.getDaoFactory().getDao();

        try {
            return dao.getMessageAttachment(channelId, messageId);
        } finally {
            dao.close();
        }
    }

    @Override
    public void removeMessages(String channelId, MessageFilter filter) {
        Channel channel = ControllerFactory.getFactory().createEngineController().getDeployedChannel(channelId);
        if (channel != null) {
            FilterOptions filterOptions = new FilterOptions(filter, channelId);
            long maxMessageId = filterOptions.getMaxMessageId();
            long minMessageId = filterOptions.getMinMessageId();

            Long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
            Map<String, Object> params = getBasicParameters(filter, localChannelId);
            /*
             * Include the processed boolean with the result set in order to determine whether the
             * message can be deleted if the channel is not stopped
             */
            params.put("includeProcessed", true);

            SqlSession session = SqlConfig.getSqlSessionManager();

            long batchSize = 50000;

            while (maxMessageId >= minMessageId) {
                /*
                 * Search in descending order so that messages will be deleted from the greatest to
                 * lowest message id
                 */
                long currentMinMessageId = Math.max(maxMessageId - batchSize + 1, minMessageId);
                params.put("maxMessageId", maxMessageId);
                params.put("minMessageId", currentMinMessageId);
                maxMessageId -= batchSize;

                TreeMap<Long, MessageSearchResult> results = searchAll(session, params, filter, localChannelId, true, filterOptions);

                Map<Long, Set<Integer>> messages = new HashMap<Long, Set<Integer>>();

                // For each message that was retrieved
                for (Entry<Long, MessageSearchResult> entry : results.entrySet()) {
                    Long messageId = entry.getKey();
                    MessageSearchResult result = entry.getValue();
                    Set<Integer> metaDataIds = result.getMetaDataIdSet();
                    boolean processed = result.isProcessed();

                    // Allow unprocessed messages to be deleted only if the channel is stopped.
                    if (channel.getCurrentState() == DeployedState.STOPPED || processed) {
                        if (metaDataIds.contains(0)) {
                            // Delete the entire message if the source connector message is to be deleted
                            messages.put(messageId, null);
                        } else {
                            // Otherwise only deleted the destination connector message
                            messages.put(messageId, metaDataIds);
                        }
                    }
                }

                // Prevent the delete from occurring at the same time as the channel being started. 
                synchronized (channel) {
                    com.mirth.connect.donkey.server.controllers.MessageController.getInstance().deleteMessages(channelId, messages);
                }
            }

            // Invalidate the queue buffer to ensure stats are updated.
            channel.invalidateQueues();
        }
    }

    @Override
    public void clearMessages(Set<String> channelIds, Boolean restartRunningChannels, Boolean clearStatistics) throws ControllerException {
        DonkeyDao dao = donkey.getDaoFactory().getDao();

        try {
            EngineController engineController = ControllerFactory.getFactory().createEngineController();

            for (String channelId : channelIds) {
                Channel channel = engineController.getDeployedChannel(channelId);

                if (channel != null) {
                    Set<Integer> connectorsToStart = new HashSet<Integer>();
                    DeployedState priorState = channel.getCurrentState();
                    if (priorState != DeployedState.PAUSED && priorState != DeployedState.PAUSING) {
                        connectorsToStart.add(0);
                    }

                    for (DestinationChain chain : channel.getDestinationChains()) {
                        for (DestinationConnector destinationConnector : chain.getDestinationConnectors().values()) {
                            if (destinationConnector.getCurrentState() != DeployedState.STOPPED && destinationConnector.getCurrentState() != DeployedState.STOPPING) {
                                connectorsToStart.add(destinationConnector.getMetaDataId());
                            }
                        }
                    }

                    boolean startChannelAfter = false;

                    if (priorState != DeployedState.STOPPED && restartRunningChannels) {
                        try {
                            logger.debug("Stopping channel \"" + channel.getName() + "\" prior to removing messages");
                            channel.stop();

                            if (priorState != DeployedState.STOPPING) {
                                startChannelAfter = true;
                            }
                        } catch (StopException e) {
                            logger.error("Failed to stop channel id " + channelId, e);
                        }
                    }

                    // Prevent the delete from occurring at the same time as the channel being started. 
                    synchronized (channel) {
                        // Only allow the messages to be cleared if the channel is stopped.
                        if (channel.getCurrentState() == DeployedState.STOPPED) {
                            logger.debug("Removing messages for channel \"" + channel.getName() + "\"");
                            dao.deleteAllMessages(channelId);

                            if (clearStatistics) {
                                logger.debug("Clearing statistics for channel \"" + channel.getName() + "\"");

                                Set<Status> statuses = Statistics.getTrackedStatuses();
                                dao.resetStatistics(channelId, null, statuses);

                                for (Integer metaDataId : channel.getMetaDataIds()) {
                                    dao.resetStatistics(channelId, metaDataId, statuses);
                                }
                            }

                            dao.commit();

                            // Invalidate the queue buffer to ensure stats are updated.
                            channel.invalidateQueues();
                        }
                    }

                    if (startChannelAfter) {
                        try {
                            logger.debug("Restarting channel \"" + channel.getName() + "\"");
                            // Only start the source connector if the channel wasn't paused or pausing before
                            channel.start(connectorsToStart);
                        } catch (StartException e) {
                            logger.error("Failed to start channel id " + channelId, e);
                        }
                    }
                }
            }
        } finally {
            dao.close();
        }
    }

    public void reprocessMessages(String channelId, MessageFilter filter, boolean replace, List<Integer> reprocessMetaDataIds) {
        EngineController engineController = ControllerFactory.getFactory().createEngineController();
        Channel deployedChannel = engineController.getDeployedChannel(channelId);
        AttachmentHandler attachmentHandler = deployedChannel.getAttachmentHandler();
        DataType dataType = deployedChannel.getSourceConnector().getInboundDataType();
        boolean isBinary = ExtensionController.getInstance().getDataTypePlugins().get(dataType.getType()).isBinary();
        Encryptor encryptor = ConfigurationController.getInstance().getEncryptor();

        FilterOptions filterOptions = new FilterOptions(filter, channelId);
        long maxMessageId = filterOptions.getMaxMessageId();
        long minMessageId = filterOptions.getMinMessageId();

        Long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        Map<String, Object> params = getBasicParameters(filter, localChannelId);
        /*
         * Include the import id with the result set in order to determine whether the message was
         * imported
         */
        params.put("includeImportId", true);

        SqlSession session = SqlConfig.getSqlSessionManager();

        long batchSize = 50000;

        while (maxMessageId >= minMessageId) {
            /*
             * Search in ascending order so that messages will be reprocessed from the lowest to
             * greatest message id
             */
            long currentMaxMessageId = Math.min(minMessageId + batchSize - 1, maxMessageId);
            params.put("maxMessageId", currentMaxMessageId);
            params.put("minMessageId", minMessageId);
            minMessageId += batchSize;

            TreeMap<Long, MessageSearchResult> foundMessages = searchAll(session, params, filter, localChannelId, true, filterOptions);

            for (Entry<Long, MessageSearchResult> entry : foundMessages.entrySet()) {
                Long messageId = entry.getKey();
                Long importId = entry.getValue().getImportId();
                params.put("messageId", messageId);

                List<MessageContent> contentList = SqlConfig.getSqlSessionManager().selectList("Message.selectMessageForReprocessing", params);

                MessageContent rawContent = null;
                MessageContent sourceMapContent = null;

                if (contentList != null) {
                    for (MessageContent content : contentList) {
                        if (content.getContentType() == ContentType.RAW) {
                            rawContent = content;
                        } else if (content.getContentType() == ContentType.SOURCE_MAP) {
                            sourceMapContent = content;
                        }
                    }
                }

                if (rawContent != null) {
                    if (rawContent.isEncrypted()) {
                        rawContent.setContent(encryptor.decrypt(rawContent.getContent()));
                        rawContent.setEncrypted(false);
                    }

                    ConnectorMessage connectorMessage = new ConnectorMessage();
                    connectorMessage.setChannelId(channelId);
                    connectorMessage.setMessageId(messageId);
                    connectorMessage.setMetaDataId(0);
                    connectorMessage.setRaw(rawContent);

                    RawMessage rawMessage = null;

                    if (isBinary) {
                        rawMessage = new RawMessage(DICOMMessageUtil.getDICOMRawBytes(connectorMessage));
                    } else {
                        rawMessage = new RawMessage(org.apache.commons.codec.binary.StringUtils.newString(attachmentHandler.reAttachMessage(rawContent.getContent(), connectorMessage, Constants.ATTACHMENT_CHARSET, false), Constants.ATTACHMENT_CHARSET));
                    }

                    rawMessage.setOverwrite(replace);
                    rawMessage.setImported(importId != null);
                    rawMessage.setOriginalMessageId(messageId);
                    rawMessage.setDestinationMetaDataIds(reprocessMetaDataIds);

                    try {
                        if (sourceMapContent != null) {
                            if (sourceMapContent.isEncrypted()) {
                                sourceMapContent.setContent(encryptor.decrypt(sourceMapContent.getContent()));
                                sourceMapContent.setEncrypted(false);
                            }

                            rawMessage.setSourceMap(MapUtil.deserializeMap(ObjectXMLSerializer.getInstance(), sourceMapContent.getContent()));
                        }

                        engineController.dispatchRawMessage(channelId, rawMessage, true);
                    } catch (SerializerException e) {
                        logger.error("Could not reprocess message " + messageId + " for channel " + channelId + " because the source map content is invalid.", e);
                    } catch (ChannelException e) {
                        if (e.isStopped()) {
                            // This should only return true if the entire channel is stopped, since we are forcing the message even if the source connector is stopped.
                            logger.error("Reprocessing job cancelled because the channel is stopping or stopped.", e);
                            return;
                        }
                    } catch (Throwable e) {
                        // Do nothing. An error should have been logged.
                    }
                } else {
                    logger.error("Could not reprocess message " + messageId + " for channel " + channelId + " because no source raw content was found. The content may have been pruned or the channel may not be configured to store raw content.");
                }
            }
        }
    }

    @Override
    public int exportMessages(final String channelId, final MessageFilter messageFilter, int pageSize, boolean includeAttachments, MessageWriterOptions options) throws MessageExportException, InterruptedException {
        final MessageController messageController = this;
        final EngineController engineController = ControllerFactory.getFactory().createEngineController();

        PaginatedList<Message> messageList = new PaginatedList<Message>() {
            @Override
            public Long getItemCount() {
                return messageController.getMessageCount(messageFilter, engineController.getDeployedChannel(channelId));
            }

            @Override
            protected List<Message> getItems(int offset, int limit) throws Exception {
                return messageController.getMessages(messageFilter, engineController.getDeployedChannel(channelId), true, offset, limit);
            }
        };

        messageList.setPageSize(pageSize);

        try {
            MessageWriter messageWriter = MessageWriterFactory.getInstance().getMessageWriter(options, ConfigurationController.getInstance().getEncryptor());
            int numExported = new MessageExporter().exportMessages(messageList, messageWriter);
            messageWriter.close();
            return numExported;
        } catch (MessageWriterException e) {
            throw new MessageExportException(e);
        }
    }

    @Override
    public void importMessage(String channelId, Message message) throws MessageImportException {
        try {
            MessageEncryptionUtil.decryptMessage(message, ConfigurationController.getInstance().getEncryptor());
            Channel channel = donkey.getDeployedChannels().get(channelId);

            if (channel == null) {
                throw new MessageImportException("Failed to import message, channel ID " + channelId + " is not currently deployed");
            } else {
                channel.importMessage(message);
            }
        } catch (DonkeyException e) {
            throw new MessageImportException(e);
        }
    }

    @Override
    public MessageImportResult importMessagesServer(final String channelId, String path, boolean includeSubfolders) throws MessageImportException, InterruptedException, MessageImportInvalidPathException {
        Channel channel = donkey.getDeployedChannels().get(channelId);

        if (channel == null) {
            throw new MessageImportException("Failed to import message, channel ID " + channelId + " is not currently deployed");
        }

        MessageWriter messageWriter = new MessageWriterChannel(channel);
        return new MessageImporter().importMessages(path, includeSubfolders, messageWriter, System.getProperty("user.dir"));
    }

    private List<MessageSearchResult> searchMessages(MessageFilter filter, String channelId, int offset, int limit) {
        long startTime = System.currentTimeMillis();

        FilterOptions filterOptions = new FilterOptions(filter, channelId);
        long maxMessageId = filterOptions.getMaxMessageId();
        long minMessageId = filterOptions.getMinMessageId();

        Long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        Map<String, Object> params = getBasicParameters(filter, localChannelId);

        try {
            TreeMap<Long, MessageSearchResult> messages = new TreeMap<Long, MessageSearchResult>();
            SqlSession session = SqlConfig.getSqlSessionManager();

            int offsetRemaining = offset;
            /*
             * If the limit is greater than the default batch size, use the limit, but cap it at
             * 50000.
             */
            long batchSize = Math.min(Math.max(limit, 500), 50000);
            long totalSearched = 0;

            while (messages.size() < limit && maxMessageId >= minMessageId) {
                /*
                 * Slowly increase the batch size in case all the necessary results are found early
                 * on.
                 */
                if (totalSearched >= 100000 && batchSize < 50000) {
                    batchSize = 50000;
                } else if (totalSearched >= 10000 && batchSize < 10000) {
                    batchSize = 10000;
                } else if (totalSearched >= 1000 && batchSize < 1000) {
                    batchSize = 1000;
                }

                /*
                 * Search in descending order so that messages will be found from the greatest to
                 * lowest message id
                 */
                long currentMinMessageId = Math.max(maxMessageId - batchSize + 1, minMessageId);
                params.put("maxMessageId", maxMessageId);
                params.put("minMessageId", currentMinMessageId);
                maxMessageId -= batchSize;
                totalSearched += batchSize;

                TreeMap<Long, MessageSearchResult> foundMessages = searchAll(session, params, filter, localChannelId, false, filterOptions);

                if (!foundMessages.isEmpty()) {
                    /*
                     * Skip results until there is no offset remaining. This is required when
                     * viewing results beyond the first page
                     */
                    if (offsetRemaining >= foundMessages.size()) {
                        offsetRemaining -= foundMessages.size();
                    } else {
                        while (offsetRemaining-- > 0) {
                            foundMessages.pollLastEntry();
                        }

                        messages.putAll(foundMessages);
                    }
                }
            }

            // Remove results beyond the limit requested
            while (messages.size() > limit) {
                messages.pollFirstEntry();
            }

            List<MessageSearchResult> results = new ArrayList<MessageSearchResult>(messages.size());

            /*
             * Now that we have the message and metadata ids that should be returned as the result,
             * we need to retrieve the message data for those.
             */
            if (!messages.isEmpty()) {
                Iterator<Long> iterator = messages.descendingKeySet().iterator();

                while (iterator.hasNext()) {
                    Map<String, Object> messageParams = new HashMap<String, Object>();
                    messageParams.put("localChannelId", localChannelId);

                    ListRangeIterator listRangeIterator = new ListRangeIterator(iterator, ListRangeIterator.DEFAULT_LIST_LIMIT, false, null);

                    while (listRangeIterator.hasNext()) {
                        ListRangeItem item = listRangeIterator.next();
                        List<Long> list = item.getList();
                        Long startRange = item.getStartRange();
                        Long endRange = item.getEndRange();

                        if (list != null || (startRange != null && endRange != null)) {
                            if (list != null) {
                                messageParams.remove("minMessageId");
                                messageParams.remove("maxMessageId");
                                messageParams.put("includeMessageList", StringUtils.join(list, ","));
                            } else {
                                messageParams.remove("includeMessageList");
                                messageParams.put("minMessageId", endRange);
                                messageParams.put("maxMessageId", startRange);
                            }

                            // Get the current batch of results
                            List<MessageSearchResult> currentResults = session.selectList("Message.selectMessagesById", messageParams);

                            // Add the metadata ids to each result
                            for (MessageSearchResult currentResult : currentResults) {
                                currentResult.setMetaDataIdSet(messages.get(currentResult.getMessageId()).getMetaDataIdSet());
                            }

                            // Add the current batch to the final list of results
                            results.addAll(currentResults);
                        }
                    }
                }
            }

            return results;
        } finally {
            long endTime = System.currentTimeMillis();
            System.out.println(endTime - startTime + " search improved");
            logger.debug("Search executed in " + (endTime - startTime) + "ms");
        }
    }

    private TreeMap<Long, MessageSearchResult> searchAll(SqlSession session, Map<String, Object> params, MessageFilter filter, Long localChannelId, boolean includeMessageData, FilterOptions filterOptions) {
        TreeMap<Long, MessageSearchResult> foundMessages = new TreeMap<Long, MessageSearchResult>();

        // Search the message table to find which message ids meet the search criteria.
        List<MessageTextResult> messageResults = session.selectList("Message.searchMessageTable", params);
        /*
         * If the message table search provided no records then there is no need to perform any more
         * searches on this range of message ids.
         */
        if (!messageResults.isEmpty()) {
            Set<Long> messageIdSet = new HashSet<Long>(messageResults.size());
            for (MessageTextResult messageResult : messageResults) {
                messageIdSet.add(messageResult.getMessageId());
            }

            /*
             * Search the metadata table to find which message and metadataids meet the search
             * criteria. If a text search is being performed, we also check the connector name
             * column while we're at it.
             */

            List<MessageTextResult> metaDataResults = session.selectList("Message.searchMetaDataTable", params);
            /*
             * Messages that matched the text search criteria. Since text search spans across
             * multiple tables (metadata, content, custom metadata), the map is created it can be
             * used for the searches on each table.
             */
            Map<Long, MessageSearchResult> textMessages = new HashMap<Long, MessageSearchResult>();
            /*
             * Messages that met the criteria on the message and metadata tables and still need to
             * have lengthy search run on them.
             */
            Map<Long, MessageSearchResult> potentialMessages = new HashMap<Long, MessageSearchResult>();
            for (MessageTextResult metaDataResult : metaDataResults) {
                if (messageIdSet.contains(metaDataResult.getMessageId())) {
                    if (filterOptions.isSearchText() && metaDataResult.isTextFound() != null && metaDataResult.isTextFound()) {
                        /*
                         * Text search was found in the metadata table so add the message/metadata
                         * id to the text messages.
                         */
                        addMessageToMap(textMessages, metaDataResult.getMessageId(), metaDataResult.getMetaDataId());

                        if (filterOptions.isSearchCustomMetaData() || filterOptions.isSearchContent()) {
                            /*
                             * If content or custom metadata is being searched, still add the
                             * message to potentialMessages so the lengthy search will be run.
                             */
                            addMessageToMap(potentialMessages, metaDataResult.getMessageId(), metaDataResult.getMetaDataId());
                        }
                    } else if (filterOptions.isSearchCustomMetaData() || filterOptions.isSearchContent() || filterOptions.isSearchText()) {
                        /*
                         * If no text search was found and any lengthy search is required, add the
                         * message to potentialMessages.
                         */
                        addMessageToMap(potentialMessages, metaDataResult.getMessageId(), metaDataResult.getMetaDataId());
                    } else {
                        /*
                         * If no lengthy search is required, just add the message to foundMesages.
                         */
                        addMessageToMap(foundMessages, metaDataResult.getMessageId(), metaDataResult.getMetaDataId());
                    }
                }
            }

            // These are no longer used so allow GC to reclaim their memory
            metaDataResults = null;
            messageIdSet = null;
            if (!includeMessageData) {
                messageResults = null;
            }

            if (potentialMessages.isEmpty()) {
                // If lengthy search is not being run, add all text messages to found messages
                foundMessages.putAll(textMessages);
            } else {
                // Otherwise run lengthy search and existing text messages will be joined within
                searchLengthy(localChannelId, potentialMessages, foundMessages, textMessages, filter, filterOptions);
            }

            /*
             * If message data was requested then copy it from the message search into the final
             * results
             */
            if (!foundMessages.isEmpty() && includeMessageData) {
                // Build a map of the message data results for quicker access
                Map<Long, MessageTextResult> messageDataResults = new HashMap<Long, MessageTextResult>(messageResults.size());
                for (MessageTextResult messageResult : messageResults) {
                    messageDataResults.put(messageResult.getMessageId(), messageResult);
                }

                /*
                 * For each found result, copy over any message data that may have been retrieved
                 * already
                 */
                for (Entry<Long, MessageSearchResult> entry : foundMessages.entrySet()) {
                    Long messageId = entry.getKey();
                    MessageSearchResult result = entry.getValue();

                    MessageTextResult textResult = messageDataResults.get(messageId);
                    if (textResult != null) {
                        result.setImportId(textResult.getImportId());
                        result.setProcessed(textResult.getProcessed());
                    }
                }
            }
        }

        return foundMessages;
    }

    private void searchLengthy(Long localChannelId, Map<Long, MessageSearchResult> potentialMessages, Map<Long, MessageSearchResult> foundMessages, Map<Long, MessageSearchResult> textMessages, MessageFilter filter, FilterOptions filterOptions) {
        SqlSession session = SqlConfig.getSqlSessionManager();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("localChannelId", localChannelId);
        params.put("includedMetaDataIds", filter.getIncludedMetaDataIds());
        params.put("excludedMetaDataIds", filter.getExcludedMetaDataIds());

        // Use a list range iterator to find the ranges or list of ids to perform each search on.
        ListRangeIterator listRangeIterator = new ListRangeIterator(new TreeSet<Long>(potentialMessages.keySet()).descendingIterator(), ListRangeIterator.DEFAULT_LIST_LIMIT, false, null);

        while (listRangeIterator.hasNext()) {
            ListRangeItem item = listRangeIterator.next();
            List<Long> list = item.getList();
            Long startRange = item.getStartRange();
            Long endRange = item.getEndRange();

            if (list != null || (startRange != null && endRange != null)) {
                if (list != null) {
                    params.remove("minMessageId");
                    params.remove("maxMessageId");
                    params.put("includeMessageList", StringUtils.join(list, ","));
                } else {
                    params.remove("includeMessageList");
                    params.put("minMessageId", endRange);
                    params.put("maxMessageId", startRange);
                }

                boolean searchCustomMetaData = filterOptions.isSearchCustomMetaData();
                boolean searchContent = filterOptions.isSearchContent();
                boolean searchText = filterOptions.isSearchText();

                /*
                 * The map of messages that contains the combined results from all of the lengthy
                 * searches. For all searches that are being performed, a message and metadata id
                 * must be found in all three in order for the message to remain in this map
                 */
                Map<Long, MessageSearchResult> tempMessages = null;

                if (searchCustomMetaData) {
                    tempMessages = new HashMap<Long, MessageSearchResult>();
                    // Perform the custom metadata search
                    searchCustomMetaData(session, new HashMap<String, Object>(params), potentialMessages, tempMessages, filter.getMetaDataSearch());

                    /*
                     * If tempMessages is empty, there is no need to search on either the content or
                     * text because the join will never return any results
                     */
                    if (tempMessages.isEmpty()) {
                        searchContent = false;
                        searchText = false;
                    }
                }
                if (searchContent) {
                    Map<Long, MessageSearchResult> contentMessages = new HashMap<Long, MessageSearchResult>();
                    // Perform the content search
                    searchContent(session, new HashMap<String, Object>(params), potentialMessages, contentMessages, filter.getContentSearch());

                    if (tempMessages == null) {
                        /*
                         * If temp messages has not been created yet, then there is no need to join
                         * the results from this search and previous searches. Just set the current
                         * messages as the temp messages
                         */
                        tempMessages = contentMessages;
                    } else {
                        /*
                         * Otherwise join the two maps so that the only results left in tempMessages
                         * are those that also exist in the current message map
                         */
                        joinMessages(tempMessages, contentMessages);
                    }

                    /*
                     * If tempMessages is empty, there is no need to search on either the text
                     * because the join will never return any results
                     */
                    if (tempMessages.isEmpty()) {
                        searchText = false;
                    }
                }
                if (searchText) {
                    // Perform the text search
                    searchText(session, new HashMap<String, Object>(params), potentialMessages, textMessages, filter.getTextSearch(), filter.getTextSearchMetaDataColumns());

                    if (tempMessages == null) {
                        /*
                         * If temp messages has not been created yet, then there is no need to join
                         * the results from this search and previous searches. Just set the current
                         * messages as the temp messages
                         */
                        tempMessages = textMessages;
                    } else {
                        /*
                         * Otherwise join the two maps so that the only results left in tempMessages
                         * are those that also exist in the current message map
                         */
                        joinMessages(tempMessages, textMessages);
                    }
                }

                /*
                 * Add all the results from tempMessages after all the joins have been completed
                 * into foundMessages
                 */
                foundMessages.putAll(tempMessages);
            }
        }
    }

    private void searchCustomMetaData(SqlSession session, Map<String, Object> params, Map<Long, MessageSearchResult> potentialMessages, Map<Long, MessageSearchResult> customMetaDataMessages, List<MetaDataSearchElement> metaDataSearchElements) {
        params.put("metaDataSearch", metaDataSearchElements);

        /*
         * Search the custom meta data table for message and metadata ids matching the metadata
         * search criteria
         */
        List<MessageTextResult> results = session.selectList("Message.searchCustomMetaDataTable", params);

        for (MessageTextResult result : results) {
            Long messageId = result.getMessageId();
            Integer metaDataId = result.getMetaDataId();

            Set<Integer> allowedMetaDataIds = potentialMessages.get(messageId).getMetaDataIdSet();
            /*
             * Ignore the message and metadata id if they are not allowed because they were already
             * filtered in a previous step
             */
            if (allowedMetaDataIds.contains(metaDataId)) {
                addMessageToMap(customMetaDataMessages, messageId, metaDataId);
            }
        }
    }

    private void searchContent(SqlSession session, Map<String, Object> params, Map<Long, MessageSearchResult> potentialMessages, Map<Long, MessageSearchResult> contentMessages, List<ContentSearchElement> contentSearchElements) {
        int index = 0;

        while (index < contentSearchElements.size() && (index == 0 || !contentMessages.isEmpty())) {
            ContentSearchElement element = contentSearchElements.get(index);

            if (CollectionUtils.isNotEmpty(element.getSearches())) {
                params.put("contentType", element.getContentCode());
                params.put("contents", element.getSearches());

                /*
                 * Search the content table for message and metadata ids matching the content search
                 * criteria
                 */
                List<MessageTextResult> results = session.selectList("Message.searchContentTable", params);

                Map<Long, MessageSearchResult> tempMessages = new HashMap<Long, MessageSearchResult>();

                for (MessageTextResult result : results) {
                    Long messageId = result.getMessageId();
                    Integer metaDataId = result.getMetaDataId();

                    Set<Integer> allowedMetaDataIds = potentialMessages.get(messageId).getMetaDataIdSet();
                    /*
                     * Ignore the message and metadata id if they are not allowed because they were
                     * already filtered in a previous step
                     */
                    if (allowedMetaDataIds.contains(metaDataId)) {
                        if (index == 0) {
                            /*
                             * For the first search, add the results to the final result map since
                             * there is nothing to join from
                             */
                            addMessageToMap(contentMessages, messageId, metaDataId);
                        } else {
                            /*
                             * For other searches, add the results to the temp result map so they
                             * can be joined with the final result map
                             */
                            addMessageToMap(tempMessages, messageId, metaDataId);
                        }
                    }
                }

                /*
                 * If the raw content is being searched, perform an additional search on the source
                 * encoded content since the destination
                 */
                if (ContentType.fromCode(element.getContentCode()) == ContentType.RAW) {
                    params.put("metaDataId", 0);
                    params.put("contentType", ContentType.ENCODED.getContentTypeCode());

                    results = session.selectList("Message.searchContentTable", params);
                    params.remove("metaDataId");

                    for (MessageTextResult result : results) {
                        Long messageId = result.getMessageId();

                        Set<Integer> allowedMetaDataIds = potentialMessages.get(messageId).getMetaDataIdSet();
                        for (Integer allowedMetaDataId : allowedMetaDataIds) {
                            if (allowedMetaDataId != 0) {
                                /*
                                 * If the source encoded is found, then all destinations have
                                 * matched on the raw content, so all allowed metadata ids other
                                 * than 0 (source) need to be added
                                 */
                                if (index == 0) {
                                    /*
                                     * For the first search, add the results to the final result map
                                     * since there is nothing to join from
                                     */
                                    addMessageToMap(contentMessages, messageId, allowedMetaDataId);
                                } else {
                                    /*
                                     * For other searches, add the results to the temp result map so
                                     * they can be joined with the final result map
                                     */
                                    addMessageToMap(tempMessages, messageId, allowedMetaDataId);
                                }
                            }
                        }
                    }
                }

                if (index > 0) {
                    /*
                     * If there are more than one searches performed, join the results since the
                     * message and metadata ids must be found in all searches in order to be
                     * considered "found"
                     */
                    joinMessages(contentMessages, tempMessages);
                }
            }

            index++;
        }
    }

    private void searchText(SqlSession session, Map<String, Object> params, Map<Long, MessageSearchResult> potentialMessages, Map<Long, MessageSearchResult> textMessages, String text, List<String> textSearchMetaDataColumns) {
        params.put("contents", Collections.singletonList(text));
        params.put("textSearch", text);
        params.put("textSearchMetaDataColumns", textSearchMetaDataColumns);

        /*
         * Search the custom meta data table for message and metadata ids matching the text search
         * criteria
         */
        List<MessageTextResult> results = session.selectList("Message.searchCustomMetaDataTable", params);

        for (MessageTextResult result : results) {
            Long messageId = result.getMessageId();
            Integer metaDataId = result.getMetaDataId();

            Set<Integer> allowedMetaDataIds = potentialMessages.get(messageId).getMetaDataIdSet();
            /*
             * Ignore the message and metadata id if they are not allowed because they were already
             * filtered in a previous step
             */
            if (allowedMetaDataIds.contains(metaDataId)) {
                addMessageToMap(textMessages, messageId, metaDataId);
            }
        }

        /*
         * Search the content table for message and metadata ids matching the text search criteria
         */
        results = session.selectList("Message.searchContentTable", params);

        for (MessageTextResult result : results) {
            Long messageId = result.getMessageId();
            Integer metaDataId = result.getMetaDataId();
            Integer contentCode = result.getContentType();
            ContentType contentType = ContentType.fromCode(contentCode);

            Set<Integer> allowedMetaDataIds = potentialMessages.get(messageId).getMetaDataIdSet();
            if (metaDataId == 0 && contentType == ContentType.ENCODED) {
                /*
                 * If the text search is found in the source encoded content, then all the allowed
                 * destinations would match on the raw content so all allowed metadata ids for this
                 * message need to be added
                 */
                for (Integer allowedMetaDataId : allowedMetaDataIds) {
                    addMessageToMap(textMessages, messageId, allowedMetaDataId);
                }
            } else if (allowedMetaDataIds.contains(metaDataId)) {
                /*
                 * Ignore the message and metadata id if they are not allowed because they were
                 * already filtered in a previous step
                 */
                addMessageToMap(textMessages, messageId, metaDataId);
            }
        }
    }

    private void addMessageToMap(Map<Long, MessageSearchResult> messages, Long messageId, Integer metaDataId) {
        MessageSearchResult result = messages.get(messageId);

        if (result == null) {
            result = new MessageSearchResult();
            result.setMetaDataIdSet(new TreeSet<Integer>());
            messages.put(messageId, result);
        }

        result.getMetaDataIdSet().add(metaDataId);
    }

    /**
     * Removes all message and metadata ids from messages that do not exist in newMessages
     */
    private void joinMessages(Map<Long, MessageSearchResult> messages, Map<Long, MessageSearchResult> newMessages) {
        Iterator<Entry<Long, MessageSearchResult>> iterator = messages.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<Long, MessageSearchResult> entry = iterator.next();
            Long tempMessageId = entry.getKey();

            if (!newMessages.containsKey(tempMessageId)) {
                iterator.remove();
            } else {
                Set<Integer> firstMetaDataIds = entry.getValue().getMetaDataIdSet();
                firstMetaDataIds.retainAll(newMessages.get(tempMessageId).getMetaDataIdSet());
            }
        }
    }

    private class MessageWriterChannel implements MessageWriter {
        private Channel channel;
        private DonkeyDao dao;
        private Encryptor encryptor = ConfigurationController.getInstance().getEncryptor();

        public MessageWriterChannel(Channel channel) {
            this.channel = channel;
            this.dao = channel.getDaoFactory().getDao();
        }

        @Override
        public boolean write(Message message) throws MessageWriterException {
            MessageEncryptionUtil.decryptMessage(message, encryptor);

            try {
                channel.importMessage(message, dao);
            } catch (DonkeyException e) {
                throw new MessageWriterException(e);
            }

            return true;
        }

        @Override
        public void close() throws MessageWriterException {
            try {
                dao.commit();
            } finally {
                dao.close();
            }
        }
    }

    private class FilterOptions {
        private long minMessageId;
        private long maxMessageId;
        private boolean searchCustomMetaData;
        private boolean searchContent;
        private boolean searchText;

        public FilterOptions(MessageFilter filter, String channelId) {
            if (filter.getMinMessageId() != null && filter.getMinMessageId() > filter.getMaxMessageId()) {
                /*
                 * If the min message id is greater than the max, use them directly so they fail at
                 * a later point. If we fix them by getting the actual min and max, we may return
                 * results that we shouldn't.
                 */
                minMessageId = filter.getMinMessageId();
                maxMessageId = filter.getMaxMessageId();
            } else {
                /*
                 * If the min is less than the actual min, set the min message id to the actual min
                 * to prevent unnecessary searches
                 */
                minMessageId = Math.max(filter.getMinMessageId() == null ? 1L : filter.getMinMessageId(), DonkeyMessageController.this.getMinMessageId(channelId));
                /*
                 * If the max is greater than the actual max, set the max message id to the actual
                 * max to prevent unnecessary searches
                 */
                maxMessageId = Math.min(filter.getMaxMessageId(), DonkeyMessageController.this.getMaxMessageId(channelId));
            }

            searchCustomMetaData = CollectionUtils.isNotEmpty(filter.getMetaDataSearch());
            searchContent = CollectionUtils.isNotEmpty(filter.getContentSearch());
            searchText = filter.getTextSearch() != null;
        }

        public long getMinMessageId() {
            return minMessageId;
        }

        public long getMaxMessageId() {
            return maxMessageId;
        }

        public boolean isSearchCustomMetaData() {
            return searchCustomMetaData;
        }

        public boolean isSearchContent() {
            return searchContent;
        }

        public boolean isSearchText() {
            return searchText;
        }
    }
}
