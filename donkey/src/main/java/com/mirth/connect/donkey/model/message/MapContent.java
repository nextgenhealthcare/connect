/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

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
