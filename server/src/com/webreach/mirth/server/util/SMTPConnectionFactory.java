/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.util;

import java.util.Properties;

import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.util.PropertyLoader;

public class SMTPConnectionFactory {
    public static SMTPConnection createSMTPConnection() throws Exception {
        Properties properties = ControllerFactory.getFactory().createConfigurationController().getServerProperties();
        String host = PropertyLoader.getProperty(properties, "smtp.host");
        String port = PropertyLoader.getProperty(properties, "smtp.port");
        boolean auth = PropertyLoader.getProperty(properties, "smtp.auth").equals("1");
        String secure = PropertyLoader.getProperty(properties, "smtp.secure");
        String username = PropertyLoader.getProperty(properties, "smtp.username");
        String password = PropertyLoader.getProperty(properties, "smtp.password");
        return new SMTPConnection(host, port, auth, secure, username, password);
    }
}
