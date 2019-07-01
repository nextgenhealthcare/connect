/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * A wrapper class for the channel map that checks against the source map in the {@link #get(Object)
 * get(key)} method for legacy support.
 */
public class ChannelMap implements Map<String, Object> {

    private Logger logger = Logger.getLogger(getClass());
    private Map<String, Object> delegate;
    private Map<String, Object> sourceMap;

    /**
     * Instantiates a new ChannelMap object.
     * 
     * @param delegate
     *            The underlying Map to reference for retrieving/setting data.
     * @param sourceMap
     *            The source map associated with the current connector message. This is used to
     *            check against in the {@link #get(Object) get(key)} method for legacy support.
     */
    public ChannelMap(Map<String, Object> delegate, Map<String, Object> sourceMap) {
        this.delegate = delegate;
        this.sourceMap = sourceMap;
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
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
     * Returns the value to which the specified key is mapped, or null if this map contains no
     * mapping for the key. If the channel map does not contain the key but the source map does, an
     * error message is logged out notifying the user that the source map should be used instead.
     */
    @Override
    public Object get(Object key) {
        if (!delegate.containsKey(key) && sourceMap.containsKey(key)) {
            String keyString = String.valueOf(key);
            logger.error("The source map entry \"" + keyString + "\" was retrieved from the channel map. This method of retrieval has been deprecated and will soon be removed. Please use sourceMap.get('" + keyString + "') instead.");
            return sourceMap.get(key);
        }

        return delegate.get(key);
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
        return delegate.put(key, value);
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