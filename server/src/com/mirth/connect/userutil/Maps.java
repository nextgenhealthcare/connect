/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.userutil;

import java.util.Map;

/**
 * Convenience class to allow fluent building of maps.
 */
@SuppressWarnings("rawtypes")
public class Maps {

    /**
     * Instantiates a new {@link MapBuilder} using a HashMap.
     * 
     * @return The new {@link MapBuilder} instance.
     */
    public static MapBuilder map() {
        return new MapBuilder();
    }

    /**
     * Instantiates a new {@link MapBuilder} using a HashMap and the given key/value entry.
     * 
     * @param key
     *            key with which the specified value is to be associated
     * @param value
     *            value to be associated with the specified key
     * @return The new {@link MapBuilder} instance.
     */
    public static MapBuilder map(Object key, Object value) {
        return new MapBuilder(key, value);
    }

    /**
     * Instantiates a new {@link MapBuilder} using the given map.
     * 
     * @return The new {@link MapBuilder} instance.
     */
    public static MapBuilder map(Map map) {
        return new MapBuilder(map);
    }
}