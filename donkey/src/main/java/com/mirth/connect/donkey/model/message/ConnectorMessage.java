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
    private Calendar receivedDate;
    private Status status;
    private MessageContent raw;
    private MessageContent processedRaw;
    private MessageContent transformed;
    private MessageContent encoded;
    private MessageContent sent;
    private MessageContent response;
    private MessageContent responseTransformed;
    private MessageContent processedResponse;
    private MapContent connectorMapContent = new MapContent();
    private MapContent channelMapContent = new MapContent();
    private MapContent responseMapContent = new MapContent();
    private Map<String, Object> metaDataMap = new HashMap<String, Object>();
    private ErrorContent processingErrorContent = new ErrorContent();
    private ErrorContent responseErrorContent = new ErrorContent();
    private int errorCode = 0;
    private int sendAttempts = 0;
    private Calendar sendDate;
    private Calendar responseDate;
    private int chainId;
    private int orderId;

    public ConnectorMessage() {}

    public ConnectorMessage(String channelId, long messageId, int metaDataId, String serverId, Calendar receivedDate, Status status) {
        this.channelId = channelId;
        this.messageId = messageId;
        this.metaDataId = metaDataId;
        this.serverId = serverId;
        this.receivedDate = receivedDate;
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

    public Calendar getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Calendar receivedDate) {
        this.receivedDate = receivedDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public MessageContent getMessageContent(ContentType contentType) {
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
            case RESPONSE_TRANSFORMED:
                return responseTransformed;
            case PROCESSED_RESPONSE:
                return processedResponse;
            default:
                return null;
        }
    }

    public void setMessageContent(MessageContent messageContent) {
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
            case RESPONSE_TRANSFORMED:
                setResponseTransformed(messageContent);
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

    public MessageContent getResponseTransformed() {
        return responseTransformed;
    }

    public void setResponseTransformed(MessageContent responseTransformed) {
        this.responseTransformed = responseTransformed;
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

    public MapContent getConnectorMapContent() {
        return connectorMapContent;
    }

    public void setConnectorMapContent(MapContent connectorMapContent) {
        this.connectorMapContent = connectorMapContent;
    }

    public MapContent getChannelMapContent() {
        return channelMapContent;
    }

    public void setChannelMapContent(MapContent channelMapContent) {
        this.channelMapContent = channelMapContent;
    }

    public MapContent getResponseMapContent() {
        return responseMapContent;
    }

    public void setResponseMapContent(MapContent responseMapContent) {
        this.responseMapContent = responseMapContent;
    }

    public Map<String, Object> getConnectorMap() {
        return connectorMapContent.getMap();
    }

    public void setConnectorMap(Map<String, Object> connectorMap) {
        connectorMapContent.setMap(connectorMap);
    }

    public Map<String, Object> getChannelMap() {
        return channelMapContent.getMap();
    }

    public void setChannelMap(Map<String, Object> channelMap) {
        channelMapContent.setMap(channelMap);
    }

    public Map<String, Object> getResponseMap() {
        return responseMapContent.getMap();
    }

    public void setResponseMap(Map<String, Object> responseMap) {
        responseMapContent.setMap(responseMap);
    }

    public Map<String, Object> getMetaDataMap() {
        return metaDataMap;
    }

    public void setMetaDataMap(Map<String, Object> metaDataMap) {
        this.metaDataMap = metaDataMap;
    }

    public ErrorContent getProcessingErrorContent() {
        return processingErrorContent;
    }

    public void setProcessingErrorContent(ErrorContent processingErrorContent) {
        this.processingErrorContent = processingErrorContent;
    }

    public ErrorContent getResponseErrorContent() {
        return responseErrorContent;
    }

    public void setResponseErrorContent(ErrorContent responseErrorContent) {
        this.responseErrorContent = responseErrorContent;
    }

    public String getProcessingError() {
        return processingErrorContent.getError();
    }

    public void setProcessingError(String processingError) {
        processingErrorContent.setError(processingError);

        updateErrorCode();
    }

    public String getResponseError() {
        return responseErrorContent.getError();
    }

    public void setResponseError(String responseError) {
        responseErrorContent.setError(responseError);

        updateErrorCode();
    }

    /**
     * Returns whether the connectorMessage contains an error of the content type that is provided.
     * The connector error code is the sum of all individual error codes of all the errors that
     * exist. Since individual error codes are all powers of 2, we can use bitwise operators to
     * determine the existence of an individual error.
     */
    public boolean containsError(ContentType contentType) {
        int errorCode = contentType.getErrorCode();

        if (errorCode > 0) {
            return (this.errorCode & errorCode) == errorCode;
        }

        return false;
    }

    /**
     * Update the errorCode of the connector message.
     */
    private void updateErrorCode() {
        // The errorCode is the sum of all the individual error codes for which an error exists.
        errorCode = 0;

        if (getProcessingError() != null) {
            errorCode += ContentType.PROCESSING_ERROR.getErrorCode();
        }
        if (getResponseError() != null) {
            errorCode += ContentType.RESPONSE_ERROR.getErrorCode();
        }
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getSendAttempts() {
        return sendAttempts;
    }

    public void setSendAttempts(int sendAttempts) {
        this.sendAttempts = sendAttempts;
    }

    public Calendar getSendDate() {
        return sendDate;
    }

    public void setSendDate(Calendar sendDate) {
        this.sendDate = sendDate;
    }

    public Calendar getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(Calendar responseDate) {
        this.responseDate = responseDate;
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
