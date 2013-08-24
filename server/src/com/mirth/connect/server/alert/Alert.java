/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.alert;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.velocity.tools.generic.DateTool;

import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.server.controllers.ConfigurationController;

public class Alert {

    private AlertModel model;
    private Long enabledDateTime;
    private Long enabledNanoTime;
    private Map<Object, Object> properties;
    private AtomicInteger alertedCount;

    public Alert(AlertModel model) {
        this.model = model;
        enabledDateTime = System.nanoTime();
        enabledNanoTime = System.nanoTime();
        alertedCount = new AtomicInteger(0);
        properties = new HashMap<Object, Object>();
    }

    public AlertModel getModel() {
        return model;
    }

    public Long getEnabledDateTime() {
        return enabledDateTime;
    }

    public Long getEnabledNanoTime() {
        return enabledNanoTime;
    }

    public Map<Object, Object> getProperties() {
        return properties;
    }

    public Map<String, Object> createContext() {
        Map<String, Object> context = new HashMap<String, Object>();

        context.put("alertId", model.getId());
        context.put("alertName", model.getName());
        context.put("serverId", ConfigurationController.getInstance().getServerId());
        context.put("date", new DateTool());

        return context;
    }

    public int getAlertedCount() {
        return alertedCount.get();
    }

    public void incrementAlertedCount() {
        alertedCount.incrementAndGet();
    }

    public void resetAlertedCount() {
        alertedCount.set(0);
    }

}
