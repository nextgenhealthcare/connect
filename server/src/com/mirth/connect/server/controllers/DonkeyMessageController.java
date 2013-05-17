/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.channel.ChannelState;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.message.DataType;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.server.mybatis.MessageSearchResult;
import com.mirth.connect.server.util.AttachmentUtil;
import com.mirth.connect.server.util.DICOMUtil;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.MessageEncryptionUtil;
import com.mirth.connect.util.MessageExporter;
import com.mirth.connect.util.MessageExporter.MessageExportException;
import com.mirth.connect.util.MessageImporter;
import com.mirth.connect.util.MessageImporter.MessageImportException;
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

    private Logger logger = Logger.getLogger(this.getClass());

    private DonkeyMessageController() {}

    private Map<String, Object> getParameters(MessageFilter filter, String channelId, Integer offset, Integer limit) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("localChannelId", ChannelController.getInstance().getLocalChannelId(channelId));
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("messageIdUpper", filter.getMessageIdUpper());
        params.put("messageIdLower", filter.getMessageIdLower());
        params.put("importIdUpper", filter.getImportIdUpper());
        params.put("importIdLower", filter.getImportIdLower());
        params.put("startDate", filter.getStartDate());
        params.put("endDate", filter.getEndDate());
        params.put("quickSearch", filter.getQuickSearch());
        params.put("statuses", filter.getStatuses());
        params.put("sendAttemptsLower", filter.getSendAttemptsLower());
        params.put("sendAttemptsUpper", filter.getSendAttemptsUpper());
        params.put("type", filter.getType());
        params.put("source", filter.getSource());
        params.put("rawSearch", filter.getContentSearch().get(ContentType.RAW));
        params.put("processedRawSearch", filter.getContentSearch().get(ContentType.PROCESSED_RAW));
        params.put("transformedSearch", filter.getContentSearch().get(ContentType.TRANSFORMED));
        params.put("encodedSearch", filter.getContentSearch().get(ContentType.ENCODED));
        params.put("sentSearch", filter.getContentSearch().get(ContentType.SENT));
        params.put("responseSearch", filter.getContentSearch().get(ContentType.RESPONSE));
        params.put("responseTransformedSearch", filter.getContentSearch().get(ContentType.RESPONSE_TRANSFORMED));
        params.put("processedResponseSearch", filter.getContentSearch().get(ContentType.PROCESSED_RESPONSE));
        params.put("includedMetaDataIds", filter.getIncludedMetaDataIds());
        params.put("excludedMetaDataIds", filter.getExcludedMetaDataIds());
        params.put("serverId", filter.getServerId());
        params.put("maxMessageId", filter.getMaxMessageId());
        params.put("metaDataSearch", filter.getMetaDataSearch());
        params.put("quickSearchMetaDataColumns", filter.getQuickSearchMetaDataColumns());
        params.put("attachment", filter.getAttachment());

        return params;
    }

    @Override
    public long getMaxMessageId(String channelId) {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            return dao.getMaxMessageId(channelId);
        } finally {
            dao.close();
        }
    }

    @Override
    public Long getMessageCount(MessageFilter filter, Channel channel) {
        if (filter.getIncludedMetaDataIds() != null && filter.getIncludedMetaDataIds().isEmpty() && filter.getExcludedMetaDataIds() == null) {
            return 0L;
        }

        return SqlConfig.getSqlSessionManager().selectOne("Message.searchMessagesCount", getParameters(filter, channel.getChannelId(), null, null));
    }

    @Override
    public List<Message> getMessages(MessageFilter filter, Channel channel, Boolean includeContent, Integer offset, Integer limit) {
        List<Message> messages = new ArrayList<Message>();
        SqlSession session = SqlConfig.getSqlSessionManager();
        String channelId = channel.getChannelId();

        if (filter.getIncludedMetaDataIds() != null && filter.getIncludedMetaDataIds().isEmpty() && filter.getExcludedMetaDataIds() == null) {
            return messages;
        }

        Map<String, Object> params = getParameters(filter, channelId, offset, limit);
        List<MessageSearchResult> results = searchMessages(session, params);

        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

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
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

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
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            return dao.getMessageAttachment(channelId, attachmentId);
        } finally {
            dao.close();
        }
    }

    @Override
    public List<Attachment> getMessageAttachment(String channelId, Long messageId) {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            return dao.getMessageAttachment(channelId, messageId);
        } finally {
            dao.close();
        }
    }

    @Override
    public int removeMessages(String channelId, MessageFilter filter) {
        // Perform the deletes in batches rather than all in one transaction.
        //TODO Tune the limit to use for each batch in the delete.
        Map<String, Object> params = getParameters(filter, channelId, 0, 100000);

        Channel channel = ControllerFactory.getFactory().createEngineController().getDeployedChannel(channelId);
        if (channel != null) {
            List<MessageSearchResult> results = null;
            Long maxMessageId = filter.getMaxMessageId();
            do {
                // Prevent the delete from occurring at the same time as the channel being started. 
                synchronized (channel) {
                    params.put("maxMessageId", maxMessageId);

                    // Perform a search using the message filter parameters
                    results = searchMessages(SqlConfig.getSqlSessionManager(), params);
                    Map<Long, Set<Integer>> messages = new HashMap<Long, Set<Integer>>();

                    // For each message that was retrieved
                    for (MessageSearchResult result : results) {
                        Long messageId = result.getMessageId();
                        Set<Integer> metaDataIds = result.getMetaDataIdSet();
                        boolean processed = result.isProcessed();

                        if (maxMessageId == null || maxMessageId >= messageId) {
                            maxMessageId = messageId - 1;
                        }

                        // Allow unprocessed messages to be deleted only if the channel is stopped.
                        if (channel.getCurrentState() == ChannelState.STOPPED || processed) {
                            if (metaDataIds.contains(0)) {
                                // Delete the entire message if the source connector message is to be deleted
                                messages.put(messageId, null);
                            } else {
                                // Otherwise only deleted the destination connector message
                                messages.put(messageId, metaDataIds);
                            }
                        }
                    }

                    com.mirth.connect.donkey.server.controllers.MessageController.getInstance().deleteMessages(channelId, messages, false);
                }
            } while (results.size() > 0);

            // Invalidate the queue buffer to ensure stats are updated.
            channel.invalidateQueues();
        }
        //TODO Decide what to return
        return 0;
    }

    @Override
    public void clearMessages(Set<String> channelIds, Boolean restartRunningChannels, Boolean clearStatistics) throws ControllerException {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            EngineController engineController = ControllerFactory.getFactory().createEngineController();
            Set<Status> statuses = null;

            if (clearStatistics) {
                statuses = new HashSet<Status>();

                for (Status status : Status.values()) {
                    if (!status.equals(Status.QUEUED)) {
                        statuses.add(status);
                    }
                }
            }

            for (String channelId : channelIds) {
                Channel channel = engineController.getDeployedChannel(channelId);

                if (channel != null) {
                    ChannelState priorState = channel.getCurrentState();
                    boolean startChannelAfter = false;

                    if (priorState != ChannelState.STOPPED && restartRunningChannels) {
                        try {
                            logger.debug("Stopping channel \"" + channel.getName() + "\" prior to removing messages");
                            channel.stop();

                            if (priorState != ChannelState.STOPPING) {
                                startChannelAfter = true;
                            }
                        } catch (StopException e) {
                            logger.error("Failed to stop channel id " + channelId, e);
                        }
                    }

                    // Prevent the delete from occurring at the same time as the channel being started. 
                    synchronized (channel) {
                        // Only allow the messages to be cleared if the channel is stopped.
                        if (channel.getCurrentState() == ChannelState.STOPPED) {
                            logger.debug("Removing messages for channel \"" + channel.getName() + "\"");
                            dao.deleteAllMessages(channelId);

                            if (clearStatistics) {
                                logger.debug("Clearing statistics for channel \"" + channel.getName() + "\"");
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
                            channel.start(priorState != ChannelState.PAUSED && priorState != ChannelState.PAUSING);
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

    @Override
    public void reprocessMessages(String channelId, MessageFilter filter, boolean replace, List<Integer> reprocessMetaDataIds) {
        EngineController engineController = ControllerFactory.getFactory().createEngineController();
        DataType dataType = engineController.getDeployedChannel(channelId).getSourceConnector().getInboundDataType();
        Encryptor encryptor = ConfigurationController.getInstance().getEncryptor();

        Map<String, Object> params = getParameters(filter, channelId, null, null);
        params.put("localChannelId", ChannelController.getInstance().getLocalChannelId(channelId));

        List<Long> messageIds = SqlConfig.getSqlSessionManager().selectList("Message.selectMessageIdsForReprocessing", params);

        params.clear();
        params.put("localChannelId", ChannelController.getInstance().getLocalChannelId(channelId));

        for (Long messageId : messageIds) {
            params.put("messageId", messageId);
            MessageContent rawContent = SqlConfig.getSqlSessionManager().selectOne("Message.selectMessageForReprocessing", params);

            if (rawContent.isEncrypted()) {
                rawContent.setContent(encryptor.decrypt(rawContent.getContent()));
                rawContent.setEncrypted(false);
            }

            if (rawContent != null) {
                ConnectorMessage connectorMessage = new ConnectorMessage();
                connectorMessage.setChannelId(channelId);
                connectorMessage.setMessageId(messageId);
                connectorMessage.setMetaDataId(0);
                connectorMessage.setRaw(rawContent);

                RawMessage rawMessage = null;

                if (ExtensionController.getInstance().getDataTypePlugins().get(dataType.getType()).isBinary()) {
                    rawMessage = new RawMessage(DICOMUtil.getDICOMRawBytes(connectorMessage));
                } else {
                    rawMessage = new RawMessage(org.apache.commons.codec.binary.StringUtils.newString(AttachmentUtil.reAttachMessage(rawContent.getContent(), connectorMessage, Constants.ATTACHMENT_CHARSET, false), Constants.ATTACHMENT_CHARSET));
                }

                if (replace) {
                    rawMessage.setMessageIdToOverwrite(messageId);
                }

                rawMessage.setDestinationMetaDataIds(reprocessMetaDataIds);

                try {
                    engineController.dispatchRawMessage(channelId, rawMessage);
                } catch (Throwable e) {
                    // Do nothing. An error should have been logged.
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
            MessageWriter messageWriter = MessageWriterFactory.getInstance().getMessageWriter(options, ConfigurationController.getInstance().getEncryptor(), channelId);
            return new MessageExporter().exportMessages(messageList, messageWriter).getNumExported();
        } catch (MessageWriterException e) {
            throw new MessageExportException(e);
        }
    }

    @Override
    public void importMessage(String channelId, Message message) throws MessageImportException {
        try {
            MessageEncryptionUtil.decryptMessage(message, ConfigurationController.getInstance().getEncryptor());
            Channel channel = Donkey.getInstance().getDeployedChannels().get(channelId);

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
    public int[] importMessagesServer(final String channelId, final String uri, boolean includeSubfolders) throws MessageImportException, InterruptedException {
        Channel channel = Donkey.getInstance().getDeployedChannels().get(channelId);

        if (channel == null) {
            throw new MessageImportException("Failed to import message, channel ID " + channelId + " is not currently deployed");
        }

        MessageWriter messageWriter = new MessageWriterChannel(channel);
        return new MessageImporter().importMessages(uri, includeSubfolders, messageWriter);
    }

    private List<MessageSearchResult> searchMessages(SqlSession session, Map<String, Object> params) {
        long startTime = System.currentTimeMillis();

        try {
            if (DatabaseUtil.statementExists("Message.searchMessages")) {
                return session.selectList("Message.searchMessages", params);
            }

            List<MessageSearchResult> results = session.selectList("Message.searchMessagesUngrouped", params);
            Map<Long, MessageSearchResult> groupedResults = new HashMap<Long, MessageSearchResult>();
            List<MessageSearchResult> orderedResults = new ArrayList<MessageSearchResult>();

            for (MessageSearchResult result : results) {
                MessageSearchResult groupedResult = groupedResults.get(result.getMessageId());

                if (groupedResult == null) {
                    groupedResult = result;
                    groupedResult.setMetaDataIdSet(new HashSet<Integer>());
                    groupedResults.put(groupedResult.getMessageId(), groupedResult);

                    orderedResults.add(groupedResult);
                }

                groupedResult.getMetaDataIdSet().add(result.getMetaDataId());
            }

            return orderedResults;
        } finally {
            long endTime = System.currentTimeMillis();
            logger.debug("Search executed in " + (endTime - startTime) + "ms");
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
}
