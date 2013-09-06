/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.smtp;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ConnectionTestResponse;

public class SmtpSenderService implements ConnectorService {

    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    @Override
    public Object invoke(String channelId, String method, Object object, String sessionId) throws Exception {
        if (method.equals("sendTestEmail")) {
            SmtpDispatcherProperties props = (SmtpDispatcherProperties) object;

            String host = replacer.replaceValues(props.getSmtpHost(), channelId);
            String portString = replacer.replaceValues(props.getSmtpPort(), channelId);

            int port = -1;
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Invalid port: \"" + portString + "\"");
            }

            String secure = props.getEncryption();

            boolean authentication = props.isAuthentication();

            String username = replacer.replaceValues(props.getUsername(), channelId);
            String password = replacer.replaceValues(props.getPassword(), channelId);
            String to = replacer.replaceValues(props.getTo(), channelId);
            String from = replacer.replaceValues(props.getFrom(), channelId);

            Email email = new SimpleEmail();
            email.setDebug(true);
            email.setHostName(host);
            email.setSmtpPort(port);

            try {
                int timeout = Integer.parseInt(props.getTimeout());
                email.setSocketTimeout(timeout);
                email.setSocketConnectionTimeout(timeout);
            } catch (NumberFormatException e) {
                // Don't set if the value is invalid
            }

            if ("SSL".equalsIgnoreCase(secure)) {
                email.setSSLOnConnect(true);
                email.setSslSmtpPort(portString);
            } else if ("TLS".equalsIgnoreCase(secure)) {
                email.setStartTLSEnabled(true);
            }

            if (authentication) {
                email.setAuthentication(username, password);
            }

            email.setSubject("Mirth Connect Test Email");

            try {
                for (String toAddress : StringUtils.split(to, ",")) {
                    email.addTo(toAddress);
                }

                email.setFrom(from);
                email.setMsg("Receipt of this email confirms that mail originating from this Mirth Connect Server is capable of reaching its intended destination.\n\nSMTP Configuration:\n- Host: " + host + "\n- Port: " + port);

                email.send();
                return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Sucessfully sent test email to: " + to);
            } catch (EmailException e) {
                return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, e.getMessage());
            }
        }

        return null;
    }

}
