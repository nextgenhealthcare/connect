/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.connectors.vm;

import java.util.Properties;

import com.webreach.mirth.model.ComponentProperties;

public class ChannelReaderProperties implements ComponentProperties {
    public static final String name = "Channel Reader";

    public static final String DATATYPE = "DataType";
    public static final String CHANNEL_RESPONSE_VALUE = "responseValue";

    public Properties getDefaults() {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(CHANNEL_RESPONSE_VALUE, "None");
        return properties;
    }
}
