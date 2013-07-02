/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.event;

import com.mirth.connect.donkey.model.event.ConnectorEventType;
import com.mirth.connect.donkey.model.event.Event;

public class ConnectorEvent extends Event {

    private String channelId;
    private Integer metaDataId;
    private String connectorName;
    private ConnectorEventType state;
    private String message;

    public ConnectorEvent(String channelId, Integer metaDataId, String connectorName, ConnectorEventType state) {
        this(channelId, metaDataId, connectorName, state, "");
    }

    public ConnectorEvent(String channelId, Integer metaDataId, String connectorName, ConnectorEventType state, String message) {
        this.channelId = channelId;
        this.metaDataId = metaDataId;
        this.connectorName = connectorName;
        this.state = state;
        this.message = message;
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

    public String getConnectorName() {
        return connectorName;
    }

    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    public ConnectorEventType getState() {
        return state;
    }

    public void setState(ConnectorEventType state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
