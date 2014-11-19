package com.mirth.connect.server.userutil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

public class MessageParameters {
    private static Logger logger = Logger.getLogger(MessageParameters.class);
    private Map<String, List<String>> delegate;

    public MessageParameters(Map<String, List<String>> delegate) {
        this.delegate = delegate;
    }

    /**
     * Get the first parameter value for the given key.
     * 
     * @param key
     *            The name of the parameter key.
     * @return The associated value or null if no value exists.
     * @Deprecated This method is deprecated and will soon be removed. Please use getParameter(key)
     *             instead.
     */
    @Deprecated
    public String get(String key) {
        logger.error("This method is deprecated and will soon be removed. Please call getParameter(key) instead.");
        return getParameter(key);
    }

    /**
     * Get the first parameter value for the given key.
     * 
     * @param key
     *            The name of the parameter key.
     * @return The associated value or null if no value exists.
     * 
     */
    public String getParameter(String key) {
        List<String> list = delegate.get(key);

        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }

        return null;
    }

    /**
     * Get all parameter values for the given key.
     * 
     * @param key
     *            The name of parameter key.
     * @return A list of all parameter values for the given key or null if no values exist.
     * 
     */
    public List<String> getParameterList(String key) {
        List<String> list = delegate.get(key);

        if (CollectionUtils.isNotEmpty(list)) {
            return Collections.unmodifiableList(list);
        }

        return null;
    }

    /**
     * Get all parameter keys.
     * 
     * @return A set of all parameter keys.
     */
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(delegate.keySet());
    }

    /**
     * Check if parameters exist for a given key.
     * 
     * @param key
     *            The name of the parameter key.
     * @return true if parameters exist for the given key, false otherwise.
     */
    public boolean contains(String key) {
        return delegate.keySet().contains(key);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}