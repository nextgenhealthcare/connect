/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("message")
public class Message implements Serializable {
    private Long messageId;
    private String serverId;
    private String channelId;
    private Calendar receivedDate;
    private boolean processed;
    private Long originalId;
    private Long importId;
    private String importChannelId;
    private Map<Integer, ConnectorMessage> connectorMessages = new LinkedHashMap<Integer, ConnectorMessage>();
    private transient ConnectorMessage mergedConnectorMessage;

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Calendar getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Calendar receivedDate) {
        this.receivedDate = receivedDate;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public Long getOriginalId() {
        return originalId;
    }

    public void setOriginalId(Long originalId) {
        this.originalId = originalId;
    }

    public Long getImportId() {
        return importId;
    }

    public void setImportId(Long importId) {
        this.importId = importId;
    }

    public String getImportChannelId() {
        return importChannelId;
    }

    public void setImportChannelId(String importChannelId) {
        this.importChannelId = importChannelId;
    }

    public Map<Integer, ConnectorMessage> getConnectorMessages() {
        return connectorMessages;
    }

    public ConnectorMessage getMergedConnectorMessage() {
        if (mergedConnectorMessage == null) {
            mergedConnectorMessage = new ConnectorMessage();
            mergedConnectorMessage.setChannelId(channelId);
            mergedConnectorMessage.setMessageId(messageId);
            mergedConnectorMessage.setServerId(serverId);
            mergedConnectorMessage.setReceivedDate(receivedDate);

            Map<String, Object> sourceMap = null;
            Map<String, Object> responseMap = new HashMap<String, Object>();
            Map<String, Object> channelMap = new HashMap<String, Object>();

            ConnectorMessage sourceConnectorMessage = connectorMessages.get(0);

            if (sourceConnectorMessage != null) {
                mergedConnectorMessage.setRaw(sourceConnectorMessage.getRaw());
                mergedConnectorMessage.setProcessedRaw(sourceConnectorMessage.getProcessedRaw());
                sourceMap = sourceConnectorMessage.getSourceMap();
                responseMap.putAll(sourceConnectorMessage.getResponseMap());
                channelMap.putAll(sourceConnectorMessage.getChannelMap());
            }

            for (ConnectorMessage connectorMessage : connectorMessages.values()) {
                if (connectorMessage.getMetaDataId() > 0) {
                    if (sourceMap == null) {
                        sourceMap = connectorMessage.getSourceMap();
                    }
                    responseMap.putAll(connectorMessage.getResponseMap());
                    channelMap.putAll(connectorMessage.getChannelMap());
                }
            }

            mergedConnectorMessage.setSourceMap(sourceMap);
            mergedConnectorMessage.setResponseMap(responseMap);
            mergedConnectorMessage.setChannelMap(channelMap);
        }

        return mergedConnectorMessage;
    }

    public String toString() {
        return "message " + messageId;
    }
}
