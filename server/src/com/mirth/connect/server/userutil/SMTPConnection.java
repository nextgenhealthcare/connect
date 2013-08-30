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

/**
 * Used to send e-mail messages.
 */
public class SMTPConnection {
    private com.mirth.connect.server.util.ServerSMTPConnection smtpConnection;

    /**
     * Instantiates an SMTP connection used to send e-mail messages with.
     * 
     * @param host
     *            - The SMTP server address.
     * @param port
     *            - The SMTP server port (e.g. 25, 587, 465).
     * @param socketTimeout
     *            - The socket connection timeout value in milliseconds.
     * @param useAuthentication
     *            - Determines whether authentication is needed for the SMTP
     *            server.
     * @param secure
     *            - The encryption security layer to use for the SMTP connection
     *            ("TLS" or "SSL"). If left blank, no encryption layer will be
     *            used.
     * @param username
     *            - If authentication is required, the username to authenticate
     *            with.
     * @param password
     *            - If authentication is required, the password to authenticate
     *            with.
     * @param from
     *            - The FROM field to use for dispatched e-mail messages.
     */
    public SMTPConnection(String host, String port, int socketTimeout, boolean useAuthentication, String secure, String username, String password, String from) {
        smtpConnection = new com.mirth.connect.server.util.ServerSMTPConnection(host, port, socketTimeout, useAuthentication, secure, username, password, from);
    }

    /**
     * Instantiates an SMTP connection used to send e-mail messages with.
     * 
     * @param host
     *            - The SMTP server address.
     * @param port
     *            - The SMTP server port (e.g. 25, 587, 465).
     * @param useAuthentication
     *            - Determines whether authentication is needed for the SMTP
     *            server.
     * @param secure
     *            - The encryption security layer to use for the SMTP connection
     *            ("TLS" or "SSL"). If left blank, no encryption layer will be
     *            used.
     * @param username
     *            - If authentication is required, the username to authenticate
     *            with.
     * @param password
     *            - If authentication is required, the password to authenticate
     *            with.
     * @param from
     *            - The FROM field to use for the e-mail.
     */
    public SMTPConnection(String host, String port, boolean useAuthentication, String secure, String username, String password, String from) {
        smtpConnection = new com.mirth.connect.server.util.ServerSMTPConnection(host, port, useAuthentication, secure, username, password, from);
    }

    SMTPConnection(com.mirth.connect.server.util.ServerSMTPConnection smtpConnection) {
        this.smtpConnection = smtpConnection;
    }

    /**
     * Returns the SMTP server address.
     */
    public String getHost() {
        return smtpConnection.getHost();
    }

    /**
     * Sets the SMTP server address.
     * 
     * @param host
     *            - The SMTP server address to use.
     */
    public void setHost(String host) {
        smtpConnection.setHost(host);
    }

    /**
     * Returns the SMTP server port.
     */
    public String getPort() {
        return smtpConnection.getPort();
    }

    /**
     * Sets the SMTP server port.
     * 
     * @param port
     *            - The SMTP server port to use (e.g. 25, 587, 465).
     */
    public void setPort(String port) {
        smtpConnection.setPort(port);
    }

    /**
     * Returns true if authentication is needed for the SMTP server, otherwise
     * returns false.
     */
    public boolean isUseAuthentication() {
        return smtpConnection.isUseAuthentication();
    }

    /**
     * Sets whether authentication is needed for the SMTP server.
     * 
     * @param useAuthentication
     *            - Determines whether authentication is needed for the SMTP
     *            server.
     */
    public void setUseAuthentication(boolean useAuthentication) {
        smtpConnection.setUseAuthentication(useAuthentication);
    }

    /**
     * Returns the encryption security layer being used for the SMTP connection
     * (e.g "TLS" or "SSL").
     */
    public String getSecure() {
        return smtpConnection.getSecure();
    }

    /**
     * Sets the encryption security layer to use for the SMTP connection.
     * 
     * @param secure
     *            - The encryption security layer to use for the SMTP connection
     *            ("TLS" or "SSL"). If left blank, no encryption layer will be
     *            used.
     */
    public void setSecure(String secure) {
        smtpConnection.setSecure(secure);
    }

    /**
     * Returns the username being used to authenticate to the SMTP server.
     */
    public String getUsername() {
        return smtpConnection.getUsername();
    }

    /**
     * Sets the username to use to authenticate to the SMTP server.
     * 
     * @param username
     *            - The username to authenticate with.
     */
    public void setUsername(String username) {
        smtpConnection.setUsername(username);
    }

    /**
     * Returns the password being used to authenticate to the SMTP server.
     */
    public String getPassword() {
        return smtpConnection.getPassword();
    }

    /**
     * Sets the password to use to authenticate to the SMTP server.
     * 
     * @param password
     *            - The password to authenticate with.
     */
    public void setPassword(String password) {
        smtpConnection.setPassword(password);
    }

    /**
     * Returns the FROM field being used for dispatched e-mail messages.
     */
    public String getFrom() {
        return smtpConnection.getFrom();
    }

    /**
     * Sets the FROM field to use for dispatched e-mail messages.
     * 
     * @param from
     *            - The FROM field to use for dispatched e-mail messages.
     */
    public void setFrom(String from) {
        smtpConnection.setFrom(from);
    }

    /**
     * Returns the socket connection timeout value in milliseconds.
     */
    public int getSocketTimeout() {
        return smtpConnection.getSocketTimeout();
    }

    /**
     * Sets the socket connection timeout value.
     * 
     * @param socketTimeout
     *            - The socket connection timeout value in milliseconds.
     */
    public void setSocketTimeout(int socketTimeout) {
        smtpConnection.setSocketTimeout(socketTimeout);
    }

    /**
     * Sends an e-mail message.
     * 
     * @param toList
     *            - A string representing a list of e-mail addresses to send the
     *            message to (separated by ",").
     * @param ccList
     *            - A string representing a list of e-mail addresses to copy the
     *            message to (separated by ",").
     * @param from
     *            - The FROM field to use for the e-mail message.
     * @param subject
     *            - The subject of the e-mail message.
     * @param body
     *            - The content of the e-mail message.
     * @param charset
     *            - The charset encoding to use when sending the e-mail message.
     * @throws EmailException
     */
    public void send(String toList, String ccList, String from, String subject, String body, String charset) throws EmailException {
        smtpConnection.send(toList, ccList, from, subject, body, charset);
    }

    /**
     * Sends an e-mail message.
     * 
     * @param toList
     *            - A string representing a list of e-mail addresses to send the
     *            message to (separated by ",").
     * @param ccList
     *            - A string representing a list of e-mail addresses to copy the
     *            message to (separated by ",").
     * @param from
     *            - The FROM field to use for the e-mail message.
     * @param subject
     *            - The subject of the e-mail message.
     * @param body
     *            - The content of the e-mail message.
     * @throws EmailException
     */
    public void send(String toList, String ccList, String from, String subject, String body) throws EmailException {
        smtpConnection.send(toList, ccList, from, subject, body);
    }

    /**
     * Sends an e-mail message.
     * 
     * @param toList
     *            - A string representing a list of e-mail addresses to send the
     *            message to (separated by ",").
     * @param ccList
     *            - A string representing a list of e-mail addresses to copy the
     *            message to (separated by ",").
     * @param subject
     *            - The subject of the e-mail message.
     * @param body
     *            - The content of the e-mail message.
     * @throws EmailException
     */
    public void send(String toList, String ccList, String subject, String body) throws EmailException {
        smtpConnection.send(toList, ccList, subject, body);
    }
}
