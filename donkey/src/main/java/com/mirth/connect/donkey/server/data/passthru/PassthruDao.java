/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data.passthru;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.event.Event;

public class PassthruDao implements DonkeyDao {
    private boolean closed = false;
    private Statistics transactionStats = new Statistics();
    private Statistics currentStats;
    private Statistics totalStats;
    private Map<String, Map<Integer, Set<Status>>> resetStats = new HashMap<String, Map<Integer, Set<Status>>>();
    private List<String> removedChannelIds = new ArrayList<String>();
    private StatisticsUpdater statisticsUpdater;

    protected PassthruDao() {
        ChannelController channelController = ChannelController.getInstance();
        currentStats = channelController.getStatistics();
        totalStats = channelController.getTotalStatistics();

        // make sure these aren't null, otherwise commit() will break
        if (currentStats == null) {
            currentStats = new Statistics();
        }

        if (totalStats == null) {
            totalStats = new Statistics();
        }
    }

    public StatisticsUpdater getStatisticsUpdater() {
        return statisticsUpdater;
    }

    public void setStatisticsUpdater(StatisticsUpdater statisticsUpdater) {
        this.statisticsUpdater = statisticsUpdater;
    }

    @Override
    public void setEncryptData(boolean encryptData) {}

    @Override
    public void setDecryptData(boolean decryptData) {}

    @Override
    public void commit() {
        commit(false);
    }

    @Override
    public void commit(boolean durable) {
        synchronized (currentStats) {
            // reset stats for any connectors that need to be reset
            for (Entry<String, Map<Integer, Set<Status>>> entry : resetStats.entrySet()) {
                String channelId = entry.getKey();
                Map<Integer, Set<Status>> metaDataIds = entry.getValue();

                for (Entry<Integer, Set<Status>> metaDataEntry : metaDataIds.entrySet()) {
                    Integer metaDataId = metaDataEntry.getKey();
                    Set<Status> statuses = metaDataEntry.getValue();

                    for (Status status : statuses) {
                        currentStats.getChannelStats(channelId).get(metaDataId).put(status, 0L);
                    }
                }
            }

            // update the in-memory stats with the stats we just saved in storage
            currentStats.update(transactionStats);

            // remove the in-memory stats for any channels that were removed
            for (String channelId : removedChannelIds) {
                currentStats.getStats().remove(channelId);
            }
        }

        synchronized (totalStats) {
            // update the in-memory total stats with the stats we just saved in storage
            totalStats.update(transactionStats);

            // remove the in-memory total stats for any channels that were removed
            for (String channelId : removedChannelIds) {
                totalStats.getStats().remove(channelId);
            }
        }

        if (statisticsUpdater != null) {
            statisticsUpdater.update(transactionStats);
        }

        transactionStats.getStats().clear();
    }

    @Override
    public void rollback() {
        transactionStats.getStats().clear();
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
    public void insertConnectorMessage(ConnectorMessage connectorMessage, boolean storeMaps) {
        transactionStats.update(connectorMessage.getChannelId(), connectorMessage.getMetaDataId(), connectorMessage.getStatus(), null);
    }

    @Override
    public void updateStatus(ConnectorMessage connectorMessage, Status previousStatus) {
        // don't decrement the previous status if it was RECEIVED
        if (previousStatus == Status.RECEIVED) {
            previousStatus = null;
        }

        transactionStats.update(connectorMessage.getChannelId(), connectorMessage.getMetaDataId(), connectorMessage.getStatus(), previousStatus);
    }

    @Override
    public void removeChannel(String channelId) {
        removedChannelIds.add(channelId);
    }

    @Override
    public void resetStatistics(String channelId, Integer metaDataId, Set<Status> statuses) {
        for (Status status : statuses) {
            if (transactionStats.getChannelStats(channelId).containsKey(metaDataId)) {
                transactionStats.getChannelStats(channelId).get(metaDataId).remove(status);
            }
        }

        if (!resetStats.containsKey(channelId)) {
            resetStats.put(channelId, new HashMap<Integer, Set<Status>>());
        }

        Map<Integer, Set<Status>> metaDataIds = resetStats.get(channelId);

        if (!metaDataIds.containsKey(metaDataId)) {
            metaDataIds.put(metaDataId, statuses);
        }
    }

    @Override
    public void insertMessage(Message message) {}

    @Override
    public void insertMessageContent(MessageContent messageContent) {}

    @Override
    public void insertMessageAttachment(String channelId, long messageId, Attachment attachment) {}

    @Override
    public void insertMetaData(ConnectorMessage connectorMessage, List<MetaDataColumn> metaDataColumns) {}

    @Override
    public void storeMessageContent(MessageContent messageContent) {}

    @Override
    public void addChannelStatistics(Statistics statistics) {}

    @Override
    public void updateSourceResponse(ConnectorMessage connectorMessage) {}

    @Override
    public void updateErrors(ConnectorMessage connectorMessage) {}

    @Override
    public void updateMaps(ConnectorMessage connectorMessage) {}

    @Override
    public void updateResponseMap(ConnectorMessage connectorMessage) {}

    @Override
    public void markAsProcessed(String channelId, long messageId) {}

    @Override
    public void resetMessage(String channelId, long messageId) {}

    @Override
    public void createChannel(String channelId, long localChannelId) {}

    @Override
    public Map<String, Long> getLocalChannelIds() {
        return new HashMap<String, Long>();
    }

    @Override
    public Long selectMaxLocalChannelId() {
        return 1L;
    }

    @Override
    public void deleteAllMessages(String channelId) {}

    @Override
    public void deleteMessageContent(String channelId, long messageId) {}

    @Override
    public void deleteMessageAttachments(String channelId, long messageId) {}

    @Override
    public void addMetaDataColumn(String channelId, MetaDataColumn metaDataColumn) {}

    @Override
    public void removeMetaDataColumn(String channelId, String columnName) {}

    @Override
    public long getMaxMessageId(String channelId) {
        return 1L;
    }

    @Override
    public long getNextMessageId(String channelId) {
        return 1L;
    }

    @Override
    public List<Attachment> getMessageAttachment(String channelId, long messageId) {
        return new ArrayList<Attachment>();
    }

    @Override
    public Attachment getMessageAttachment(String channelId, String attachmentId) {
        return new Attachment();
    }

    @Override
    public Map<Integer, ConnectorMessage> getConnectorMessages(String channelId, long messageId) {
        return new HashMap<Integer, ConnectorMessage>();
    }

    @Override
    public List<ConnectorMessage> getConnectorMessages(String channelId, int metaDataId, Status status) {
        return new ArrayList<ConnectorMessage>();
    }

    @Override
    public List<ConnectorMessage> getConnectorMessages(String channelId, int metaDataId, Status status, int offset, int limit, Long minMessageId, Long maxMessageId) {
        return new ArrayList<ConnectorMessage>();
    }

    @Override
    public List<Message> getUnfinishedMessages(String channelId, String serverId) {
        return new ArrayList<Message>();
    }

    @Override
    public int getConnectorMessageCount(String channelId, int metaDataId, Status status) {
        return 0;
    }

    @Override
    public long getConnectorMessageMaxMessageId(String channelId, int metaDataId, Status status) {
        return 0;
    }

    @Override
    public void insertEvent(Event event) {}

    @Override
    public List<MetaDataColumn> getMetaDataColumns(String channelId) {
        return new ArrayList<MetaDataColumn>();
    }

    @Override
    public void deleteMessage(String channelId, long messageId, boolean deleteStatistics) {}

    @Override
    public void deleteConnectorMessages(String channelId, long messageId, List<Integer> metaDataIds, boolean deleteStatistics) {}

    @Override
    public void batchInsertMessageContent(MessageContent messageContent) {}

    @Override
    public void executeBatchInsertMessageContent(String channelId) {}

    @Override
    public Statistics getChannelStatistics() {
        return new Statistics();
    }

    @Override
    public Statistics getChannelTotalStatistics() {
        return new Statistics();
    }

    @Override
    public List<ConnectorMessage> getConnectorMessages(String channelId, long messageId, Set<Integer> metaDataIds, boolean includeContent) {
        return null;
    }
}
