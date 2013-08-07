/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data.buffered;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;

public class BufferedDao implements DonkeyDao {
    private DonkeyDaoFactory daoFactory;
    private boolean encryptData;
    private boolean decryptData;
    private Queue<DaoTask> tasks = new LinkedList<DaoTask>();
    private boolean closed = false;
    private Logger logger = Logger.getLogger(this.getClass());

    protected BufferedDao(DonkeyDaoFactory daoFactory, boolean encryptData, boolean decryptData) {
        this.daoFactory = daoFactory;
        this.encryptData = encryptData;
        this.decryptData = decryptData;
    }

    @Override
    public void setEncryptData(boolean encryptData) {
        this.encryptData = encryptData;
    }

    @Override
    public void setDecryptData(boolean decryptData) {
        this.decryptData = decryptData;
    }

    @Override
    public void commit() {
        commit(null);
    }

    @Override
    public void commit(boolean durable) {
        commit(new Boolean(durable));
    }

    private void commit(Boolean durable) {
        if (closed) {
            logger.error("Failed to commit, the storage buffer has been closed");
            return;
        }

        if (tasks.isEmpty()) {
            return;
        }

        executeTasks(durable);
    }

    private DonkeyDao getDelegateDao() {
        DonkeyDao dao = daoFactory.getDao();

        dao.setEncryptData(encryptData);
        dao.setDecryptData(decryptData);

        return dao;
    }

    private void executeTasks(Boolean durable) {
        DonkeyDao dao = getDelegateDao();

        try {
            while (!tasks.isEmpty()) {
                DaoTask task = tasks.poll();
                Object[] p = task.getParameters();

                // @formatter:off
                switch (task.getTaskType()) {
                    case INSERT_MESSAGE: dao.insertMessage((Message) p[0]); break;
                    case INSERT_CONNECTOR_MESSAGE: dao.insertConnectorMessage((ConnectorMessage) p[0], (Boolean) p[1]); break;
                    case INSERT_MESSAGE_CONTENT: dao.insertMessageContent((MessageContent) p[0]); break;
                    case BATCH_INSERT_MESSAGE_CONTENT: dao.batchInsertMessageContent((MessageContent) p[0]); break;
                    case EXECUTE_BATCH_INSERT_MESSAGE_CONTENT: dao.executeBatchInsertMessageContent((String) p[0]); break;
                    case INSERT_MESSAGE_ATTACHMENT: dao.insertMessageAttachment((String) p[0], (Long) p[1], (Attachment) p[2]); break;
                    case INSERT_META_DATA: dao.insertMetaData((ConnectorMessage) p[0], (List<MetaDataColumn>) p[1]); break;
                    case STORE_MESSAGE_CONTENT: dao.storeMessageContent((MessageContent) p[0]); break;
                    case STORE_CHANNEL_STATISTICS: dao.addChannelStatistics((Statistics) p[0]); break;
                    case UPDATE_RESPONSE_ERROR: dao.updateSourceResponse((ConnectorMessage) p[0]); break;
                    case UPDATE_STATUS: dao.updateStatus((ConnectorMessage) p[0], (Status) p[1]); break;
                    case UPDATE_ERRORS: dao.updateErrors((ConnectorMessage) p[0]); break;
                    case UPDATE_MAPS: dao.updateMaps((ConnectorMessage) p[0]); break;
                    case UPDATE_RESPONSE_MAP: dao.updateResponseMap((ConnectorMessage) p[0]); break;
                    case MARK_AS_PROCESSED: dao.markAsProcessed((String) p[0], (Long) p[1]); break;
                    case RESET_MESSAGE: dao.resetMessage((String) p[0], (Long) p[1]); break;
                    case DELETE_MESSAGE: dao.deleteMessage((String) p[0], (Long) p[1], (Boolean) p[2]); break;
                    case DELETE_CONNECTOR_MESSAGES: dao.deleteConnectorMessages((String) p[0], (Long) p[1], (List<Integer>) p[2], (Boolean) p[3]); break;
                    case DELETE_ALL_MESSAGES: dao.deleteAllMessages((String) p[0]); break;
                    case DELETE_MESSAGE_CONTENT: dao.deleteMessageContent((String) p[0], (Long) p[1]); break;
                    case DELETE_MESSAGE_ATTACHMENTS: dao.deleteMessageAttachments((String) p[0], (Long) p[1]); break;
                    case CREATE_CHANNEL: dao.createChannel((String) p[0], (Long) p[1]); break;
                    case REMOVE_CHANNEL: dao.removeChannel((String) p[0]); break;
                    case ADD_META_DATA_COLUMN: dao.addMetaDataColumn((String) p[0], (MetaDataColumn) p[1]); break;
                    case REMOVE_META_DATA_COLUMN: dao.removeMetaDataColumn((String) p[0], (String) p[1]); break;
                    case RESET_STATISTICS: dao.resetStatistics((String) p[0], (Integer) p[1], (Set<Status>) p[2]); break;
                    case RESET_ALL_STATISTICS: dao.resetAllStatistics((String) p[0]); break;
                }
                // @formatter:on
            }

            if (durable == null) {
                dao.commit();
            } else {
                dao.commit(durable);
            }
        } finally {
            if (dao != null) {
                dao.close();
            }
        }
    }

    @Override
    public void rollback() {
        tasks.clear();
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void insertMessage(Message message) {
        tasks.add(new DaoTask(DaoTaskType.INSERT_MESSAGE, new Object[] { message }));
    }

    @Override
    public void insertConnectorMessage(ConnectorMessage connectorMessage, boolean storeMaps) {
        tasks.add(new DaoTask(DaoTaskType.INSERT_CONNECTOR_MESSAGE, new Object[] {
                connectorMessage, storeMaps }));
    }

    @Override
    public void insertMessageContent(MessageContent messageContent) {
        tasks.add(new DaoTask(DaoTaskType.INSERT_MESSAGE_CONTENT, new Object[] { messageContent }));
    }

    @Override
    public void batchInsertMessageContent(MessageContent messageContent) {
        tasks.add(new DaoTask(DaoTaskType.BATCH_INSERT_MESSAGE_CONTENT, new Object[] { messageContent }));
    }

    @Override
    public void executeBatchInsertMessageContent(String channelId) {
        tasks.add(new DaoTask(DaoTaskType.EXECUTE_BATCH_INSERT_MESSAGE_CONTENT, new Object[] { channelId }));
    }

    @Override
    public void insertMessageAttachment(String channelId, long messageId, Attachment attachment) {
        tasks.add(new DaoTask(DaoTaskType.INSERT_MESSAGE_ATTACHMENT, new Object[] { channelId,
                messageId, attachment }));
    }

    @Override
    public void insertMetaData(ConnectorMessage connectorMessage, List<MetaDataColumn> metaDataColumns) {
        tasks.add(new DaoTask(DaoTaskType.INSERT_META_DATA, new Object[] { connectorMessage,
                metaDataColumns }));
    }

    @Override
    public void storeMessageContent(MessageContent messageContent) {
        tasks.add(new DaoTask(DaoTaskType.STORE_MESSAGE_CONTENT, new Object[] { messageContent }));
    }

    @Override
    public void addChannelStatistics(Statistics statistics) {
        tasks.add(new DaoTask(DaoTaskType.STORE_CHANNEL_STATISTICS, new Object[] { statistics }));
    }

    @Override
    public void updateSourceResponse(ConnectorMessage connectorMessage) {
        tasks.add(new DaoTask(DaoTaskType.UPDATE_RESPONSE_ERROR, new Object[] { connectorMessage }));
    }

    @Override
    public void updateStatus(ConnectorMessage connectorMessage, Status previousStatus) {
        tasks.add(new DaoTask(DaoTaskType.UPDATE_STATUS, new Object[] { connectorMessage,
                previousStatus }));
    }

    @Override
    public void updateErrors(ConnectorMessage connectorMessage) {
        tasks.add(new DaoTask(DaoTaskType.UPDATE_ERRORS, new Object[] { connectorMessage }));
    }

    @Override
    public void updateMaps(ConnectorMessage connectorMessage) {
        tasks.add(new DaoTask(DaoTaskType.UPDATE_MAPS, new Object[] { connectorMessage }));
    }

    @Override
    public void updateResponseMap(ConnectorMessage connectorMessage) {
        tasks.add(new DaoTask(DaoTaskType.UPDATE_RESPONSE_MAP, new Object[] { connectorMessage }));
    }

    @Override
    public void markAsProcessed(String channelId, long messageId) {
        tasks.add(new DaoTask(DaoTaskType.MARK_AS_PROCESSED, new Object[] { channelId, messageId }));
    }

    @Override
    public void resetMessage(String channelId, long messageId) {
        tasks.add(new DaoTask(DaoTaskType.RESET_MESSAGE, new Object[] { channelId, messageId }));
    }

    @Override
    public void deleteMessage(String channelId, long messageId, boolean deleteStatistics) {
        tasks.add(new DaoTask(DaoTaskType.DELETE_MESSAGE, new Object[] { channelId, messageId,
                deleteStatistics }));
    }

    @Override
    public void deleteConnectorMessages(String channelId, long messageId, List<Integer> metaDataIds, boolean deleteStatistics) {
        tasks.add(new DaoTask(DaoTaskType.DELETE_CONNECTOR_MESSAGES, new Object[] { channelId,
                messageId, metaDataIds, deleteStatistics }));
    }

    @Override
    public void deleteAllMessages(String channelId) {
        tasks.add(new DaoTask(DaoTaskType.DELETE_ALL_MESSAGES, new Object[] { channelId }));
    }

    @Override
    public void deleteMessageContent(String channelId, long messageId) {
        tasks.add(new DaoTask(DaoTaskType.DELETE_MESSAGE_CONTENT, new Object[] { channelId,
                messageId }));
    }

    @Override
    public void deleteMessageAttachments(String channelId, long messageId) {
        tasks.add(new DaoTask(DaoTaskType.DELETE_MESSAGE_ATTACHMENTS, new Object[] { channelId,
                messageId }));
    }

    @Override
    public void createChannel(String channelId, long localChannelId) {
        tasks.add(new DaoTask(DaoTaskType.CREATE_CHANNEL, new Object[] { channelId, localChannelId }));
    }

    @Override
    public void removeChannel(String channelId) {
        tasks.add(new DaoTask(DaoTaskType.REMOVE_CHANNEL, new Object[] { channelId }));
    }

    @Override
    public void addMetaDataColumn(String channelId, MetaDataColumn metaDataColumn) {
        tasks.add(new DaoTask(DaoTaskType.ADD_META_DATA_COLUMN, new Object[] { channelId,
                metaDataColumn }));
    }

    @Override
    public void removeMetaDataColumn(String channelId, String columnName) {
        tasks.add(new DaoTask(DaoTaskType.REMOVE_META_DATA_COLUMN, new Object[] { channelId,
                columnName }));
    }

    @Override
    public void resetStatistics(String channelId, Integer metaDataId, Set<Status> statuses) {
        tasks.add(new DaoTask(DaoTaskType.RESET_STATISTICS, new Object[] { channelId, metaDataId,
                statuses }));
    }
    
    @Override
    public void resetAllStatistics(String channelId) {
        tasks.add(new DaoTask(DaoTaskType.RESET_ALL_STATISTICS, new Object[] { channelId }));
    }

    @Override
    public Map<String, Long> getLocalChannelIds() {
        DonkeyDao dao = getDelegateDao();

        try {
            return dao.getLocalChannelIds();
        } finally {
            dao.close();
        }
    }

    @Override
    public Long selectMaxLocalChannelId() {
        DonkeyDao dao = getDelegateDao();

        try {
            return dao.selectMaxLocalChannelId();
        } finally {
            dao.close();
        }
    }

    @Override
    public long getMaxMessageId(String channelId) {
        DonkeyDao dao = getDelegateDao();

        try {
            return dao.getMaxMessageId(channelId);
        } finally {
            dao.close();
        }
    }

    @Override
    public long getNextMessageId(String channelId) {
        DonkeyDao dao = getDelegateDao();

        try {
            long id = dao.getNextMessageId(channelId);
            dao.commit();
            return id;
        } finally {
            dao.close();
        }
    }

    @Override
    public List<Attachment> getMessageAttachment(String channelId, long messageId) {
        DonkeyDao dao = getDelegateDao();

        try {
            return dao.getMessageAttachment(channelId, messageId);
        } finally {
            dao.close();
        }
    }

    @Override
    public Attachment getMessageAttachment(String channelId, String attachmentId) {
        DonkeyDao dao = getDelegateDao();

        try {
            return dao.getMessageAttachment(channelId, attachmentId);
        } finally {
            dao.close();
        }
    }

    @Override
    public List<ConnectorMessage> getUnfinishedConnectorMessages(String channelId, String serverId, int metaDataId, Status status) {
        DonkeyDao dao = getDelegateDao();

        try {
            return dao.getUnfinishedConnectorMessages(channelId, serverId, metaDataId, status);
        } finally {
            dao.close();
        }
    }

    @Override
    public List<ConnectorMessage> getConnectorMessages(String channelId, String serverId, int metaDataId, Status status, int offset, int limit, Long minMessageId, Long maxMessageId) {
        DonkeyDao dao = getDelegateDao();

        try {
            return dao.getConnectorMessages(channelId, serverId, metaDataId, status, offset, limit, minMessageId, maxMessageId);
        } finally {
            dao.close();
        }
    }

    @Override
    public List<ConnectorMessage> getConnectorMessages(String channelId, long messageId, Set<Integer> metaDataIds, boolean includeContent) {
        DonkeyDao dao = getDelegateDao();

        try {
            return dao.getConnectorMessages(channelId, messageId, metaDataIds, includeContent);
        } finally {
            dao.close();
        }
    }

    @Override
    public Map<Integer, ConnectorMessage> getConnectorMessages(String channelId, long messageId) {
        DonkeyDao dao = getDelegateDao();

        try {
            return dao.getConnectorMessages(channelId, messageId);
        } finally {
            dao.close();
        }
    }

    @Override
    public int getConnectorMessageCount(String channelId, String serverId, int metaDataId, Status status) {
        DonkeyDao dao = getDelegateDao();

        try {
            return dao.getConnectorMessageCount(channelId, serverId, metaDataId, status);
        } finally {
            dao.close();
        }
    }

    public long getConnectorMessageMaxMessageId(String channelId, String serverId, int metaDataId, Status status) {
        DonkeyDao dao = getDelegateDao();

        try {
            return dao.getConnectorMessageMaxMessageId(channelId, serverId, metaDataId, status);
        } finally {
            dao.close();
        }
    }

    @Override
    public List<Message> getUnfinishedMessages(String channelId, String serverId) {
        DonkeyDao dao = getDelegateDao();

        try {
            return dao.getUnfinishedMessages(channelId, serverId);
        } finally {
            dao.close();
        }
    }

    @Override
    public List<MetaDataColumn> getMetaDataColumns(String channelId) {
        DonkeyDao dao = getDelegateDao();

        try {
            return dao.getMetaDataColumns(channelId);
        } finally {
            dao.close();
        }
    }

    @Override
    public Statistics getChannelStatistics(String serverId) {
        DonkeyDao dao = getDelegateDao();

        try {
            return dao.getChannelStatistics(serverId);
        } finally {
            dao.close();
        }
    }

    @Override
    public Statistics getChannelTotalStatistics(String serverId) {
        DonkeyDao dao = getDelegateDao();

        try {
            return dao.getChannelTotalStatistics(serverId);
        } finally {
            dao.close();
        }
    }
}
