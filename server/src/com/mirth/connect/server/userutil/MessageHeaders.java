package com.mirth.connect.server.userutil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

public class MessageHeaders {
    private static Logger logger = Logger.getLogger(MessageHeaders.class);
    private Map<String, List<String>> delegate;

    public MessageHeaders(Map<String, List<String>> delegate) {
        this.delegate = delegate;
    }

    /**
     * Get the first header value for the given key.
     * 
     * @param key
     *            The name of the header key.
     * @return The associated value or null if no value exists.
     * @Deprecated This method is deprecated and will soon be removed. Please use getHeader(key)
     *             instead.
     */
    @Deprecated
    public String get(String key) {
        logger.error("This method is deprecated and will soon be removed. Please call getHeader(key) instead.");
        return getHeader(key);
    }

    /**
     * Get the first header value for the given key.
     * 
     * @param key
     *            The name of the header key.
     * @return The associated value or null if no value exists.
     * 
     */
    public String getHeader(String key) {
        List<String> list = delegate.get(key);

        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }

        return null;
    }

    /**
     * Get all header values for the given key.
     * 
     * @param key
     *            The name of header key.
     * @return A list of all header values for the given key or null if no values exist.
     * 
     */
    public List<String> getHeaderList(String key) {
        List<String> list = delegate.get(key);

        if (CollectionUtils.isNotEmpty(list)) {
            return Collections.unmodifiableList(list);
        }

        return null;
    }

    /**
     * Get all header keys.
     * 
     * @return A set of all header keys.
     */
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(delegate.keySet());
    }

    /**
     * Check if headers exists for a given key.
     * 
     * @param key
     *            The name of the header key.
     * @return true if headers exist for the given key, false otherwise.
     */
    public boolean contains(String key) {
        return delegate.keySet().contains(key);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
