/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.event;

import java.util.Map;

import com.mirth.connect.donkey.model.event.DeployedStateEventType;
import com.mirth.connect.donkey.model.event.Event;
import com.mirth.connect.donkey.model.message.Status;

public class DeployedStateEvent extends Event {

    private String channelId;
    private Integer metaDataId;
    private DeployedStateEventType state;
    private Map<Integer, Map<Status, Long>> connectorStatistics;

    public DeployedStateEvent(String channelId, Integer metaDataId, DeployedStateEventType state) {
        this(channelId, metaDataId, state, null);
    }

    public DeployedStateEvent(String channelId, Integer metaDataId, DeployedStateEventType state, Map<Integer, Map<Status, Long>> connectorStatistics) {
        this.channelId = channelId;
        this.metaDataId = metaDataId;
        this.state = state;
        this.connectorStatistics = connectorStatistics;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Integer getMetaDataId() {
        return metaDataId;
    }

    public void setMetaDataId(Integer metaDataId) {
        this.metaDataId = metaDataId;
    }

    public DeployedStateEventType getState() {
        return state;
    }

    public void setState(DeployedStateEventType state) {
        this.state = state;
    }

    public Map<Integer, Map<Status, Long>> getConnectorStatistics() {
        return connectorStatistics;
    }
}