package com.mirth.connect.server.userutil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.ImmutableConnectorMessage;
import com.mirth.connect.donkey.model.message.Status;

@Deprecated
// TODO: Remove in 3.1
public class MessageObject {
    private Logger logger = Logger.getLogger(getClass());
    private ImmutableConnectorMessage connectorMessage;
    
    public MessageObject(ImmutableConnectorMessage connectorMessage) {
        this.connectorMessage = connectorMessage;
    }
    
    ImmutableConnectorMessage getImmutableConnectorMessage() {
        return connectorMessage;
    }

    public String getSource() {
        logger.error("The messageObject.getSource() method is deprecated and will soon be removed. Please extract the message source from the \"msg\" variable in a transformer instead.");
        return "";
    }

    public void setSource(String source) {
        logger.error("The messageObject.setSource() method is deprecated and will soon be removed. Please use a custom metadata column instead.");
    }

    public String getType() {
        logger.error("The messageObject.getType() method is deprecated and will soon be removed. Please extract the message type from the \"msg\" variable in a transformer instead.");
        return "";
    }

    public void setType(String type) {
        logger.error("The messageObject.setType(type) method is deprecated and will soon be removed. Please use a custom metadata column instead.");
    }

    public String getId() {
        logger.error("The messageObject.getId() method is deprecated and will soon be removed. Please use connectorMessage.getMessageId() instead.");
        return String.valueOf(connectorMessage.getMessageId());
    }

    public void setId(String id) {
        logger.error("The messageObject.setId(id) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    public String getVersion() {
        logger.error("The messageObject.getVersion() method is deprecated and will soon be removed. Please extract the message version from the \"msg\" variable in a transformer instead.");
        return "";
    }

    public void setVersion(String version) {
        logger.error("The messageObject.setVersion(version) method is deprecated and will soon be removed. Please use a custom metadata column instead.");
    }

    public String getChannelId() {
        logger.error("The messageObject.getChannelId() method is deprecated and will soon be removed. Please use connectorMessage.getChannelId() or the variable \"channelId\" instead.");
        return connectorMessage.getChannelId();
    }

    public void setChannelId(String channelId) {
        logger.error("The messageObject.setChannelId(channelId) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    public Status getStatus() {
        logger.error("The messageObject.getStatus() method is deprecated and will soon be removed. Please use connectorMessage.getStatus() instead. Note that the UNKNOWN and ACCEPTED statuses are no longer valid.");
        return connectorMessage.getStatus();
    }

    public void setStatus(Status status) {
        logger.error("The messageObject.setStatus(status) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    public Calendar getDateCreated() {
        logger.error("The messageObject.getDateCreated() method is deprecated and will soon be removed. Please use connectorMessage.getReceivedDate() instead.");
        return connectorMessage.getReceivedDate();
    }

    public void setDateCreated(Calendar dateCreated) {
        logger.error("The messageObject.setDateCreated(dateCreated) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    public String getEncodedData() {
        logger.error("The messageObject.getEncodedData() method is deprecated and will soon be removed. Please use connectorMessage.getEncodedData() instead.");
        return connectorMessage.getEncodedData();
    }

    public void setEncodedData(String encodedData) {
        logger.error("The messageObject.setEncodedData(encodedData) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    public String getEncodedDataProtocol() {
        logger.error("The messageObject.getEncodedDataProtocol() method is deprecated and will soon be removed. Please use connectorMessage.getEncoded().getDataType() instead.");
        return connectorMessage.getEncoded().getDataType();
    }

    public void setEncodedDataProtocol(String encodedDataProtocol) {
        logger.error("The messageObject.setEncodedDataProtocol(encodedDataProtocol) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    public String getRawData() {
        logger.error("The messageObject.getRawData() method is deprecated and will soon be removed. Please use connectorMessage.getRawData() instead.");
        return connectorMessage.getRawData();
    }

    public void setRawData(String rawData) {
        logger.error("The messageObject.setRawData(rawData) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    public String getRawDataProtocol() {
        logger.error("The messageObject.getRawDataProtocol() method is deprecated and will soon be removed. Please use connectorMessage.getRaw().getDataType() instead.");
        return connectorMessage.getRaw().getDataType();
    }

    public void setRawDataProtocol(String rawDataProtocol) {
        logger.error("The messageObject.setRawDataProtocol(rawDataProtocol) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    public String getTransformedData() {
        logger.error("The messageObject.getTransformedData() method is deprecated and will soon be removed. Please use connectorMessage.getTransformedData() instead.");
        return connectorMessage.getTransformedData();
    }

    public void setTransformedData(String transformedData) {
        logger.error("The messageObject.setTransformedData(transformedData) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    public String getTransformedDataProtocol() {
        logger.error("The messageObject.getTransformedDataProtocol() method is deprecated and will soon be removed. Please use connectorMessage.getTransformed().getDataType() instead.");
        return connectorMessage.getTransformed().getDataType();
    }

    public void setTransformedDataProtocol(String transformedDataProtocol) {
        logger.error("The messageObject.setTransformedDataProtocol(transformedDataProtocol) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    public Map getConnectorMap() {
        logger.error("The messageObject.getConnectorMap() method is deprecated and will soon be removed. Please use connectorMessage.getConnectorMap() or the variable \"connectorMap\" instead.");
        return connectorMessage.getConnectorMap();
    }

    public void setConnectorMap(Map variableMap) {
        logger.error("The messageObject.setConnectorMap(variableMap) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    public boolean isEncrypted() {
        logger.error("The messageObject.isEncrypted() method is deprecated and will soon be removed. This method always returns false.");
        return false;
    }

    public void setEncrypted(boolean encrypted) {
        logger.error("The messageObject.setEncrypted(encrypted) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    public String getConnectorName() {
        return connectorMessage.getConnectorName();
    }

    public void setConnectorName(String connectorName) {
        logger.error("The messageObject.setConnectorName(connectorName) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    public String getErrors() {
        logger.error("The messageObject.getErrors() method is deprecated and will soon be removed. Please use connectorMessage.getProcessingError() instead.");
        return connectorMessage.getProcessingError();
    }

    public void setErrors(String errors) {
        logger.error("The messageObject.setErrors(errors) method is deprecated and will soon be removed. This method no longer does anything.");
    }
    
    public Map getResponseMap() {
        logger.error("The messageObject.getResponseMap() method is deprecated and will soon be removed. Please use connectorMessage.getResponseMap() or the variable \"responseMap\" instead.");
        return connectorMessage.getResponseMap();
    }

    public void setResponseMap(Map responseMap) {
        logger.error("The messageObject.setResponseMap(responseMap) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    public Map getChannelMap() {
        logger.error("The messageObject.getChannelMap() method is deprecated and will soon be removed. Please use connectorMessage.getChannelMap() or the variable \"channelMap\" instead.");
        return connectorMessage.getChannelMap();
    }

    public void setChannelMap(Map channelMap) {
        logger.error("The messageObject.setChannelMap(channelMap) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    public String getServerId() {
        logger.error("The messageObject.getServerId() method is deprecated and will soon be removed. Please use connectorMessage.getServerId() instead.");
        return connectorMessage.getServerId();
    }

    public void setServerId(String serverId) {
        logger.error("The messageObject.setServerId(serverId) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    public boolean isAttachment() {
        logger.error("The messageObject.isAttachment() method is deprecated and will soon be removed. This method always returns false.");
        return false;
    }

    public void setAttachment(boolean attachment) {
        logger.error("The messageObject.setAttachment(attachment) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    public Object clone() {
        logger.error("The messageObject.clone() method is deprecated and will soon be removed. This method always returns null.");
        return null;
    }

    public boolean equals(Object that) {
        logger.error("The messageObject.equals(that) method is deprecated and will soon be removed. This method always returns false.");
        return false;
    }

    public String getCorrelationId() {
        logger.error("The messageObject.getCorrelationId() method is deprecated and will soon be removed. Please use connectorMessage.getMessageId() instead.");
        return String.valueOf(connectorMessage.getMessageId());
    }

    public void setCorrelationId(String correlationId) {
        logger.error("The messageObject.setCorrelationId(correlationId) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    public Map<String, Object> getContext() {
        logger.error("The messageObject.getContext() method is deprecated and will soon be removed. This method always returns an empty map.");
        return new HashMap<String, Object>();
    }

    public void setContext(Map<String, Object> context) {
        logger.error("The messageObject.setContext(context) method is deprecated and will soon be removed. This method no longer does anything.");
    }
    
    public String toString() {
        logger.error("The messageObject.toString() method is deprecated and will soon be removed. Please use connectorMessage.toString() instead.");
        return connectorMessage.toString();
    }
    
    public String toAuditString() {
        logger.error("The messageObject.toAuditString() method is deprecated and will soon be removed. Please use connectorMessage.toString() instead.");
        return connectorMessage.toString();
    }
}
