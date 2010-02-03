/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.connectors.http;

import java.util.Properties;

import com.webreach.mirth.model.QueuedSenderProperties;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

public class HTTPSenderProperties extends QueuedSenderProperties
{
    public static final String name = "HTTP Sender";
	
    public static final String DATATYPE = "DataType";
    public static final String HTTP_URL = "host";
    public static final String HTTP_METHOD = "method";
    public static final String HTTP_ADDITIONAL_PROPERTIES = "requestVariables";
    public static final String CHANNEL_ID = "replyChannelId";
    public static final String HTTP_HEADER_PROPERTIES = "headerVariables";
    public static final String HTTP_EXCLUDE_HEADERS = "excludeHeaders";
    public static final String HTTP_MULTIPART = "multipart";
    
    public Properties getDefaults()
    {
        Properties properties = super.getDefaults();
        properties.put(DATATYPE, name);
        properties.put(HTTP_URL, "");
        properties.put(HTTP_METHOD, "post");
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        
        Properties additionalProperties = new Properties();
        additionalProperties.put("$payload", "");
        properties.put(HTTP_ADDITIONAL_PROPERTIES, serializer.toXML(additionalProperties));
        
        properties.put(HTTP_HEADER_PROPERTIES, serializer.toXML(new Properties()));
        properties.put(HTTP_EXCLUDE_HEADERS, "0");
        properties.put(HTTP_MULTIPART, "0");
        properties.put(CHANNEL_ID, "sink");
        return properties;
    }

    public static String getInformation(Properties properties) {
        return "Host: " + properties.getProperty(HTTP_URL) + "   Method: " + properties.getProperty(HTTP_METHOD);
    }
}
