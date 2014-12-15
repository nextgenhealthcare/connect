/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.userutil;

import java.util.Calendar;

/**
 * This class represents a connector message and is used to retrieve details such as the message ID,
 * metadata ID, status, and various content types.
 */
public class ValueReplacerConnectorMessage {
    private ImmutableConnectorMessage connectorMessage;

    /**
     * Instantiates an ValueReplacerConnectorMessage.
     * 
     * @param connectorMessage
     *            The connector message that this object will reference for retrieving data.
     */
    public ValueReplacerConnectorMessage(ImmutableConnectorMessage connectorMessage) {
        this.connectorMessage = connectorMessage;
    }

    /**
     * Returns the metadata ID of this connector message. Note that the source connector has a
     * metadata ID of 0.
     */
    public int getMetaDataId() {
        return connectorMessage.getMetaDataId();
    }

    /**
     * Returns the ID of the channel associated with this connector message.
     */
    public String getChannelId() {
        return connectorMessage.getChannelId();
    }

    /**
     * Returns the name of the connector associated with this connector message.
     */
    public String getConnectorName() {
        return connectorMessage.getConnectorName();
    }

    /**
     * Returns the ID of the server associated with this connector message.
     */
    public String getServerId() {
        return connectorMessage.getServerId();
    }

    /**
     * Returns the date/time that this connector message was created by the channel.
     */
    public Calendar getReceivedDate() {
        return connectorMessage.getReceivedDate();
    }

    /**
     * Returns the status (e.g. SENT) of this connector message.
     */
    public Status getStatus() {
        return connectorMessage.getStatus();
    }

    /**
     * Retrieves raw content associated with this connector message.
     * 
     * @return The raw content, as a string.
     */
    public String getRawData() {
        return connectorMessage.getRawData();
    }

    /**
     * Retrieves transformed content associated with this connector message.
     * 
     * @return The transformed content, as a string.
     */
    public String getTransformedData() {
        return connectorMessage.getTransformedData();
    }

    /**
     * Retrieves encoded content associated with this connector message.
     * 
     * @return The encoded content, as a string.
     */
    public String getEncodedData() {
        return connectorMessage.getEncodedData();
    }

    /**
     * Returns the sequential ID of the overall Message associated with this connector message.
     */
    public long getMessageId() {
        return connectorMessage.getMessageId();
    }

    @Override
    public String toString() {
        return connectorMessage.toString();
    }
}