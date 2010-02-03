/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.connectors.js;

import java.util.Properties;

import com.webreach.mirth.model.ComponentProperties;

public class JavaScriptWriterProperties implements ComponentProperties {
    public static final String name = "JavaScript Writer";

    public static final String DATATYPE = "DataType";
    public static final String JAVASCRIPT_HOST = "host";
    public static final String JAVASCRIPT_SCRIPT = "script";

    public Properties getDefaults() {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(JAVASCRIPT_HOST, "sink");
        properties.put(JAVASCRIPT_SCRIPT, "");
        return properties;
    }
}
