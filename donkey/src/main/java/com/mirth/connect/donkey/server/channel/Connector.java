/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.Map;
import java.util.Set;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.message.DataType;

public abstract class Connector {
    protected Channel channel;

    private String channelId;
    private int metaDataId;
    private DataType inboundDataType;
    private DataType outboundDataType;
    private DeployedState currentState = DeployedState.STOPPED;
    private ConnectorProperties connectorProperties;
    private Map<String, Integer> destinationIdMap;
    private FilterTransformerExecutor filterTransformerExecutor;
    private Set<String> resourceIds;

    public abstract void onDeploy() throws ConnectorTaskException;

    public abstract void onUndeploy() throws ConnectorTaskException;

    public abstract void onStart() throws ConnectorTaskException;

    public abstract void onStop() throws ConnectorTaskException;

    public abstract void onHalt() throws ConnectorTaskException;

    public abstract void start() throws ConnectorTaskException, InterruptedException;

    public abstract void stop() throws ConnectorTaskException, InterruptedException;

    public abstract void halt() throws ConnectorTaskException, InterruptedException;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

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

    public DeployedState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(DeployedState currentState) {
        this.currentState = currentState;
    }

    public ConnectorProperties getConnectorProperties() {
        return connectorProperties;
    }

    public void setConnectorProperties(ConnectorProperties connectorProperties) {
        this.connectorProperties = connectorProperties;
    }

    public Map<String, Integer> getDestinationIdMap() {
        return destinationIdMap;
    }

    public void setDestinationIdMap(Map<String, Integer> destinationIdMap) {
        this.destinationIdMap = destinationIdMap;
    }

    public FilterTransformerExecutor getFilterTransformerExecutor() {
        return filterTransformerExecutor;
    }

    public void setFilterTransformerExecutor(FilterTransformerExecutor filterTransformerExecutor) {
        this.filterTransformerExecutor = filterTransformerExecutor;
    }

    public Set<String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(Set<String> resourceIds) {
        this.resourceIds = resourceIds;
    }
}
