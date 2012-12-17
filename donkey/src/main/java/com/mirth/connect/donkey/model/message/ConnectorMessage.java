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
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("connectorMessage")
public class ConnectorMessage implements Serializable {
    private long messageId;
    private int metaDataId;
    private String channelId;
    private String connectorName;
    private String serverId;
    private Calendar dateCreated;
    private Status status;
    private MessageContent raw;
    private MessageContent processedRaw;
    private MessageContent transformed;
    private MessageContent encoded;
    private MessageContent sent;
    private MessageContent response;
    private MessageContent processedResponse;
    private Map<String, Object> connectorMap = new HashMap<String, Object>();
    private Map<String, Object> channelMap = new HashMap<String, Object>();
    private Map<String, Response> responseMap = new HashMap<String, Response>();
    private Map<String, Object> metaDataMap = new HashMap<String, Object>();
    private String errors;
    private int sendAttempts = 0;
    private int chainId;
    private int orderId;

    public ConnectorMessage() {}

    public ConnectorMessage(String channelId, long messageId, int metaDataId, String serverId, Calendar dateCreated, Status status) {
        this.channelId = channelId;
        this.messageId = messageId;
        this.metaDataId = metaDataId;
        this.serverId = serverId;
        this.dateCreated = dateCreated;
        this.status = status;
    }

    public int getMetaDataId() {
        return metaDataId;
    }

    public void setMetaDataId(int metaDataId) {
        this.metaDataId = metaDataId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getConnectorName() {
        return connectorName;
    }

    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public Calendar getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Calendar dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public MessageContent getContent(ContentType contentType) {
        switch (contentType) {
            case RAW:
                return raw;
            case PROCESSED_RAW:
                return processedRaw;
            case TRANSFORMED:
                return transformed;
            case ENCODED:
                return encoded;
            case SENT:
                return sent;
            case RESPONSE:
                return response;
            case PROCESSED_RESPONSE:
                return processedResponse;
        }

        return null;
    }

    public void setContent(MessageContent messageContent) {
        switch (messageContent.getContentType()) {
            case RAW:
                setRaw(messageContent);
                break;
            case PROCESSED_RAW:
                setProcessedRaw(messageContent);
                break;
            case TRANSFORMED:
                setTransformed(messageContent);
                break;
            case ENCODED:
                setEncoded(messageContent);
                break;
            case SENT:
                setSent(messageContent);
                break;
            case RESPONSE:
                setResponse(messageContent);
                break;
            case PROCESSED_RESPONSE:
                setProcessedResponse(messageContent);
                break;
            default:
                /*
                 * if the content type is not recognized, then this code needs
                 * to be fixed to include all possible content types. We throw a
                 * runtime exception since this is an internal error that
                 * needs to be corrected.
                 */
                throw new RuntimeException("Unrecognized content type: " + messageContent.getContentType().getContentTypeCode());
        }
    }

    public MessageContent getRaw() {
        return raw;
    }

    public void setRaw(MessageContent messageContentRaw) {
        this.raw = messageContentRaw;
    }

    public MessageContent getProcessedRaw() {
        return processedRaw;
    }

    public void setProcessedRaw(MessageContent processedRaw) {
        this.processedRaw = processedRaw;
    }

    public MessageContent getTransformed() {
        return transformed;
    }

    public void setTransformed(MessageContent messageContentTransformed) {
        this.transformed = messageContentTransformed;
    }

    public MessageContent getEncoded() {
        return encoded;
    }

    public void setEncoded(MessageContent messageContentEncoded) {
        this.encoded = messageContentEncoded;
    }

    public MessageContent getSent() {
        return sent;
    }

    public void setSent(MessageContent messageContentSent) {
        this.sent = messageContentSent;
    }

    public MessageContent getResponse() {
        return response;
    }

    public void setResponse(MessageContent messageContentResponse) {
        this.response = messageContentResponse;
    }

    public MessageContent getProcessedResponse() {
        return processedResponse;
    }

    public void setProcessedResponse(MessageContent processedResponse) {
        this.processedResponse = processedResponse;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public Map<String, Object> getConnectorMap() {
        return connectorMap;
    }

    public void setConnectorMap(Map<String, Object> connectorMap) {
        this.connectorMap = connectorMap;
    }

    public Map<String, Object> getChannelMap() {
        return channelMap;
    }

    public void setChannelMap(Map<String, Object> channelMap) {
        this.channelMap = channelMap;
    }

    public Map<String, Response> getResponseMap() {
        return responseMap;
    }

    public void setResponseMap(Map<String, Response> responseMap) {
        this.responseMap = responseMap;
    }

    public Map<String, Object> getMetaDataMap() {
        return metaDataMap;
    }

    public void setMetaDataMap(Map<String, Object> metaDataMap) {
        this.metaDataMap = metaDataMap;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public int getSendAttempts() {
        return sendAttempts;
    }

    public void setSendAttempts(int sendAttempts) {
        this.sendAttempts = sendAttempts;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String toString() {
        return "message " + messageId + "-" + metaDataId + " (" + status + ")";
    }
}
