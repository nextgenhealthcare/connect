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
import com.mirth.connect.model.util.DefaultMetaData;

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
        logger.error("The ${message.source} reference is deprecated and will soon be removed. Please use the \"" + DefaultMetaData.SOURCE_VARIABLE_MAPPING + "\" variable in the connector map instead.");
        Object source = connectorMessage.getConnectorMap().get(DefaultMetaData.SOURCE_VARIABLE_MAPPING);
        return source != null ? source.toString() : "";
    }

    @Deprecated
    // TODO: Remove in 3.1
    public String getType() {
        logger.error("The ${message.type} reference is deprecated and will soon be removed. Please use the \"" + DefaultMetaData.TYPE_VARIABLE_MAPPING + "\" variable in the connector map instead.");
        Object type = connectorMessage.getConnectorMap().get(DefaultMetaData.TYPE_VARIABLE_MAPPING);
        return type != null ? type.toString() : "";
    }

    @Deprecated
    // TODO: Remove in 3.1
    public String getVersion() {
        logger.error("The ${message.version} reference is deprecated and will soon be removed. Please use the \"" + DefaultMetaData.VERSION_VARIABLE_MAPPING + "\" variable in the connector map instead.");
        Object version = connectorMessage.getConnectorMap().get(DefaultMetaData.VERSION_VARIABLE_MAPPING);
        return version != null ? version.toString() : "";
    }

    public String toString() {
        return connectorMessage.toString();
    }
}
