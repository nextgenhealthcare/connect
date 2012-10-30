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
import java.util.Set;

import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.event.Event;

public class PassthruDao implements DonkeyDao {
    private boolean closed = false;

    protected PassthruDao() {}

    @Override
    public void commit() {}
    
    @Override
    public void commit(boolean durable) {}

    @Override
    public void rollback() {}

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public boolean isClosed() {
        return closed;
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
    public void insertConnectorMessage(ConnectorMessage connectorMessage, boolean storeMaps) {}

    @Override
    public void storeMessageContent(MessageContent messageContent) {}

    @Override
    public void updateStatus(ConnectorMessage connectorMessage, Status previousStatus) {}

    @Override
    public void updateErrors(ConnectorMessage connectorMessage) {}

    @Override
    public void updateMaps(ConnectorMessage connectorMessage) {}

    @Override
    public void updateResponseMap(ConnectorMessage connectorMessage) {}

    @Override
    public void markAsProcessed(String channelId, long messageId) {}

    @Override
    public void createChannel(String channelId, long localChannelId) {}

    @Override
    public Map<String, Long> getLocalChannelIds() {
        return new HashMap<String, Long>();
    }

    @Override
    public void removeChannel(String channelId) {}

    @Override
    public Long selectMaxLocalChannelId() {
        return 1L;
    }

    @Override
    public void deleteAllMessages(String channelId) {}

    @Override
    public void deleteAllContent(String channelId, long messageId) {}

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
    public Map<Integer, ConnectorMessage> getConnectorMessages(String channelId, long messageId) {
        return new HashMap<Integer, ConnectorMessage>();
    }

    @Override
    public List<ConnectorMessage> getConnectorMessages(String channelId, int metaDataId, Status status) {
        return new ArrayList<ConnectorMessage>();
    }

    @Override
    public List<ConnectorMessage> getConnectorMessages(String channelId, int metaDataId, Status status, int offset, int limit) {
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
    public void resetStatistics(String channelId, Integer metaDataId, Set<Status> statuses) {}

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
}
