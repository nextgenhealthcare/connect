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
    public static final String EMAIL_HOST = "dispatcherSmtpHost";
    public static final String EMAIL_PORT = "dispatcherSmtpPort";
    public static final String EMAIL_SECURE = "dispatcherEncryption";
    public static final String EMAIL_AUTHENTICATION = "dispatcherAuthentication";
    public static final String EMAIL_USERNAME = "dispatcherUsername";
    public static final String EMAIL_PASSWORD = "dispatcherPassword";
    public static final String EMAIL_TO = "dispatcherTo";
    public static final String EMAIL_REPLY_TO = "dispatcherReplyTo";
    public static final String EMAIL_FROM = "dispatcherFrom";
    public static final String EMAIL_SUBJECT = "dispatcherSubject";
    public static final String EMAIL_BODY = "dispatcherBody";
    public static final String EMAIL_HTML = "dispatcherHtml";
    public static final String EMAIL_ATTACHMENTS = "dispatcherAttachments";

    public Properties getDefaults() {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(EMAIL_HOST, "");
        properties.put(EMAIL_PORT, "25");
        properties.put(EMAIL_SECURE, "none");
        properties.put(EMAIL_AUTHENTICATION, "0");
        properties.put(EMAIL_USERNAME, "");
        properties.put(EMAIL_PASSWORD, "");
        properties.put(EMAIL_TO, "");
        properties.put(EMAIL_FROM, "");
        properties.put(EMAIL_SUBJECT, "");
        properties.put(EMAIL_BODY, "");
        properties.put(EMAIL_HTML, "0");
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        properties.put(EMAIL_ATTACHMENTS, serializer.toXML(new ArrayList<Attachment>()));
        return properties;
    }

    public static String getInformation(Properties properties) {
        return "From: " + properties.getProperty(EMAIL_FROM) + " To: " + properties.getProperty(EMAIL_TO) + " SMTP Info: " + properties.getProperty(EMAIL_HOST) + ":" + properties.getProperty(EMAIL_PORT);
    }

}
