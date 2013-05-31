package com.mirth.connect.server.alert;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.server.controllers.ConfigurationController;

public class Alert {

    private AlertModel model;
    private Long enabledDateTime;
    private Long enabledNanoTime;
    private Map<Integer, Object> properties = new HashMap<Integer, Object>();
    private AtomicInteger alertedCount;
    
    public Alert(AlertModel model) {
        this.model = model;
        enabledDateTime = System.nanoTime();
        enabledNanoTime = System.nanoTime();
        alertedCount = new AtomicInteger(0);
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

    public Map<Integer, Object> getProperties() {
        return properties;
    }
    
    public Map<String, Object> createContext() {
        Map<String, Object> context = new HashMap<String, Object>();
        
        context.put("alertId", model.getId());
        context.put("alertName", model.getName());
        context.put("serverId", ConfigurationController.getInstance().getServerId());
        
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
