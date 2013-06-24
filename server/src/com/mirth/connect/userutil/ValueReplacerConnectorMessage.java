/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.userutil;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.ImmutableConnectorMessage;
import com.mirth.connect.donkey.model.message.Status;

public class ValueReplacerConnectorMessage {
    private Logger logger = Logger.getLogger(getClass());
    private ImmutableConnectorMessage connectorMessage;

    public ValueReplacerConnectorMessage(ImmutableConnectorMessage connectorMessage) {
        this.connectorMessage = connectorMessage;
    }

    public int getMetaDataId() {
        return connectorMessage.getMetaDataId();
    }

    public String getChannelId() {
        return connectorMessage.getChannelId();
    }

    public String getConnectorName() {
        return connectorMessage.getConnectorName();
    }

    public String getServerId() {
        return connectorMessage.getServerId();
    }

    public Calendar getReceivedDate() {
        return connectorMessage.getReceivedDate();
    }

    public Status getStatus() {
        return connectorMessage.getStatus();
    }

    public String getRawData() {
        return connectorMessage.getRawData();
    }

    public String getTransformedData() {
        return connectorMessage.getTransformedData();
    }

    public String getEncodedData() {
        return connectorMessage.getEncodedData();
    }

    public long getMessageId() {
        return connectorMessage.getMessageId();
    }

    @Deprecated
    // TODO: Remove in 3.1
    public long getId() {
        logger.error("The ${message.id} reference is deprecated and will soon be removed. Please use ${message.messageId} instead.");
        return connectorMessage.getMessageId();
    }

    @Deprecated
    // TODO: Remove in 3.1
    public String getSource() {
        logger.error("The ${message.source} reference is deprecated and will soon be removed. Please extract the source in a transformer and use a map variable instead.");
        return null;
    }

    @Deprecated
    // TODO: Remove in 3.1
    public String getType() {
        logger.error("The ${message.type} reference is deprecated and will soon be removed. Please extract the type in a transformer and use a map variable instead.");
        return null;
    }

    @Deprecated
    // TODO: Remove in 3.1
    public String getVersion() {
        logger.error("The ${message.version} reference is deprecated and will soon be removed. Please extract the version in a transformer and use a map variable instead.");
        return null;
    }

    public String toString() {
        return connectorMessage.toString();
    }
}
