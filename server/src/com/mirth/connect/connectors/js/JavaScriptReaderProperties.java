/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.js;

import java.util.Properties;

import com.mirth.connect.model.ComponentProperties;

public class JavaScriptReaderProperties implements ComponentProperties {
    public static final String name = "JavaScript Reader";

    public static final String DATATYPE = "DataType";
    public static final String JAVASCRIPT_HOST = "host";
    public static final String JAVASCRIPT_POLLING_TYPE = "pollingType";
    public static final String JAVASCRIPT_POLLING_TIME = "pollingTime";
    public static final String JAVASCRIPT_POLLING_FREQUENCY = "pollingFrequency";
    public static final String JAVASCRIPT_SCRIPT = "script";

    public Properties getDefaults() {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(JAVASCRIPT_HOST, "sink");
        properties.put(JAVASCRIPT_POLLING_FREQUENCY, "5000");
        properties.put(JAVASCRIPT_POLLING_TYPE, "interval");
        properties.put(JAVASCRIPT_POLLING_TIME, "12:00 AM");
        properties.put(JAVASCRIPT_SCRIPT, "");
        return properties;
    }
}
