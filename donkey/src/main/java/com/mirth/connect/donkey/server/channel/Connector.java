/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import com.mirth.connect.donkey.model.channel.ChannelState;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.message.DataType;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.Startable;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.Stoppable;
import com.mirth.connect.donkey.server.UndeployException;

public abstract class Connector implements Startable, Stoppable {
    private String channelId;
    private int metaDataId;
    private DataType inboundDataType;
    private DataType outboundDataType;
    private ChannelState currentState = ChannelState.STOPPED;
    private ConnectorProperties connectorProperties;

    public abstract void onDeploy() throws DeployException;

    public abstract void onUndeploy() throws UndeployException;

    public abstract void onStart() throws StartException;

    public abstract void onStop() throws StopException;
    
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
    
    public ChannelState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(ChannelState currentState) {
        this.currentState = currentState;
    }

    public ConnectorProperties getConnectorProperties() {
        return connectorProperties;
    }

    public void setConnectorProperties(ConnectorProperties connectorProperties) {
        this.connectorProperties = connectorProperties;
    }
}
