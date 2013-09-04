/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A wrapper class for the response map which allows users to retrieve values
 * using the proper "d#" key (where "#" is the destination connector's metadata
 * ID), or by using the actual destination name.
 */
public class ResponseMap implements Map<String, Object> {

    private Map<String, Object> delegate;
    private Map<String, String> destinationNameMap;

    /**
     * Instantiates a new ResponseMap object.
     * 
     * @param delegate
     *            The underlying Map to reference for retrieving/setting data.
     * @param destinationNameMap
     *            A Map of destination names and their corresponding "d#"
     *            response map keys (where "#" is the destination connector
     *            metadata ID).
     */
    public ResponseMap(Map<String, Object> delegate, Map<String, String> destinationNameMap) {
        this.delegate = delegate;
        this.destinationNameMap = destinationNameMap;
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key) || (destinationNameMap != null && destinationNameMap.containsKey(key) && delegate.containsKey(destinationNameMap.get(key)));
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key. If the given key is not contained in
     * the underlying map, this object will traverse the destination name map in
     * an attempt to find the correct key.
     */
    @Override
    public Object get(Object key) {
        Object value = delegate.get(key);

        if (value == null && destinationNameMap != null && destinationNameMap.containsKey(key)) {
            value = delegate.get(destinationNameMap.get(key));
        }

        if (value != null && value instanceof com.mirth.connect.donkey.model.message.Response) {
            value = new Response((com.mirth.connect.donkey.model.message.Response) value);
        }

        return value;
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return delegate.keySet();
    }

    @Override
    public Object put(String key, Object value) {
        if (value != null && value instanceof Response) {
            value = ((Response) value).getDonkeyResponse();
        }

        Object result = delegate.put(key, value);

        if (result != null && result instanceof com.mirth.connect.donkey.model.message.Response) {
            result = new Response((com.mirth.connect.donkey.model.message.Response) result);
        }

        return result;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        delegate.putAll(m);
    }

    @Override
    public Object remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public Collection<Object> values() {
        return delegate.values();
    }
}