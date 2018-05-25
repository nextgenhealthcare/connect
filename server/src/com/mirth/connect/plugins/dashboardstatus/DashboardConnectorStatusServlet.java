/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.dashboardstatus;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.server.api.CheckAuthorizedChannelId;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.ControllerFactory;

public class DashboardConnectorStatusServlet extends MirthServlet implements DashboardConnectorStatusServletInterface {

    private static final DashboardConnectorStatusMonitor monitor = (DashboardConnectorStatusMonitor) ControllerFactory.getFactory().createExtensionController().getServicePlugins().get(PLUGIN_POINT);

    public DashboardConnectorStatusServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, PLUGIN_POINT);
    }

    @Override
    public Map<String, Object[]> getConnectorStateMap(String serverId) {
        return monitor.getConnectorListener().getConnectorStateMap(serverId);
    }

    @Override
    public Map<String, String> getChannelStates() {
        Map<String, Object[]> connectorStates = redactChannelIds(monitor.getConnectorListener().getConnectorStateMap(""));
        Map<String, String> channelStates = new HashMap<String, String>();

        for (Entry<String, Object[]> entry : connectorStates.entrySet()) {
            if (StringUtils.contains(entry.getKey(), "_0")) {
                String channelId = StringUtils.remove(entry.getKey(), "_0");
                if (!isChannelRedacted(channelId)) {
                    channelStates.put(channelId, (String) entry.getValue()[1]);
                }
            }
        }

        return channelStates;
    }

    @Override
    @CheckAuthorizedChannelId
    public String getChannelState(String channelId) {
        Object[] stateArray = monitor.getConnectorListener().getConnectorStateMap("").get(channelId + "_0");
        if (stateArray == null) {
            throw new MirthApiException(Status.NOT_FOUND);
        } else {
            return (String) stateArray[1];
        }
    }

    @Override
    public LinkedList<ConnectionLogItem> getAllChannelLogs(String serverId, int fetchSize, Long lastLogId) {
        return monitor.getConnectorListener().getChannelLog(serverId, null, fetchSize, lastLogId);
    }

    @Override
    public LinkedList<ConnectionLogItem> getChannelLog(String serverId, String channelId, int fetchSize, Long lastLogId) {
        return monitor.getConnectorListener().getChannelLog(serverId, channelId, fetchSize, lastLogId);
    }

}