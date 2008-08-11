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

public class SMTPConnection {
    private String host;
    private int port;
    private boolean auth;
    private String username;
    private String password;

    public SMTPConnection(String host, int port, boolean auth, String username, String password) {
        this.host = host;
        this.port = port;
        this.auth = auth;
        this.username = username;
        this.password = password;
    }

    public void send(String toAddresses, String ccAddresses, String fromAddress, String subject, String body) throws Exception {
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", auth);
        properties.put("mail.debug", false);
        
        Session session = Session.getInstance(properties, null);
        Message message = new MimeMessage(session);
        
        if ((fromAddress != null) && (fromAddress.length() > 0)) {
            message.setFrom(new InternetAddress(fromAddress));    
        } else {
            throw new Exception("FROM address not specified.");
        }
        
        if ((toAddresses != null) && (toAddresses.length() > 0)) {
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddresses, false));
        } else {
            throw new Exception("TO address not specified.");
        }
        
        if ((ccAddresses != null) && (ccAddresses.length() > 0)) {
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccAddresses, false));    
        }
        
        message.setSubject(subject);
        message.setText(body);
        message.setSentDate(new Date());

        if (auth) {
            Transport transport = session.getTransport("smtp");
            transport.connect(host, username, password);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } else {
            Transport.send(message);
        }
    }
}
