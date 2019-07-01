/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.List;
import java.util.Map;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.alert.AlertStatus;
import com.mirth.connect.server.alert.Alert;
import com.mirth.connect.server.alert.AlertWorker;
import com.mirth.connect.server.alert.action.Protocol;

public abstract class AlertController extends Controller {
    public static AlertController getInstance() {
        return ControllerFactory.getFactory().createAlertController();
    }

    public abstract void initAlerts();

    public abstract void addWorker(AlertWorker alertWorker);

    public abstract void removeAllWorkers();

    public abstract List<AlertStatus> getAlertStatusList() throws ControllerException;

    public abstract AlertModel getAlert(String alertId) throws ControllerException;

    public abstract List<AlertModel> getAlerts() throws ControllerException;

    public abstract void updateAlert(AlertModel alert) throws ControllerException;

    public abstract void removeAlert(String alertId) throws ControllerException;

    public abstract void enableAlert(AlertModel alert) throws ControllerException;

    public abstract void disableAlert(String alertId) throws ControllerException;

    public abstract Alert getEnabledAlert(String alertId);

    public abstract Protocol getAlertActionProtocol(String protocolName);

    public abstract void registerAlertActionProtocol(Protocol protocol);

    public abstract Map<String, Map<String, String>> getAlertActionProtocolOptions();
}
