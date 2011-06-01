/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.server.controllers.ControllerFactory;

public class SMTPConnectionFactory {
    public static SMTPConnection createSMTPConnection() throws Exception {
        ServerSettings settings = ControllerFactory.getFactory().createConfigurationController().getServerSettings();
        String host = settings.getSmtpHost();
        String port = settings.getSmtpPort();
        boolean auth = settings.getSmtpAuth();
        String secure = settings.getSmtpSecure();
        String username = settings.getSmtpUsername();
        String password = settings.getSmtpPassword();
        return new SMTPConnection(host, port, auth, secure, username, password);
    }
}
