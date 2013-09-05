/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

import java.util.HashMap;
import java.util.Map;

public class MapContent {
    private Object content = new HashMap<String, Object>();
    private boolean encrypted = false;
    private transient boolean persisted = false;

    public MapContent() {

    }

    public MapContent(Map<String, Object> map, boolean persisted) {
        this.content = map;
        this.persisted = persisted;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap() {
        if (content instanceof Map) {
            return (Map<String, Object>) content;
        }
        
        return null;
    }

    public void setMap(Map<String, Object> map) {
        this.content = map;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }
}
