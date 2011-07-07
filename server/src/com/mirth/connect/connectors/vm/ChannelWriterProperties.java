/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.vm;

import java.util.Properties;

import com.mirth.connect.model.ComponentProperties;

public class ChannelWriterProperties implements ComponentProperties {
    public static final String name = "Channel Writer";
    public static final String DATATYPE = "DataType";
    public static final String CHANNEL_ID = "host";
    public static final String CHANNEL_SYNCHRONOUS = "synchronised";
    public static final String CHANNEL_TEMPLATE = "template";

    public Properties getDefaults() {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(CHANNEL_ID, "sink");
        properties.put(CHANNEL_SYNCHRONOUS, "0");
        properties.put(CHANNEL_TEMPLATE, "${message.encodedData}");
        return properties;
    }

    public static String getInformation(Properties properties) {
        return properties.getProperty(CHANNEL_ID);
    }
}
