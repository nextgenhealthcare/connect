/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data.timed;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.util.ActionTimer;

public class TimedDao implements DonkeyDao {
    private DonkeyDao dao;
    private ActionTimer timer;

    protected TimedDao(DonkeyDao dao, ActionTimer timer) {
        this.dao = dao;
        this.timer = timer;
    }

    public DonkeyDao getDao() {
        return dao;
    }

    public void setDao(DonkeyDao dao) {
        this.dao = dao;
    }

    public ActionTimer getTimer() {
        return timer;
    }

    public void setTimer(ActionTimer timer) {
        this.timer = timer;
    }

    @Override
    public void setEncryptData(boolean encryptData) {
        dao.setEncryptData(encryptData);
    }

    @Override
    public void setDecryptData(boolean decryptData) {
        dao.setDecryptData(decryptData);
    }

    @Override
    public void commit() {
        dao.commit();
    }

    @Override
    public void commit(boolean durable) {
        dao.commit(durable);
    }

    @Override
    public void rollback() {
        dao.rollback();
    }

    @Override
    public void close() {
        dao.close();
    }

    @Override
    public boolean isClosed() {
        return dao.isClosed();
    }

    @Override
    public void insertMessage(Message message) {
        long startTime = System.currentTimeMillis();

        try {
            dao.insertMessage(message);
        } finally {
            timer.log("insertMessage", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void insertConnectorMessage(ConnectorMessage connectorMessage, boolean storeMaps, boolean updateStats) {
        long startTime = System.currentTimeMillis();

        try {
            dao.insertConnectorMessage(connectorMessage, storeMaps, updateStats);
        } finally {
            timer.log("insertConnectorMessage", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void insertMessageContent(MessageContent messageContent) {
        long startTime = System.currentTimeMillis();

        try {
            dao.insertMessageContent(messageContent);
        } finally {
            timer.log("insertMessageContent", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void batchInsertMessageContent(MessageContent messageContent) {
        long startTime = System.currentTimeMillis();

        try {
            dao.batchInsertMessageContent(messageContent);
        } finally {
            timer.log("batchInsertMessageContent", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void executeBatchInsertMessageContent(String channelId) {
        long startTime = System.currentTimeMillis();

        try {
            dao.executeBatchInsertMessageContent(channelId);
        } finally {
            timer.log("executeBatchInsertMessageContent", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void insertMessageAttachment(String channelId, long messageId, Attachment attachment) {
        long startTime = System.currentTimeMillis();

        try {
            dao.insertMessageAttachment(channelId, messageId, attachment);
        } finally {
            timer.log("insertMessageAttachment", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void insertMetaData(ConnectorMessage connectorMessage, List<MetaDataColumn> metaDataColumns) {
        long startTime = System.currentTimeMillis();

        try {
            dao.insertMetaData(connectorMessage, metaDataColumns);
        } finally {
            timer.log("insertMetaData", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void storeMetaData(ConnectorMessage connectorMessage, List<MetaDataColumn> metaDataColumns) {
        long startTime = System.currentTimeMillis();

        try {
            dao.storeMetaData(connectorMessage, metaDataColumns);
        } finally {
            timer.log("storeMetaData", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void storeMessageContent(MessageContent messageContent) {
        long startTime = System.currentTimeMillis();

        try {
            dao.storeMessageContent(messageContent);
        } finally {
            timer.log("storeMessageContent", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void addChannelStatistics(Statistics statistics) {
        long startTime = System.currentTimeMillis();

        try {
            dao.addChannelStatistics(statistics);
        } finally {
            timer.log("addChannelStatistics", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void updateSendAttempts(ConnectorMessage connectorMessage) {
        long startTime = System.currentTimeMillis();

        try {
            dao.updateSendAttempts(connectorMessage);
        } finally {
            timer.log("updateResponseError", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void updateStatus(ConnectorMessage connectorMessage, Status previousStatus) {
        long startTime = System.currentTimeMillis();

        try {
            dao.updateStatus(connectorMessage, previousStatus);
        } finally {
            timer.log("updateStatus", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void updateErrors(ConnectorMessage connectorMessage) {
        long startTime = System.currentTimeMillis();

        try {
            dao.updateErrors(connectorMessage);
        } finally {
            timer.log("updateErrors", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void updateMaps(ConnectorMessage connectorMessage) {
        long startTime = System.currentTimeMillis();

        try {
            dao.updateMaps(connectorMessage);
        } finally {
            timer.log("updateMaps", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void updateSourceMap(ConnectorMessage connectorMessage) {
        long startTime = System.currentTimeMillis();

        try {
            dao.updateSourceMap(connectorMessage);
        } finally {
            timer.log("updateSourceMap", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void updateResponseMap(ConnectorMessage connectorMessage) {
        long startTime = System.currentTimeMillis();

        try {
            dao.updateResponseMap(connectorMessage);
        } finally {
            timer.log("updateResponseMap", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void markAsProcessed(String channelId, long messageId) {
        long startTime = System.currentTimeMillis();

        try {
            dao.markAsProcessed(channelId, messageId);
        } finally {
            timer.log("markAsProcessed", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void resetMessage(String channelId, long messageId) {
        long startTime = System.currentTimeMillis();

        try {
            dao.resetMessage(channelId, messageId);
        } finally {
            timer.log("resetMessage", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void deleteMessage(String channelId, long messageId) {
        long startTime = System.currentTimeMillis();

        try {
            dao.deleteMessage(channelId, messageId);
        } finally {
            timer.log("deleteMessage", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void deleteConnectorMessages(String channelId, long messageId, Set<Integer> metaDataIds) {
        long startTime = System.currentTimeMillis();

        try {
            dao.deleteConnectorMessages(channelId, messageId, metaDataIds);
        } finally {
            timer.log("deleteConnectorMessages", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void deleteMessageStatistics(String channelId, long messageId, Set<Integer> metaDataIds) {
        long startTime = System.currentTimeMillis();

        try {
            dao.deleteMessageStatistics(channelId, messageId, metaDataIds);
        } finally {
            timer.log("deleteMessageStatistics", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void deleteAllMessages(String channelId) {
        long startTime = System.currentTimeMillis();

        try {
            dao.deleteAllMessages(channelId);
        } finally {
            timer.log("deleteAllMessages", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void deleteMessageContent(String channelId, long messageId) {
        long startTime = System.currentTimeMillis();

        try {
            dao.deleteMessageContent(channelId, messageId);
        } finally {
            timer.log("deleteMessageContent", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void deleteMessageAttachments(String channelId, long messageId) {
        long startTime = System.currentTimeMillis();

        try {
            dao.deleteMessageAttachments(channelId, messageId);
        } finally {
            timer.log("deleteMessageAttachments", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void createChannel(String channelId, long localChannelId) {
        long startTime = System.currentTimeMillis();

        try {
            dao.createChannel(channelId, localChannelId);
        } finally {
            timer.log("createChannel", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void removeChannel(String channelId) {
        long startTime = System.currentTimeMillis();

        try {
            dao.removeChannel(channelId);
        } finally {
            timer.log("removeChannel", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void addMetaDataColumn(String channelId, MetaDataColumn metaDataColumn) {
        long startTime = System.currentTimeMillis();

        try {
            dao.addMetaDataColumn(channelId, metaDataColumn);
        } finally {
            timer.log("addMetaDataColumn", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void removeMetaDataColumn(String channelId, String columnName) {
        long startTime = System.currentTimeMillis();

        try {
            dao.removeMetaDataColumn(channelId, columnName);
        } finally {
            timer.log("removeMetaDataColumn", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void resetStatistics(String channelId, Integer metaDataId, Set<Status> statuses) {
        long startTime = System.currentTimeMillis();

        try {
            dao.resetStatistics(channelId, metaDataId, statuses);
        } finally {
            timer.log("resetStatistics", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void resetAllStatistics(String channelId) {
        long startTime = System.currentTimeMillis();

        try {
            dao.resetAllStatistics(channelId);
        } finally {
            timer.log("resetStatistics", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public Long selectMaxLocalChannelId() {
        long startTime = System.currentTimeMillis();

        try {
            return dao.selectMaxLocalChannelId();
        } finally {
            timer.log("selectMaxLocalChannelId", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public Map<String, Long> getLocalChannelIds() {
        long startTime = System.currentTimeMillis();

        try {
            return dao.getLocalChannelIds();
        } finally {
            timer.log("getLocalChannelIds", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public long getMaxMessageId(String channelId) {
        long startTime = System.currentTimeMillis();

        try {
            return dao.getMaxMessageId(channelId);
        } finally {
            timer.log("getMaxMessageId", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public long getMinMessageId(String channelId) {
        long startTime = System.currentTimeMillis();

        try {
            return dao.getMinMessageId(channelId);
        } finally {
            timer.log("getMinMessageId", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public long getNextMessageId(String channelId) {
        long startTime = System.currentTimeMillis();

        try {
            return dao.getNextMessageId(channelId);
        } finally {
            timer.log("getNextMessageId", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public List<Attachment> getMessageAttachment(String channelId, long messageId) {
        long startTime = System.currentTimeMillis();

        try {
            return dao.getMessageAttachment(channelId, messageId);
        } finally {
            timer.log("getMessageAttachment", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public Attachment getMessageAttachment(String channelId, String attachmentId) {
        long startTime = System.currentTimeMillis();

        try {
            return dao.getMessageAttachment(channelId, attachmentId);
        } finally {
            timer.log("getMessageAttachment", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public List<Message> getPendingConnectorMessages(String channelId, String serverId, int limit, Long minMessageId) {
        long startTime = System.currentTimeMillis();

        try {
            return dao.getPendingConnectorMessages(channelId, serverId, limit, minMessageId);
        } finally {
            timer.log("getPendingConnectorMessages", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public List<ConnectorMessage> getConnectorMessages(String channelId, String serverId, int metaDataId, Status status, int offset, int limit, Long minMessageId, Long maxMessageId) {
        long startTime = System.currentTimeMillis();

        try {
            return dao.getConnectorMessages(channelId, serverId, metaDataId, status, offset, limit, minMessageId, maxMessageId);
        } finally {
            timer.log("getConnectorMessages", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public List<ConnectorMessage> getConnectorMessages(String channelId, long messageId, Set<Integer> metaDataIds, boolean includeContent) {
        long startTime = System.currentTimeMillis();

        try {
            return dao.getConnectorMessages(channelId, messageId, metaDataIds, includeContent);
        } finally {
            timer.log("getConnectorMessages", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public Map<Integer, ConnectorMessage> getConnectorMessages(String channelId, long messageId) {
        long startTime = System.currentTimeMillis();

        try {
            return dao.getConnectorMessages(channelId, messageId);
        } finally {
            timer.log("getConnectorMessages", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public int getConnectorMessageCount(String channelId, String serverId, int metaDataId, Status status) {
        long startTime = System.currentTimeMillis();

        try {
            return dao.getConnectorMessageCount(channelId, serverId, metaDataId, status);
        } finally {
            timer.log("getConnectorMessageCount", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public long getConnectorMessageMaxMessageId(String channelId, String serverId, int metaDataId, Status status) {
        long startTime = System.currentTimeMillis();

        try {
            return dao.getConnectorMessageMaxMessageId(channelId, serverId, metaDataId, status);
        } finally {
            timer.log("getConnectorMessageMaxMessageId", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public Set<Status> getConnectorMessageStatuses(String channelId, long messageId, boolean checkProcessed) {
        long startTime = System.currentTimeMillis();

        try {
            return dao.getConnectorMessageStatuses(channelId, messageId, checkProcessed);
        } finally {
            timer.log("getConnectorMessageStatuses", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public List<Message> getUnfinishedMessages(String channelId, String serverId, int limit, Long minMessageId) {
        long startTime = System.currentTimeMillis();

        try {
            return dao.getUnfinishedMessages(channelId, serverId, limit, minMessageId);
        } finally {
            timer.log("getUnfinishedMessages", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public List<MetaDataColumn> getMetaDataColumns(String channelId) {
        long startTime = System.currentTimeMillis();

        try {
            return dao.getMetaDataColumns(channelId);
        } finally {
            timer.log("getMetaDataColumns", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public Statistics getChannelStatistics(String serverId) {
        long startTime = System.currentTimeMillis();

        try {
            return dao.getChannelStatistics(serverId);
        } finally {
            timer.log("getChannelStatistics", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public Statistics getChannelTotalStatistics(String serverId) {
        long startTime = System.currentTimeMillis();

        try {
            return dao.getChannelTotalStatistics(serverId);
        } finally {
            timer.log("getChannelTotalStatistics", System.currentTimeMillis() - startTime);
        }
    }
}
