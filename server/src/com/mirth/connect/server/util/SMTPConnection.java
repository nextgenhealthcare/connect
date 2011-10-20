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
    private int socketTimeout = 5000;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public boolean isUseAuthentication() {
        return useAuthentication;
    }

    public void setUseAuthentication(boolean useAuthentication) {
        this.useAuthentication = useAuthentication;
    }

    public String getSecure() {
        return secure;
    }

    public void setSecure(String secure) {
        this.secure = secure;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public SMTPConnection(String host, String port, boolean useAuthentication, String secure, String username, String password, String from) {
        this.host = host;
        this.port = port;
        this.useAuthentication = useAuthentication;
        this.secure = secure;
        this.username = username;
        this.password = password;
        this.from = from;
    }

    public void send(String toList, String ccList, String from, String subject, String body) throws EmailException {
        Email email = new SimpleEmail();
        email.setHostName(host);
        email.setSmtpPort(Integer.parseInt(port));
        email.setSocketConnectionTimeout(socketTimeout);
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
