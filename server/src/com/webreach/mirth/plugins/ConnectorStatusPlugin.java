package com.webreach.mirth.plugins;

import java.util.Properties;

import com.webreach.mirth.server.controllers.MonitoringController.Priority;
import com.webreach.mirth.server.controllers.MonitoringController.Status;

public interface ConnectorStatusPlugin
{
    public abstract void updateStatus(String connectorName, Status status, Priority priority, String connectionId);
}
