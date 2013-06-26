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

public class ResponseMap extends HashMap<String, Object> {
    
    private Map<String, String> destinationNameMap;
    
    public ResponseMap(Map<String, Object> m, Map<String, String> destinationNameMap) {
        super(m);
        this.destinationNameMap = destinationNameMap;
    }
    
    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(key) || (destinationNameMap != null && destinationNameMap.containsKey(key) && super.containsKey(destinationNameMap.get(key)));
    }

    @Override
    public Object get(Object key) {
        Object value = super.get(key);
        if (value == null && destinationNameMap != null && destinationNameMap.containsKey(key)) {
            value = super.get(destinationNameMap.get(key));
        }
        return value;
    }
}
