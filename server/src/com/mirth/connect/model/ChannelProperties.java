/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.util.Properties;

public class ChannelProperties implements ComponentProperties
{
    public static final String SYNCHRONOUS = "synchronous";
    public static final String TRANSACTIONAL = "transactional";
    public static final String INITIAL_STATE = "initialState";
    public static final String DONT_STORE_FILTERED = "dont_store_filtered";
    public static final String ERROR_MESSAGES_ONLY = "error_messages_only";
    public static final String MAX_MESSAGE_AGE = "max_message_age";
    public static final String STORE_MESSAGES = "store_messages";
    public static final String ENCRYPT_DATA = "encryptData";
    public static final String CLEAR_GLOBAL_CHANNEL_MAP = "clearGlobalChannelMap";
    
    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(SYNCHRONOUS, "true");
        properties.put(TRANSACTIONAL, "false");
        properties.put(INITIAL_STATE, "started");
        properties.put(DONT_STORE_FILTERED, "false");
        properties.put(ERROR_MESSAGES_ONLY, "false");
        properties.put(MAX_MESSAGE_AGE, "-1");
        properties.put(STORE_MESSAGES, "true");
        properties.put(ENCRYPT_DATA, "false");
        properties.put(CLEAR_GLOBAL_CHANNEL_MAP, "true");
        return properties;
    }
}
