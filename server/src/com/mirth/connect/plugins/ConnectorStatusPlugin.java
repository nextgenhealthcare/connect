/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.net.Socket;

import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;

public interface ConnectorStatusPlugin {
    public void start();

    public void stop();

    public void updateStatus(String connectorName, ConnectorType type, Event event, Socket socket);
}
