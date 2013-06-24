/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import org.apache.commons.mail.EmailException;

public class SMTPConnection {
    private com.mirth.connect.server.util.SMTPConnection smtpConnection;
    
    public SMTPConnection(String host, String port, int socketTimeout, boolean useAuthentication, String secure, String username, String password, String from) {
        smtpConnection = new com.mirth.connect.server.util.SMTPConnection(host, port, socketTimeout, useAuthentication, secure, username, password, from);
    }
    
    public SMTPConnection(String host, String port, boolean useAuthentication, String secure, String username, String password, String from) {
        smtpConnection = new com.mirth.connect.server.util.SMTPConnection(host, port, useAuthentication, secure, username, password, from);
    }
    
    SMTPConnection(com.mirth.connect.server.util.SMTPConnection smtpConnection) {
        this.smtpConnection = smtpConnection;
    }
    
    public String getHost() {
        return smtpConnection.getHost();
    }

    public void setHost(String host) {
        smtpConnection.setHost(host);
    }

    public String getPort() {
        return smtpConnection.getPort();
    }

    public void setPort(String port) {
        smtpConnection.setPort(port);
    }

    public boolean isUseAuthentication() {
        return smtpConnection.isUseAuthentication();
    }

    public void setUseAuthentication(boolean useAuthentication) {
        smtpConnection.setUseAuthentication(useAuthentication);
    }

    public String getSecure() {
        return smtpConnection.getSecure();
    }

    public void setSecure(String secure) {
        smtpConnection.setSecure(secure);
    }

    public String getUsername() {
        return smtpConnection.getUsername();
    }

    public void setUsername(String username) {
        smtpConnection.setUsername(username);
    }

    public String getPassword() {
        return smtpConnection.getPassword();
    }

    public void setPassword(String password) {
        smtpConnection.setPassword(password);
    }

    public String getFrom() {
        return smtpConnection.getFrom();
    }

    public void setFrom(String from) {
        smtpConnection.setFrom(from);
    }

    public int getSocketTimeout() {
        return smtpConnection.getSocketTimeout();
    }

    public void setSocketTimeout(int socketTimeout) {
        smtpConnection.setSocketTimeout(socketTimeout);
    }

    public void send(String toList, String ccList, String from, String subject, String body) throws EmailException {
        smtpConnection.send(toList, ccList, from, subject, body);
    }

    public void send(String toList, String ccList, String subject, String body) throws EmailException {
        smtpConnection.send(toList, ccList, subject, body);
    }
}
