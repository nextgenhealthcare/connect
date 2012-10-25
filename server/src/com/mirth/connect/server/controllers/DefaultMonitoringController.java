/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.net.Socket;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mirth.connect.plugins.ConnectorStatusPlugin;

public class DefaultMonitoringController extends MonitoringController {
    private Logger logger = Logger.getLogger(this.getClass());
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();

    private static DefaultMonitoringController instance = null;

    private DefaultMonitoringController() {

    }

    public static MonitoringController create() {
        synchronized (DefaultMonitoringController.class) {
            if (instance == null) {
                instance = new DefaultMonitoringController();
            }

            return instance;
        }
    }

    @Override
    public void updateStatus(String channelId, int metaDataId, ConnectorType type, Event event, Socket socket, String information) {
        Map<String, ConnectorStatusPlugin> connectorStatusPlugins = extensionController.getConnectorStatusPlugins();

        for (ConnectorStatusPlugin plugin : connectorStatusPlugins.values()) {
            try {
                plugin.updateStatus(channelId, metaDataId, type, event, socket, information);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
