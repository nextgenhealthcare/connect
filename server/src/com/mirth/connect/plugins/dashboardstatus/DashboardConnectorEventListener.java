/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.dashboardstatus;

import java.awt.Color;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.Event;
import com.mirth.connect.donkey.server.event.EventType;
import com.mirth.connect.server.event.EventListener;

public class DashboardConnectorEventListener extends EventListener {

    private ConnectionStatusLogController logController;

    public DashboardConnectorEventListener() {
        logController = ConnectionStatusLogController.getInstance();
    }

    @Override
    protected void onShutdown() {}

    @Override
    public Set<EventType> getEventTypes() {
        return logController.getEventTypes();
    }

    @Override
    protected void processEvent(Event event) {
        logController.processEvent(event);
    }

    public Map<String, Object[]> getConnectorStateMap(String serverId) {
        return logController.getConnectorStateMap(serverId);
    }

    public synchronized LinkedList<ConnectionLogItem> getChannelLog(String serverId, String channelId, int fetchSize, Long lastLogId) {
        return logController.getChannelLog(serverId, channelId, fetchSize, lastLogId);
    }

    public Color getColor(ConnectionStatusEventType type) {
        return logController.getColor(type);
    }

}
