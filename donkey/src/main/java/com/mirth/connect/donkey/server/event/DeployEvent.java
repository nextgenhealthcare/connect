/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.event;

import java.util.Map;

import com.mirth.connect.donkey.model.event.ChannelEventType;
import com.mirth.connect.donkey.model.message.Status;

public class DeployEvent extends ChannelEvent {

    private Map<Integer, Map<Status, Long>> connectorStatistics;

    public DeployEvent(String channelId, Map<Integer, Map<Status, Long>> connectorStatistics, ChannelEventType state) {
        super(channelId, state);
        this.connectorStatistics = connectorStatistics;
    }

    public Map<Integer, Map<Status, Long>> getConnectorStatistics() {
        return connectorStatistics;
    }

}
