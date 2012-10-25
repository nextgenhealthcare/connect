/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;

public class SerializationUtilsCloner implements DonkeyCloner {
    /**
     * Deep clones an object using serialization. If the object to be cloned
     * is a ConnectorMap or ChannelMap and there was an error during cloning,
     * then this create a copy of the map, cloning all serializable values in
     * the original map. All non-serializable values are copied to the new map
     * without serialization. This is done so that users can still place
     * non-serializable objects into the maps (such as database connection
     * objects).
     */
    @Override
    public Object clone(Object object) {
        Object clonedObject = null;

        try {
            clonedObject = SerializationUtils.clone((Serializable) object);
        } catch (SerializationException e) {
            if (object instanceof Map) {
                clonedObject = cloneMap((HashMap<String, Object>) object);
            } else {
                throw e;
            }
        }
        return clonedObject;
    }

    /**
     * Returns a copy of a ConnectorMap or ChannelMap object, leaving all
     * non-serializable values as original objects.
     */
    private static Map<String, Object> cloneMap(Map<String, Object> map) {
        Map<String, Object> newMap = new HashMap<String, Object>();

        for (String key : map.keySet()) {
            try {
                // Attempt to clone the object
                newMap.put(key, SerializationUtils.clone((Serializable) map.get(key)));
            } catch (Exception e) {
                // If cloning failed, put the original object in the map
                newMap.put(key, map.get(key));
            }
        }

        return newMap;
    }
}
