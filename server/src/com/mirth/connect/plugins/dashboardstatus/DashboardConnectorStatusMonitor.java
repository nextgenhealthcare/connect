/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.dashboardstatus;

import static com.mirth.connect.plugins.dashboardstatus.DashboardConnectorStatusServletInterface.PERMISSION_VIEW;
import static com.mirth.connect.plugins.dashboardstatus.DashboardConnectorStatusServletInterface.PLUGIN_POINT;

import java.util.Properties;

import com.mirth.connect.client.core.api.util.OperationUtil;
import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.plugins.ServicePlugin;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;

public class DashboardConnectorStatusMonitor implements ServicePlugin {

    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private DashboardConnectorEventListener connectorListener;

    @Override
    public String getPluginPointName() {
        return PLUGIN_POINT;
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

    public DashboardConnectorEventListener getConnectorListener() {
        return connectorListener;
    }

    @Override
    public void update(Properties properties) {}

    @Override
    public Properties getDefaultProperties() {
        return new Properties();
    }

    @Override
    public ExtensionPermission[] getExtensionPermissions() {
        ExtensionPermission viewPermission = new ExtensionPermission(PLUGIN_POINT, PERMISSION_VIEW, "Displays the connection status and history of the selected channel on the Dashboard.", OperationUtil.getOperationNamesForPermission(PERMISSION_VIEW, DashboardConnectorStatusServletInterface.class), new String[] {});
        return new ExtensionPermission[] { viewPermission };
    }
}