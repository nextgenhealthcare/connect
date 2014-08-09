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
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.mirth.connect.donkey.server.Constants;

public class SourceMap implements Map<String, Object> {

    private Map<String, Object> delegate;

    /**
     * Instantiates a new SourceMap object.
     * 
     * @param delegate
     *            The underlying Map to reference for retrieving/setting data.
     */
    public SourceMap(Map<String, Object> delegate) {
        this.delegate = delegate;
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

    @Override
    public Object get(Object key) {
        Object object = delegate.get(key);

        if (Constants.DESTINATION_SET_KEY.equals(key)) {
            if (object != null && object instanceof Set) {
                return Collections.unmodifiableSet((Set) object);
            }
        }

        return object;
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