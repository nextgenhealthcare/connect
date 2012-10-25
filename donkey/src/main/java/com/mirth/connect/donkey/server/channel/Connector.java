/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.message.DataType;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;

public class Connector {
    private String channelId;
    private int metaDataId;
    private DataType inboundDataType;
    private DataType outboundDataType;
    private ConnectorProperties connectorProperties;
    private DonkeyDaoFactory daoFactory;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public int getMetaDataId() {
        return metaDataId;
    }

    public void setMetaDataId(int metaDataId) {
        this.metaDataId = metaDataId;
    }

    public DataType getInboundDataType() {
        return inboundDataType;
    }

    public void setInboundDataType(DataType inboundDataType) {
        this.inboundDataType = inboundDataType;
    }

    public DataType getOutboundDataType() {
        return outboundDataType;
    }

    public void setOutboundDataType(DataType outboundDataType) {
        this.outboundDataType = outboundDataType;
    }

    public ConnectorProperties getConnectorProperties() {
        return connectorProperties;
    }

    public void setConnectorProperties(ConnectorProperties connectorProperties) {
        this.connectorProperties = connectorProperties;
    }

    public DonkeyDaoFactory getDaoFactory() {
        return daoFactory;
    }

    public void setDaoFactory(DonkeyDaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }
}
