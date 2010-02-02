package com.webreach.mirth.plugins.dashboardstatus;

import java.net.Socket;

import com.webreach.mirth.plugins.ConnectorStatusPlugin;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;

public class DashboardConnectorStatusMonitorLogger implements ConnectorStatusPlugin {
    private static final String DASHBOARD_STATUS_COLUMN_SERVER = "Dashboard Status Column Server";
    private DashboardConnectorStatusMonitor monitor;

    public DashboardConnectorStatusMonitorLogger() {
        monitor = (DashboardConnectorStatusMonitor) ControllerFactory.getFactory().createExtensionController().getLoadedPlugins().get(DASHBOARD_STATUS_COLUMN_SERVER);
    }

    public void updateStatus(String connectorName, ConnectorType type, Event event, Socket socket) {
        monitor.updateStatus(connectorName, type, event, socket);
    }

}
