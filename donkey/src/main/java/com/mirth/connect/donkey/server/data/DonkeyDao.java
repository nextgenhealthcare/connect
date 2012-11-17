/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data;

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
import com.mirth.connect.donkey.server.event.Event;

public interface DonkeyDao {
    public void insertMessage(Message message);

    public void insertConnectorMessage(ConnectorMessage connectorMessage, boolean storeMaps);

    public void insertMessageContent(MessageContent messageContent);

    public void batchInsertMessageContent(MessageContent messageContent);

    public void executeBatchInsertMessageContent(String channelId);

    public void insertMessageAttachment(String channelId, long messageId, Attachment attachment);

    public void insertMetaData(ConnectorMessage connectorMessage, List<MetaDataColumn> metaDataColumns);

    public void insertEvent(Event event);

    public void storeMessageContent(MessageContent messageContent);
    
    public void addChannelStatistics(Statistics statistics);
    
    public void updateSourceResponse(String channelId, long messageId, boolean attemptedResponse, String responseError);
    
    public void updateStatus(ConnectorMessage connectorMessage, Status previousStatus);

    public void updateErrors(ConnectorMessage connectorMessage);

    public void updateMaps(ConnectorMessage connectorMessage);

    public void updateResponseMap(ConnectorMessage connectorMessage);
    
    public void markAsProcessed(String channelId, long messageId);

    public void deleteMessage(String channelId, long messageId, boolean deleteStatistics);

    public void deleteConnectorMessages(String channelId, long messageId, List<Integer> metaDataIds, boolean deleteStatistics);

    public void deleteAllMessages(String channelId);

    public void deleteAllContent(String channelId, long messageId);

    public void createChannel(String channelId, long localChannelId);

    public void removeChannel(String channelId);

    public void addMetaDataColumn(String channelId, MetaDataColumn metaDataColumn);

    public void removeMetaDataColumn(String channelId, String columnName);

    public void resetStatistics(String channelId, Integer metaDataId, Set<Status> statuses);

    public Long selectMaxLocalChannelId();

    public Map<String, Long> getLocalChannelIds();

    public long getMaxMessageId(String channelId);

    public long getNextMessageId(String channelId);

    public List<ConnectorMessage> getConnectorMessages(String channelId, int metaDataId, Status status);
    
    public List<ConnectorMessage> getConnectorMessages(String channelId, int metaDataId, Status status, int offset, int limit, Long minMessageId, Long maxMessageId);

    public Map<Integer, ConnectorMessage> getConnectorMessages(String channelId, long messageId);

    public int getConnectorMessageCount(String channelId, int metaDataId, Status status);
    
    public long getConnectorMessageMaxMessageId(String channelId, int metaDataId, Status status);

    public List<Message> getUnfinishedMessages(String channelId, String serverId);

    public List<MetaDataColumn> getMetaDataColumns(String channelId);

    public Statistics getChannelStatistics();

    public Statistics getChannelTotalStatistics();

    public void commit();
    
    public void commit(boolean durable);

    public void rollback();

    public void close();

    public boolean isClosed();
}
