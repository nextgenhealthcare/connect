package com.mirth.connect.donkey.model.message;

import java.util.HashMap;
import java.util.Map;

public class MapContent {
    private Map<String, Object> map = new HashMap<String, Object>();
    private transient boolean persisted = false;

    public MapContent() {

    }
    
    public MapContent(Map<String, Object> map, boolean persisted) {
        this.map = map;
        this.persisted = persisted;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }
}
