package com.webreach.mirth.plugins;

import java.net.Socket;

import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;

public interface ConnectorStatusPlugin {
    public abstract void updateStatus(String connectorName, ConnectorType type, Event event, Socket socket);
}
