/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.client.ui.util;

import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

public class PropertiesUtil {
    public static Properties convertMapToProperties(Map<String, String> map) {
        Properties properties = new Properties();
        for (Entry<String, String> entry : map.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }
        return properties;
    }
}
