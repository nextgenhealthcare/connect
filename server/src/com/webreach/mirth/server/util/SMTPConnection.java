/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.util;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

public class SMTPConnection {
    private Logger logger = Logger.getLogger(this.getClass());
    
    public static final String SECURE_SSL = "ssl";
    public static final String SECURE_TLS = "tls";

    private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    private static final String PROTOCOL_SMTP = "smtp";
    private static final String PROTOCOL_SMTPS = "smtps";

    private String host;
    private String port;
    private boolean auth;
    private String secure;
    private String username;
    private String password;

    public SMTPConnection(String host, String port, boolean auth, String secure, String username, String password) {
        this.host = host;
        this.port = port;
        this.auth = auth;
        this.secure = secure;
        this.username = username;
        this.password = password;
    }

    public void send(String toAddresses, String ccAddresses, String fromAddress, String subject, String body) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.timeout", "5000");

        if (secure.equalsIgnoreCase(SECURE_SSL)) {
            if (auth) {
                properties.put("mail.smtps.auth", "true");
            }

            properties.put("mail.smtps.host", host);
            properties.put("mail.smtps.socketFactory.port", port);
            properties.put("mail.smtps.socketFactory.class", SSL_FACTORY);
            properties.put("mail.smtps.socketFactory.fallback", "false");
        } else {
            if (auth) {
                properties.put("mail.smtp.auth", "true");
            }

            if (secure.equalsIgnoreCase(SECURE_TLS)) {
                properties.put("mail.smtp.starttls.enable", "true");
            }

            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", port);
        }

        Session session = Session.getInstance(properties);
        Message message = new MimeMessage(session);

        if ((fromAddress != null) && (fromAddress.length() > 0)) {
            message.setFrom(new InternetAddress(fromAddress));
        } else {
            throw new Exception("FROM address not specified.");
        }

        if ((toAddresses != null) && (toAddresses.length() > 0)) {
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddresses, false));
        } else {
            throw new Exception("TO address(es) not specified.");
        }

        if ((ccAddresses != null) && (ccAddresses.length() > 0)) {
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccAddresses, false));
        }

        message.setSubject(subject);
        message.setText(body);
        message.setSentDate(new Date());

        Transport transport = null;

        if (secure.equalsIgnoreCase(SECURE_SSL)) {
            transport = session.getTransport(PROTOCOL_SMTPS);
        } else {
            transport = session.getTransport(PROTOCOL_SMTP);
        }

        transport.addConnectionListener(new SMTPConnectionListener());
        transport.addTransportListener(new SMTPTransportListener());
        
        if (auth) {
            transport.connect(username, password);
        } else {
            transport.connect();
        }

        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }

    private class SMTPConnectionListener implements ConnectionListener {
        public void closed(ConnectionEvent e) {
            logger.info("SMTP connection opened.");
        }

        public void disconnected(ConnectionEvent e) {
            logger.error("SMTP connection disconnected.");
        }

        public void opened(ConnectionEvent e) {
            logger.info("SMTP connection opened.");
        }
    }

    private class SMTPTransportListener implements TransportListener {

        public void messageDelivered(TransportEvent e) {
            logger.info("SMTP message delievered.");
        }

        public void messageNotDelivered(TransportEvent e) {
            logger.error("SMTP message not delievered.");
        }

        public void messagePartiallyDelivered(TransportEvent e) {
            logger.error("SMTP message partially delievered.");
        }
    }
}
