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
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

public class SMTPConnection {
    private Logger logger = Logger.getLogger(this.getClass());
    private String host;
    private int port;
    private String username;
    private String password;

    public SMTPConnection(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public void send(String to, String cc, String from, String subject, String body) {
        try {
            Properties properties = System.getProperties();

            // attaching to default Session, or we could start a new one --
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", port);
            
            // ast: set auth explicity false/true to avoid authentication
            // problems
            if (username == null) {
                properties.put("mail.smtp.auth", "false");
            } else {
                properties.put("mail.smtp.auth", "true");
            }   
            
            Session session = Session.getInstance(properties, null);
            session.setDebug(false);
            
            // create a new message
            Message message = new MimeMessage(session);

            // set the FROM and TO fields
            if (from.length() > 0) {
                message.setFrom(new InternetAddress(from));
            } else {
                // set a default from address if there was none specified
                message.setFrom();
            }

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));

            // include CC recipients
            if (cc != null) {
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
            }

            // set the subject and body text
            message.setSubject(subject);
            message.setText(body);

            // set some other header information
            message.setSentDate(new Date());

            // send the message
            if (username != null) {
                Transport transport = session.getTransport("smtp");
                transport.connect(host, username, password);
                transport.sendMessage(message, message.getAllRecipients());
                transport.close();
            } else {
                Transport.send(message, message.getAllRecipients());
            }
        } catch (Exception e) {
            logger.warn("Could not send email message.", e);
        }
    }
}
