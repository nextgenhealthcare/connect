/*
 * Copyright (c) Mirth Corporation. All rights reserved. http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.userutil.ImmutableConnectorMessage;

/**
 * This class allows extra message metadata to be passed in for use by the preprocessor script.
 * 
 * @deprecated This class is deprecated and will soon be removed; it exists only for preprocessor
 *             legacy support. Please retrieve values from the channel map instead.
 */
public class MuleContext {
    private Logger logger = Logger.getLogger(getClass());
    private ImmutableConnectorMessage connectorMessage;
    private MuleContextMap map;

    /**
     * Instantiates a MuleContext object.
     * 
     * @param connectorMessage
     *            The connector message that this object will reference for retrieving data.
     * 
     * @deprecated This class is deprecated and will soon be removed. Please retrieve values from
     *             the channel map instead.
     */
    public MuleContext(ImmutableConnectorMessage connectorMessage) {
        this.connectorMessage = connectorMessage;
    }

    /**
     * Returns a map of metadata accessible in the preprocessor script.
     * 
     * @deprecated This class is deprecated and will soon be removed.
     */
    public Map<String, Object> getProperties() {
        logger.error("The \"muleContext\" object is deprecated and will soon be removed. Please retrieve values from the channel map instead.");
        if (map == null) {
            map = new MuleContextMap(connectorMessage.getChannelMap());
        }
        return map;
    }

    private class MuleContextMap implements Map<String, Object> {

        private Map<String, Object> delegate;

        public MuleContextMap(Map<String, Object> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
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
        public Object get(Object key) {
            if ("messageId".equals(key)) {
                return connectorMessage.getMessageId();
            } else if ("destinations".equals(key)) {
                key = Constants.DESTINATION_META_DATA_IDS_KEY;
            }

            return delegate.get(key);
        }

        @Override
        public Object put(String key, Object value) {
            return delegate.put(key, value);
        }

        @Override
        public Object remove(Object key) {
            return delegate.remove(key);
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> m) {
            delegate.putAll(m);
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public Set<String> keySet() {
            return delegate.keySet();
        }

        @Override
        public Collection<Object> values() {
            return delegate.values();
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return delegate.entrySet();
        }
    }
}