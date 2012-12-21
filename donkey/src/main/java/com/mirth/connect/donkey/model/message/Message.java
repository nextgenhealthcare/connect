/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

import java.io.Serializable;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("message")
public class Message implements Serializable {
    private Long messageId;
    private String serverId;
    private String channelId;
    private Calendar dateCreated;
    private boolean processed;
    private Long importId;
    private String importChannelId;
    private boolean attemptedResponse;
    private String responseError;
    private Map<Integer, ConnectorMessage> connectorMessages = new LinkedHashMap<Integer, ConnectorMessage>();

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

    public Calendar getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Calendar dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
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
        ConnectorMessage sourceConnectorMessage = connectorMessages.get(0);

        ConnectorMessage connectorMessage = new ConnectorMessage();
        connectorMessage.setChannelId(channelId);
        connectorMessage.setMessageId(messageId);
        connectorMessage.setServerId(serverId);
        connectorMessage.setDateCreated(dateCreated);
        connectorMessage.setRaw(sourceConnectorMessage.getRaw());
        connectorMessage.setProcessedRaw(sourceConnectorMessage.getProcessedRaw());

        Map<String, Response> responseMap = sourceConnectorMessage.getResponseMap();

        for (ConnectorMessage destinationMessage : connectorMessages.values()) {
            responseMap.putAll(destinationMessage.getResponseMap());
        }

        connectorMessage.setResponseMap(responseMap);

        Map<String, Object> channelMap = sourceConnectorMessage.getChannelMap();

        for (ConnectorMessage destinationMessage : connectorMessages.values()) {
            channelMap.putAll(destinationMessage.getChannelMap());
        }

        connectorMessage.setChannelMap(channelMap);

        return connectorMessage;
    }

    public boolean isAttemptedResponse() {
        return attemptedResponse;
    }

    public void setAttemptedResponse(boolean attemptedResponse) {
        this.attemptedResponse = attemptedResponse;
    }

    public String getResponseError() {
        return responseError;
    }

    public void setResponseError(String responseError) {
        this.responseError = responseError;
    }

    public String toString() {
        return "message " + messageId;
    }
}
