/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.smtp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.util.ConnectionTestResponse;

public class SmtpSenderService implements ConnectorService {

    @Override
    public Object invoke(String method, Object object, String sessionId) throws Exception {
        if (method.equals("sendTestEmail")) {
            SmtpDispatcherProperties props = (SmtpDispatcherProperties) object;

            String host = props.getSmtpHost();

            int port = -1;
            try {
                port = Integer.parseInt(props.getSmtpPort());
            } catch (NumberFormatException e) {
                return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Invalid port: \"" + props.getSmtpPort() + "\"");
            }

            String secure = props.getEncryption();

            boolean authentication = props.isAuthentication();

            String username = props.getUsername();
            String password = props.getPassword();
            String to = props.getTo();
            String from = props.getFrom();

            Email email = new SimpleEmail();
            email.setDebug(true);
            email.setHostName(host);
            email.setSmtpPort(port);

            if ("SSL".equalsIgnoreCase(secure)) {
                email.setSSL(true);
            } else if ("TLS".equalsIgnoreCase(secure)) {
                email.setTLS(true);
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
