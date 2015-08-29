/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.smtp;

import java.util.Properties;

import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.util.TemplateValueReplacer;

public class SmtpSenderService implements ConnectorService {

    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    @Override
    public Object invoke(String channelId, String channelName, String method, Object object, String sessionId) throws Exception {
        if (method.equals("sendTestEmail")) {
            SmtpDispatcherProperties props = (SmtpDispatcherProperties) object;
            Properties properties = new Properties();

            properties.put("port", replacer.replaceValues(props.getSmtpPort(), channelId, channelName));
            properties.put("encryption", props.getEncryption());
            properties.put("host", replacer.replaceValues(props.getSmtpHost(), channelId, channelName));
            properties.put("timeout", props.getTimeout());
            properties.put("authentication", String.valueOf(props.isAuthentication()));
            properties.put("username", replacer.replaceValues(props.getUsername(), channelId, channelName));
            properties.put("password", replacer.replaceValues(props.getPassword(), channelId, channelName));
            properties.put("toAddress", replacer.replaceValues(props.getTo(), channelId, channelName));
            properties.put("fromAddress", replacer.replaceValues(props.getFrom(), channelId, channelName));

            return ConfigurationController.getInstance().sendTestEmail(properties);
        }

        return null;
    }
}