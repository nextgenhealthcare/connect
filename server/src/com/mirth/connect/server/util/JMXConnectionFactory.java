/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.management.remote.JMXConnector;

import com.mirth.connect.util.PropertyLoader;

public class JMXConnectionFactory {
    public static JMXConnection createJMXConnection() throws Exception {
        Properties properties = PropertyLoader.loadProperties("mirth");
        String port = PropertyLoader.getProperty(properties, "jmx.port");
        String jmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:" + port + "/server";
        String password = PropertyLoader.getProperty(properties, "jmx.password");
        Map<String, String[]> environment = new HashMap<String, String[]>();
        String[] credentials = { "admin", password };
        environment.put(JMXConnector.CREDENTIALS, credentials);
        return new JMXConnection(jmxUrl, "MirthConfiguration", environment);
    }
}
