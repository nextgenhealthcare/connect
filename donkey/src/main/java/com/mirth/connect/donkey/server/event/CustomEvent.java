package com.mirth.connect.donkey.server.event;

import java.util.Map;

import com.mirth.connect.donkey.model.event.Event;

public class CustomEvent extends Event {
    private String type;
    private Map<String, Object> properties;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
