/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.smtp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;

import com.mirth.connect.model.ComponentProperties;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class SmtpSenderProperties implements ComponentProperties {
    public static final String name = "SMTP Sender";

    public static final String DATATYPE = "DataType";
    public static final String SMTP_HOST = "smtpHost";
    public static final String SMTP_PORT = "smtpPort";
    public static final String SMTP_SECURE = "encryption";
    public static final String SMTP_AUTHENTICATION = "authentication";
    public static final String SMTP_USERNAME = "username";
    public static final String SMTP_PASSWORD = "password";
    public static final String SMTP_TO = "to";
    public static final String SMTP_REPLYTO = "replyTo";
    public static final String SMTP_HEADERS = "headers";
    public static final String SMTP_FROM = "from";
    public static final String SMTP_SUBJECT = "subject";
    public static final String SMTP_BODY = "body";
    public static final String SMTP_HTML = "html";
    public static final String SMTP_ATTACHMENTS = "attachments";

    public Properties getDefaults() {
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        
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
        properties.put(SMTP_HEADERS, serializer.toXML(new LinkedHashMap<String, String>()));
        properties.put(SMTP_SUBJECT, "");
        properties.put(SMTP_BODY, "");
        properties.put(SMTP_HTML, "0");
        properties.put(SMTP_ATTACHMENTS, serializer.toXML(new ArrayList<Attachment>()));
        return properties;
    }

    public static String getInformation(Properties properties) {
        return "From: " + properties.getProperty(SMTP_FROM) + " To: " + properties.getProperty(SMTP_TO) + " SMTP Info: " + properties.getProperty(SMTP_HOST) + ":" + properties.getProperty(SMTP_PORT);
    }

}
