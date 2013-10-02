/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.dashboardstatus;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.plugins.ServicePlugin;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;

public class DashboardConnectorStatusMonitor implements ServicePlugin {
    private Logger logger = Logger.getLogger(this.getClass());

    public static final String PLUGINPOINT = "Dashboard Connector Service";

    private static final String METHOD_GET_STATES = "getStates";
    private static final String METHOD_GET_CONNECTION_INFO_LOGS = "getConnectionInfoLogs";
    private static final String METHOD_REMOVE_SESSIONID = "removeSessionId";
    private static final String METHOD_CHANNELS_DEPLOYED = "channelsDeployed";

    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private DashboardConnectorEventListener connectorListener;

    @Override
    public String getPluginPointName() {
        return PLUGINPOINT;
    }

    @Override
    public void init(Properties properties) {}

    @Override
    public void start() {
        connectorListener = new DashboardConnectorEventListener();

        eventController.addListener(connectorListener);
    }

    @Override
    public void stop() {
        eventController.removeListener(connectorListener);
    }

    @Override
    public void update(Properties properties) {

    }

    @Override
    public Properties getDefaultProperties() {
        return new Properties();
    }

    @Override
    public Object invoke(String method, Object object, String sessionId) {
        if (method.equals(METHOD_GET_STATES)) {
            return connectorListener.getConnectorStateMap();
        } else if (method.equals(METHOD_GET_CONNECTION_INFO_LOGS)) {
            return connectorListener.getChannelLog(object, sessionId);
        } else if (method.equals(METHOD_CHANNELS_DEPLOYED)) {
            return connectorListener.channelDeployed(sessionId);
        } else if (method.equals(METHOD_REMOVE_SESSIONID)) {
            return connectorListener.removeSession(sessionId);
        }

        return null;
    }

    @Override
    public ExtensionPermission[] getExtensionPermissions() {
        ExtensionPermission viewPermission = new ExtensionPermission(PLUGINPOINT, "View Connection Status", "Displays the connection status and history of the selected channel on the Dashboard.", new String[] {
                METHOD_GET_STATES, METHOD_GET_CONNECTION_INFO_LOGS }, new String[] {});

        return new ExtensionPermission[] { viewPermission };
    }
}