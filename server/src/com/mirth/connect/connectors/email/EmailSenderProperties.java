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
    public static final String EMAIL_ADDRESS = "hostname";
    public static final String EMAIL_PORT = "smtpPort";
    public static final String EMAIL_SECURE = "emailSecure";
    public static final String EMAIL_USE_AUTHENTICATION = "useAuthentication";
    public static final String EMAIL_USE_SERVER_SETTINGS = "useServerSettings";
    public static final String EMAIL_USERNAME = "username";
    public static final String EMAIL_PASSWORD = "password";
    public static final String EMAIL_TO = "toAddresses";
    public static final String EMAIL_FROM = "fromAddress";
    public static final String EMAIL_SUBJECT = "subject";
    public static final String EMAIL_BODY = "body";
    public static final String EMAIL_REPLY_TO = "replyToAddresses";
    public static final String EMAIL_CONTENT_TYPE = "contentType";
    public static final String EMAIL_ATTACHMENT_NAMES = "attachmentNames";
    public static final String EMAIL_ATTACHMENT_CONTENTS = "attachmentContents";
    public static final String EMAIL_ATTACHMENT_TYPES = "attachmentTypes";

    public Properties getDefaults() {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(EMAIL_ADDRESS, "");
        properties.put(EMAIL_PORT, "");
        properties.put(EMAIL_SECURE, "none");
        properties.put(EMAIL_USE_AUTHENTICATION, "0");
        properties.put(EMAIL_USE_SERVER_SETTINGS, "0");
        properties.put(EMAIL_USERNAME, "");
        properties.put(EMAIL_PASSWORD, "");
        properties.put(EMAIL_TO, "");
        properties.put(EMAIL_FROM, "");
        properties.put(EMAIL_SUBJECT, "");
        properties.put(EMAIL_BODY, "");
        properties.put(EMAIL_CONTENT_TYPE, "text/plain");
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        properties.put(EMAIL_ATTACHMENT_NAMES, serializer.toXML(new ArrayList()));
        properties.put(EMAIL_ATTACHMENT_CONTENTS, serializer.toXML(new ArrayList()));
        properties.put(EMAIL_ATTACHMENT_TYPES, serializer.toXML(new ArrayList()));
        return properties;
    }

    public static String getInformation(Properties properties) {
        return "From: " + properties.getProperty(EMAIL_FROM) + "   To: " + properties.getProperty(EMAIL_TO) + "   SMTP Info: " + properties.getProperty(EMAIL_ADDRESS) + ":" + properties.getProperty(EMAIL_PORT);
    }
}
