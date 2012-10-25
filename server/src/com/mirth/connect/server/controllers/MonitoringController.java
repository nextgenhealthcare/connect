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

public abstract class MonitoringController extends Controller {
    public enum Event {
        CONNECTED, DISCONNECTED, INITIALIZED, BUSY, DONE, ATTEMPTING
    };

    public enum ConnectorType {
        LISTENER, SENDER, READER, WRITER
    };

    public static MonitoringController getInstance() {
        return ControllerFactory.getFactory().createMonitoringController();
    }

    public abstract void updateStatus(String channelId, int metaDataId, ConnectorType type, Event event, Socket socket, String information);

    public void updateStatus(String channelId, int metaDataId, ConnectorType type, Event event, Socket socket) {
        updateStatus(channelId, metaDataId, type, event, socket, null);
    }

    public void updateStatus(String channelId, int metaDataId, ConnectorType type, Event event, String information) {
        updateStatus(channelId, metaDataId, type, event, null, information);
    }

    public void updateStatus(String channelId, int metaDataId, ConnectorType type, Event event) {
        updateStatus(channelId, metaDataId, type, event, null, null);
    }

}
