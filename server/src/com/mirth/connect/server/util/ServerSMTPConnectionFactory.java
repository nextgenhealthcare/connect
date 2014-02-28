/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;

public class ServerSMTPConnectionFactory {
    public static ServerSMTPConnection createSMTPConnection() throws ControllerException {
        try {
            TemplateValueReplacer replacer = new TemplateValueReplacer();
            ServerSettings settings = ControllerFactory.getFactory().createConfigurationController().getServerSettings();
            return new ServerSMTPConnection(replacer.replaceValues(settings.getSmtpHost()), replacer.replaceValues(settings.getSmtpPort()), Integer.parseInt(replacer.replaceValues(settings.getSmtpTimeout())), settings.getSmtpAuth(), settings.getSmtpSecure(), replacer.replaceValues(settings.getSmtpUsername()), replacer.replaceValues(settings.getSmtpPassword()), replacer.replaceValues(settings.getSmtpFrom()));
        } catch (Exception e) {
            if (e instanceof ControllerException) {
                throw (ControllerException) e;
            }
            throw new ControllerException(e);
        }
    }
}
