/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.dashboardstatus;

import java.net.Socket;

import com.mirth.connect.plugins.ConnectorStatusPlugin;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;

public class DashboardConnectorStatusMonitorLogger implements ConnectorStatusPlugin {
    private static final String DASHBOARD_STATUS_COLUMN_SERVER = "Dashboard Status Column Server";
    private DashboardConnectorStatusMonitor monitor;

    public DashboardConnectorStatusMonitorLogger() {
        monitor = (DashboardConnectorStatusMonitor) ControllerFactory.getFactory().createExtensionController().getLoadedServerPlugins().get(DASHBOARD_STATUS_COLUMN_SERVER);
    }

    public void updateStatus(String connectorName, ConnectorType type, Event event, Socket socket) {
        monitor.updateStatus(connectorName, type, event, socket);
    }

}
