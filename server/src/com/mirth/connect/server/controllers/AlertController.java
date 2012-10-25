/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.List;

import com.mirth.connect.model.Alert;

public abstract class AlertController extends Controller {
    public static AlertController getInstance() {
        return ControllerFactory.getFactory().createAlertController();
    }
    
    public abstract List<Alert> getAlert(Alert alert) throws ControllerException;

    public abstract List<Alert> getAlertByChannelId(String channelId) throws ControllerException;

    public abstract void updateAlerts(List<Alert> alerts) throws ControllerException;

    public abstract void removeAlert(Alert alert) throws ControllerException;

    public abstract void sendAlerts(String channelId, String errorType, String customMessage, Throwable e);
}
