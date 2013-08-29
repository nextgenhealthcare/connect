/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.ImmutableConnectorMessage;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.model.util.DefaultMetaData;

/**
 * <b>This class is deprecated and will soon be removed. Please refer to
 * ImmutableConnectorMessage instead.</b><br/>
 * <br/>
 * 
 * This class represents a connector message and is used to retrieve details
 * such as the message ID, metadata ID, status, and various content types.
 * 
 * @see com.mirth.connect.donkey.model.message.ImmutableConnectorMessage
 */
@Deprecated
// TODO: Remove in 3.1
public class MessageObject {
    private Logger logger = Logger.getLogger(getClass());
    private ImmutableConnectorMessage connectorMessage;

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * Instantiates a MessageObject.
     * 
     * @param connectorMessage
     *            - The connector message that this object will reference for
     *            retrieving data.
     */
    public MessageObject(ImmutableConnectorMessage connectorMessage) {
        this.connectorMessage = connectorMessage;
    }

    ImmutableConnectorMessage getImmutableConnectorMessage() {
        return connectorMessage;
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use the
     * {@value com.mirth.connect.model.util.DefaultMetaData#SOURCE_VARIABLE_MAPPING}
     * variable in the connector map instead.</b><br/>
     * <br/>
     * 
     * Returns the source of the message (dependent on the inbound data type).
     */
    public String getSource() {
        logger.error("The messageObject.getSource() method is deprecated and will soon be removed. Please use the \"" + DefaultMetaData.SOURCE_VARIABLE_MAPPING + "\" variable in the connector map instead.");
        Object source = connectorMessage.getConnectorMap().get(DefaultMetaData.SOURCE_VARIABLE_MAPPING);
        return source != null ? source.toString() : "";
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please set the
     * {@value com.mirth.connect.model.util.DefaultMetaData#SOURCE_VARIABLE_MAPPING}
     * variable in the connector map instead.</b><br/>
     * <br/>
     * 
     * Sets the source of the message.
     */
    public void setSource(String source) {
        logger.error("The messageObject.setSource(source) method is deprecated and will soon be removed. Please set the \"" + DefaultMetaData.SOURCE_VARIABLE_MAPPING + "\" variable in the connector map instead.");
        connectorMessage.getConnectorMap().put(DefaultMetaData.SOURCE_VARIABLE_MAPPING, source);
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use the
     * {@value com.mirth.connect.model.util.DefaultMetaData#TYPE_VARIABLE_MAPPING}
     * variable in the connector map instead.</b><br/>
     * <br/>
     * 
     * Returns the type of the message (dependent on the inbound data type).
     */
    public String getType() {
        logger.error("The messageObject.getType() method is deprecated and will soon be removed. Please use the \"" + DefaultMetaData.TYPE_VARIABLE_MAPPING + "\" variable in the connector map instead.");
        Object type = connectorMessage.getConnectorMap().get(DefaultMetaData.TYPE_VARIABLE_MAPPING);
        return type != null ? type.toString() : "";
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please set the
     * {@value com.mirth.connect.model.util.DefaultMetaData#TYPE_VARIABLE_MAPPING}
     * variable in the connector map instead.</b><br/>
     * <br/>
     * 
     * Sets the type of the message.
     */
    public void setType(String type) {
        logger.error("The messageObject.setType(type) method is deprecated and will soon be removed. Please set the \"" + DefaultMetaData.TYPE_VARIABLE_MAPPING + "\" variable in the connector map instead.");
        connectorMessage.getConnectorMap().put(DefaultMetaData.TYPE_VARIABLE_MAPPING, type);
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use
     * connectorMessage.getMessageId() instead.</b><br/>
     * <br/>
     * 
     * Returns the sequential ID of the overall Message associated with this
     * connector message.
     */
    public String getId() {
        logger.error("The messageObject.getId() method is deprecated and will soon be removed. Please use connectorMessage.getMessageId() instead.");
        return String.valueOf(connectorMessage.getMessageId());
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setId(String id) {
        logger.error("The messageObject.setId(id) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use the
     * {@value com.mirth.connect.model.util.DefaultMetaData#VERSION_VARIABLE_MAPPING}
     * variable in the connector map instead.</b><br/>
     * <br/>
     * 
     * Returns the version of the message (dependent on the inbound data type).
     */
    public String getVersion() {
        logger.error("The messageObject.getVersion() method is deprecated and will soon be removed. Please use the \"" + DefaultMetaData.VERSION_VARIABLE_MAPPING + "\" variable in the connector map instead.");
        Object version = connectorMessage.getConnectorMap().get(DefaultMetaData.VERSION_VARIABLE_MAPPING);
        return version != null ? version.toString() : "";
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please set the
     * {@value com.mirth.connect.model.util.DefaultMetaData#VERSION_VARIABLE_MAPPING}
     * variable in the connector map instead.</b><br/>
     * <br/>
     * 
     * Sets the version of the message.
     */
    public void setVersion(String version) {
        logger.error("The messageObject.setVersion(version) method is deprecated and will soon be removed. Please set the \"" + DefaultMetaData.VERSION_VARIABLE_MAPPING + "\" variable in the connector map instead.");
        connectorMessage.getConnectorMap().put(DefaultMetaData.VERSION_VARIABLE_MAPPING, version);
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use
     * connectorMessage.getChannelId() or the variable "channelId" instead.</b><br/>
     * <br/>
     * 
     * Returns the ID of the channel associated with this connector message.
     */
    public String getChannelId() {
        logger.error("The messageObject.getChannelId() method is deprecated and will soon be removed. Please use connectorMessage.getChannelId() or the variable \"channelId\" instead.");
        return connectorMessage.getChannelId();
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setChannelId(String channelId) {
        logger.error("The messageObject.setChannelId(channelId) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use
     * connectorMessage.getStatus() instead. Note that the UNKNOWN and ACCEPTED
     * statuses are no longer valid.</b><br/>
     * <br/>
     * 
     * Returns the status (e.g. SENT) of this connector message.
     */
    public Status getStatus() {
        logger.error("The messageObject.getStatus() method is deprecated and will soon be removed. Please use connectorMessage.getStatus() instead. Note that the UNKNOWN and ACCEPTED statuses are no longer valid.");
        return connectorMessage.getStatus();
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setStatus(Status status) {
        logger.error("The messageObject.setStatus(status) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use
     * connectorMessage.getReceivedDate() instead.</b><br/>
     * <br/>
     * 
     * Returns the date/time that this connector message was created by the
     * channel.
     */
    public Calendar getDateCreated() {
        logger.error("The messageObject.getDateCreated() method is deprecated and will soon be removed. Please use connectorMessage.getReceivedDate() instead.");
        return connectorMessage.getReceivedDate();
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setDateCreated(Calendar dateCreated) {
        logger.error("The messageObject.setDateCreated(dateCreated) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use
     * connectorMessage.getEncodedData() instead.</b><br/>
     * <br/>
     * 
     * Retrieves encoded content associated with this connector message.
     * 
     * @return The encoded content, as a string.
     */
    public String getEncodedData() {
        logger.error("The messageObject.getEncodedData() method is deprecated and will soon be removed. Please use connectorMessage.getEncodedData() instead.");
        return connectorMessage.getEncodedData();
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setEncodedData(String encodedData) {
        logger.error("The messageObject.setEncodedData(encodedData) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use
     * connectorMessage.getEncoded().getDataType() instead.</b><br/>
     * <br/>
     * 
     * Returns the data type (e.g. "HL7V2") of the encoded content associated
     * with this connector message.
     */
    public String getEncodedDataProtocol() {
        logger.error("The messageObject.getEncodedDataProtocol() method is deprecated and will soon be removed. Please use connectorMessage.getEncoded().getDataType() instead.");
        return connectorMessage.getEncoded().getDataType();
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setEncodedDataProtocol(String encodedDataProtocol) {
        logger.error("The messageObject.setEncodedDataProtocol(encodedDataProtocol) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use
     * connectorMessage.getRawData() instead.</b><br/>
     * <br/>
     * 
     * Retrieves raw content associated with this connector message.
     * 
     * @return The raw content, as a string.
     */
    public String getRawData() {
        logger.error("The messageObject.getRawData() method is deprecated and will soon be removed. Please use connectorMessage.getRawData() instead.");
        return connectorMessage.getRawData();
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setRawData(String rawData) {
        logger.error("The messageObject.setRawData(rawData) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use
     * connectorMessage.getRaw().getDataType() instead.</b><br/>
     * <br/>
     * 
     * Returns the data type (e.g. "HL7V2") of the raw content associated with
     * this connector message.
     */
    public String getRawDataProtocol() {
        logger.error("The messageObject.getRawDataProtocol() method is deprecated and will soon be removed. Please use connectorMessage.getRaw().getDataType() instead.");
        return connectorMessage.getRaw().getDataType();
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setRawDataProtocol(String rawDataProtocol) {
        logger.error("The messageObject.setRawDataProtocol(rawDataProtocol) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use
     * connectorMessage.getTransformedData() instead.</b><br/>
     * <br/>
     * 
     * Retrieves transformed content associated with this connector message.
     * 
     * @return The transformed content, as a string.
     */
    public String getTransformedData() {
        logger.error("The messageObject.getTransformedData() method is deprecated and will soon be removed. Please use connectorMessage.getTransformedData() instead.");
        return connectorMessage.getTransformedData();
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setTransformedData(String transformedData) {
        logger.error("The messageObject.setTransformedData(transformedData) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use
     * connectorMessage.getRaw().getDataType() instead.</b><br/>
     * <br/>
     * 
     * Returns the data type (e.g. "HL7V2") of the transformed content
     * associated with this connector message.
     */
    public String getTransformedDataProtocol() {
        logger.error("The messageObject.getTransformedDataProtocol() method is deprecated and will soon be removed. Please use connectorMessage.getTransformed().getDataType() instead.");
        return connectorMessage.getTransformed().getDataType();
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setTransformedDataProtocol(String transformedDataProtocol) {
        logger.error("The messageObject.setTransformedDataProtocol(transformedDataProtocol) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use
     * connectorMessage.getConnectorMap() or the variable "connectorMap"
     * instead.</b><br/>
     * <br/>
     * 
     * Returns the connector map.
     */
    public Map getConnectorMap() {
        logger.error("The messageObject.getConnectorMap() method is deprecated and will soon be removed. Please use connectorMessage.getConnectorMap() or the variable \"connectorMap\" instead.");
        return connectorMessage.getConnectorMap();
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setConnectorMap(Map variableMap) {
        logger.error("The messageObject.setConnectorMap(variableMap) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method always returns false.
     */
    public boolean isEncrypted() {
        logger.error("The messageObject.isEncrypted() method is deprecated and will soon be removed. This method always returns false.");
        return false;
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setEncrypted(boolean encrypted) {
        logger.error("The messageObject.setEncrypted(encrypted) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use
     * connectorMessage.getConnectorName() or the variable "connector"
     * instead.</b><br/>
     * <br/>
     * 
     * Returns the name of the connector associated with this connector message.
     */
    public String getConnectorName() {
        logger.error("The messageObject.getConnectorName() method is deprecated and will soon be removed. Please use connectorMessage.getConnectorName() or the variable \"connector\" instead.");
        return connectorMessage.getConnectorName();
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setConnectorName(String connectorName) {
        logger.error("The messageObject.setConnectorName(connectorName) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use
     * connectorMessage.getProcessingError() instead.</b><br/>
     * <br/>
     * 
     * Returns the processing error string associated with this connector
     * message, if it exists.
     */
    public String getErrors() {
        logger.error("The messageObject.getErrors() method is deprecated and will soon be removed. Please use connectorMessage.getProcessingError() instead.");
        return connectorMessage.getProcessingError();
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setErrors(String errors) {
        logger.error("The messageObject.setErrors(errors) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use
     * connectorMessage.getResponseMap() or the variable "responseMap"
     * instead.</b><br/>
     * <br/>
     * 
     * Returns the response map.
     */
    public Map getResponseMap() {
        logger.error("The messageObject.getResponseMap() method is deprecated and will soon be removed. Please use connectorMessage.getResponseMap() or the variable \"responseMap\" instead.");
        return connectorMessage.getResponseMap();
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setResponseMap(Map responseMap) {
        logger.error("The messageObject.setResponseMap(responseMap) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use
     * connectorMessage.getChannelMap() or the variable "channelMap"
     * instead.</b><br/>
     * <br/>
     * 
     * Returns the channel map.
     */
    public Map getChannelMap() {
        logger.error("The messageObject.getChannelMap() method is deprecated and will soon be removed. Please use connectorMessage.getChannelMap() or the variable \"channelMap\" instead.");
        return connectorMessage.getChannelMap();
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setChannelMap(Map channelMap) {
        logger.error("The messageObject.setChannelMap(channelMap) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use
     * connectorMessage.getServerId() instead.</b><br/>
     * <br/>
     * 
     * Returns the ID of the server associated with this connector message.
     */
    public String getServerId() {
        logger.error("The messageObject.getServerId() method is deprecated and will soon be removed. Please use connectorMessage.getServerId() instead.");
        return connectorMessage.getServerId();
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setServerId(String serverId) {
        logger.error("The messageObject.setServerId(serverId) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method always returns false.
     */
    public boolean isAttachment() {
        logger.error("The messageObject.isAttachment() method is deprecated and will soon be removed. This method always returns false.");
        return false;
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setAttachment(boolean attachment) {
        logger.error("The messageObject.setAttachment(attachment) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method always returns null.
     */
    public Object clone() {
        logger.error("The messageObject.clone() method is deprecated and will soon be removed. This method always returns null.");
        return null;
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method always returns false.
     */
    public boolean equals(Object that) {
        logger.error("The messageObject.equals(that) method is deprecated and will soon be removed. This method always returns false.");
        return false;
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use
     * connectorMessage.getMessageId() instead.</b><br/>
     * <br/>
     * 
     * Returns the sequential ID of the overall Message associated with this
     * connector message.
     */
    public String getCorrelationId() {
        logger.error("The messageObject.getCorrelationId() method is deprecated and will soon be removed. Please use connectorMessage.getMessageId() instead.");
        return String.valueOf(connectorMessage.getMessageId());
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setCorrelationId(String correlationId) {
        logger.error("The messageObject.setCorrelationId(correlationId) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method always returns an empty map.
     */
    public Map<String, Object> getContext() {
        logger.error("The messageObject.getContext() method is deprecated and will soon be removed. This method always returns an empty map.");
        return new HashMap<String, Object>();
    }

    /**
     * <b>This class is deprecated and will soon be removed.</b><br/>
     * <br/>
     * 
     * This method no longer does anything.
     */
    public void setContext(Map<String, Object> context) {
        logger.error("The messageObject.setContext(context) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use
     * connectorMessage.toString() instead.</b><br/>
     * <br/>
     * 
     * Returns a string representation of the object.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        logger.error("The messageObject.toString() method is deprecated and will soon be removed. Please use connectorMessage.toString() instead.");
        return connectorMessage.toString();
    }

    /**
     * <b>This class is deprecated and will soon be removed. Please use
     * connectorMessage.toString() instead.</b><br/>
     * <br/>
     * 
     * Returns a string representation of the object.
     * 
     * @see java.lang.Object#toString()
     */
    public String toAuditString() {
        logger.error("The messageObject.toAuditString() method is deprecated and will soon be removed. Please use connectorMessage.toString() instead.");
        return connectorMessage.toString();
    }
}
