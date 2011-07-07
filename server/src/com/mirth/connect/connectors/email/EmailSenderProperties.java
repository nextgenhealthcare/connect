/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.email;

import java.util.ArrayList;
import java.util.Properties;

import com.mirth.connect.model.ComponentProperties;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class EmailSenderProperties implements ComponentProperties {
    public static final String name = "Email Sender";

    public static final String DATATYPE = "DataType";
    public static final String SMTP_HOST = "dispatcherSmtpHost";
    public static final String SMTP_PORT = "dispatcherSmtpPort";
    public static final String SMTP_SECURE = "dispatcherEncryption";
    public static final String SMTP_AUTHENTICATION = "dispatcherAuthentication";
    public static final String SMTP_USERNAME = "dispatcherUsername";
    public static final String SMTP_PASSWORD = "dispatcherPassword";
    public static final String SMTP_TO = "dispatcherTo";
    public static final String SMTP_REPLY_TO = "dispatcherReplyTo";
    public static final String SMTP_FROM = "dispatcherFrom";
    public static final String SMTP_SUBJECT = "dispatcherSubject";
    public static final String SMTP_BODY = "dispatcherBody";
    public static final String SMTP_HTML = "dispatcherHtml";
    public static final String SMTP_ATTACHMENTS = "dispatcherAttachments";

    public Properties getDefaults() {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(SMTP_HOST, "");
        properties.put(SMTP_PORT, "25");
        properties.put(SMTP_SECURE, "none");
        properties.put(SMTP_AUTHENTICATION, "0");
        properties.put(SMTP_USERNAME, "");
        properties.put(SMTP_PASSWORD, "");
        properties.put(SMTP_TO, "");
        properties.put(SMTP_FROM, "");
        properties.put(SMTP_SUBJECT, "");
        properties.put(SMTP_BODY, "");
        properties.put(SMTP_HTML, "0");
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        properties.put(SMTP_ATTACHMENTS, serializer.toXML(new ArrayList<Attachment>()));
        return properties;
    }

    public static String getInformation(Properties properties) {
        return "From: " + properties.getProperty(SMTP_FROM) + " To: " + properties.getProperty(SMTP_TO) + " SMTP Info: " + properties.getProperty(SMTP_HOST) + ":" + properties.getProperty(SMTP_PORT);
    }

}
