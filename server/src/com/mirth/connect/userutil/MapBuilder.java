/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.userutil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Convenience class to allow fluent building of maps.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class MapBuilder implements Map {

    private Map delegate;

    MapBuilder() {
        this(new HashMap());
    }

    MapBuilder(Object key, Object value) {
        this(new HashMap());
        put(key, value);
    }

    MapBuilder(Map map) {
        this.delegate = map;
    }

    /**
     * Adds an entry to the map using the {@link #put} method, and returns this builder.
     * 
     * @param key
     *            key with which the specified value is to be associated
     * @param value
     *            value to be associated with the specified key
     * @return This MapBuilder instance.
     */
    public MapBuilder add(Object key, Object value) {
        put(key, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return delegate.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(Object key) {
        return delegate.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object put(Object key, Object value) {
        return delegate.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object remove(Object key) {
        return delegate.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(Map m) {
        delegate.putAll(m);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        delegate.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<?> keySet() {
        return delegate.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<?> values() {
        return delegate.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<?> entrySet() {
        return delegate.entrySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return delegate.toString();
    }
}