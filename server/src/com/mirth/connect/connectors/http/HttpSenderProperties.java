/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.util.Properties;

import com.mirth.connect.model.QueuedSenderProperties;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class HttpSenderProperties extends QueuedSenderProperties {
    public static final String name = "HTTP Sender";
    public static final String DATATYPE = "DataType";
    public static final String HTTP_URL = "host";
    public static final String HTTP_METHOD = "dispatcherMethod";
    public static final String HTTP_HEADERS = "dispatcherHeaders";
    public static final String HTTP_PARAMETERS = "dispatcherParameters";
    public static final String HTTP_REPLY_CHANNEL_ID = "dispatcherReplyChannelId";
    public static final String HTTP_INCLUDE_HEADERS_IN_RESPONSE = "dispatcherIncludeHeadersInResponse";
    public static final String HTTP_MULTIPART = "dispatcherMultipart";
    public static final String HTTP_USE_AUTHENTICATION = "dispatcherUseAuthentication";
    public static final String HTTP_AUTHENTICATION_TYPE = "dispatcherAuthenticationType";
    public static final String HTTP_USERNAME = "dispatcherUsername";
    public static final String HTTP_PASSWORD = "dispatcherPassword";
    public static final String HTTP_CONTENT = "dispatcherContent";
    public static final String HTTP_CONTENT_TYPE = "dispatcherContentType";
    public static final String HTTP_CHARSET = "dispatcherCharset";
    public static final String HTTP_SOCKET_TIMEOUT = "dispatcherSocketTimeout";

    public Properties getDefaults() {
        Properties properties = super.getDefaults();
        
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();

        properties.put(DATATYPE, name);
        properties.put(HTTP_URL, "");

        properties.put(HTTP_METHOD, "post");
        properties.put(HTTP_HEADERS, serializer.toXML(new Properties()));
        properties.put(HTTP_PARAMETERS, serializer.toXML(new Properties()));
        properties.put(HTTP_REPLY_CHANNEL_ID, "sink");
        properties.put(HTTP_INCLUDE_HEADERS_IN_RESPONSE, "0");
        properties.put(HTTP_MULTIPART, "0");
        properties.put(HTTP_USE_AUTHENTICATION, "0");
        properties.put(HTTP_AUTHENTICATION_TYPE, "Basic");
        properties.put(HTTP_USERNAME, "");
        properties.put(HTTP_PASSWORD, "");
        properties.put(HTTP_CONTENT, "");
        properties.put(HTTP_CONTENT_TYPE, "text/plain");
        properties.put(HTTP_CHARSET, "UTF-8");
        properties.put(HTTP_SOCKET_TIMEOUT, "30000");

        return properties;
    }

    public static String getInformation(Properties properties) {
        return "Host: " + properties.getProperty(HTTP_URL) + "   Method: " + properties.getProperty(HTTP_METHOD);
    }
}
