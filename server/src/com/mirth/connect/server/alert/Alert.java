package com.mirth.connect.server.alert;

import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.model.alert.AlertModel;

public class Alert {

    private AlertModel model;
    private Map<String, Object> context = new HashMap<String, Object>();
    private Map<Integer, Object> properties = new HashMap<Integer, Object>();
    
    protected Alert() {
        
    }
    
    public Alert(AlertModel model) {
        this.model = model;
    }

    public AlertModel getModel() {
        return model;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public Map<Integer, Object> getProperties() {
        return properties;
    }

}
