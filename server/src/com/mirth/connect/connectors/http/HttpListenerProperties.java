/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.util.HashMap;
import java.util.Properties;

import com.mirth.connect.model.ComponentProperties;
import com.mirth.connect.model.converters.ObjectXMLSerializer;


public class HttpListenerProperties implements ComponentProperties {
    public static final String name = "HTTP Listener";
    public static final String DATATYPE = "DataType";
    public static final String HTTP_HOST = "host";
    public static final String HTTP_PORT = "port";
    public static final String HTTP_RESPONSE = "receiverResponse";
    public static final String HTTP_BODY_ONLY = "receiverBodyOnly";
    public static final String HTTP_RESPONSE_CONTENT_TYPE = "receiverResponseContentType";
    public static final String HTTP_RESPONSE_STATUS_CODE = "receiverResponseStatusCode";
    public static final String HTTP_RESPONSE_HEADERS = "receiverResponseHeaders";
    public static final String HTTP_CHARSET = "receiverCharset";
    public static final String HTTP_CONTEXT_PATH = "receiverContextPath";
    public static final String HTTP_TIMEOUT = "receiverTimeout";
    
    public Properties getDefaults() {
        Properties properties = new Properties();
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        
        properties.put(DATATYPE, name);
        
        properties.put(HTTP_HOST, "0.0.0.0");
        properties.put(HTTP_PORT, "80");
        properties.put(HTTP_RESPONSE, "None");
        properties.put(HTTP_BODY_ONLY, "1");
        properties.put(HTTP_RESPONSE_CONTENT_TYPE, "text/plain");
        properties.put(HTTP_CHARSET, "UTF-8");
        properties.put(HTTP_CONTEXT_PATH, "");
        properties.put(HTTP_RESPONSE_STATUS_CODE, 0);
        properties.put(HTTP_RESPONSE_HEADERS, serializer.toXML(new HashMap<String, String>()));
        properties.put(HTTP_TIMEOUT, "0");
        return properties;
    }

}
