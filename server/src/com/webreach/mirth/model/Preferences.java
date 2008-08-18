package com.webreach.mirth.model;

import java.util.HashMap;
import java.util.Map;

public class Preferences {
    private Map<String, String> preferences = new HashMap<String, String>();
    
    public String get(String key) {
        return preferences.get(key);
    }
    
    public void put (String key, String value) {
        preferences.put(key, value);
    }
    
    public boolean isEmpty() {
        return preferences.isEmpty();
    }
}
