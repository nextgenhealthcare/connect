/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class SMTPConnection {
    private String host;
    private String port;
    private boolean useAuthentication;
    private String secure;
    private String username;
    private String password;
    private String from;

    public SMTPConnection(String host, String port, boolean auth, String secure, String username, String password, String from) {
        this.host = host;
        this.port = port;
        this.useAuthentication = auth;
        this.secure = secure;
        this.username = username;
        this.password = password;
        this.from = from;
    }

    public void send(String toList, String ccList, String from, String subject, String body) throws EmailException {
        Email email = new SimpleEmail();
        email.setHostName(host);
        email.setSmtpPort(Integer.parseInt(port));
        email.setSocketConnectionTimeout(5000);
        email.setDebug(true);
        
        if (useAuthentication) {
            email.setAuthentication(username, password);
        }
        
        if (StringUtils.equalsIgnoreCase(secure, "TLS")) {
            email.setTLS(true);
        } else if (StringUtils.equalsIgnoreCase(secure, "SSL")) {
            email.setSSL(true);
        }

        for (String to : StringUtils.split(toList, ",")) {
            email.addTo(to);
        }

        if (StringUtils.isNotEmpty(ccList)) {
            for (String cc : StringUtils.split(ccList, ",")) {
                email.addCc(cc);
            }
        }

        email.setFrom(from);
        email.setSubject(subject);
        email.setMsg(body);
        email.send();
    }
    
    public void send(String toList, String ccList, String subject, String body) throws EmailException {
        send(toList, ccList, from, subject, body);
    }
}
