/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.event;

import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;

public class ConnectorCountEvent extends ConnectionStatusEvent {
    
    private Integer maximum;
    private Boolean increment;
    
    public ConnectorCountEvent(String channelId, Integer metaDataId, String connectorName, ConnectionStatusEventType state, String message, Integer maximum) {
        super(channelId, metaDataId, connectorName, state, message);

        this.maximum = maximum;
    }

    public ConnectorCountEvent(String channelId, Integer metaDataId, String connectorName, ConnectionStatusEventType state, String message, Boolean increment) {
        super(channelId, metaDataId, connectorName, state, message);

        this.increment = increment;
    }

    public Integer getMaximum() {
        return maximum;
    }

    public void setMaximum(Integer maximum) {
        this.maximum = maximum;
    }

    public Boolean isIncrement() {
        return increment;
    }

    public void setIncrement(Boolean increment) {
        this.increment = increment;
    }
}
