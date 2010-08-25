package com.mirth.connect.connectors.http;

import java.util.Properties;

import com.mirth.connect.model.ComponentProperties;


public class HttpListenerProperties implements ComponentProperties {
    public static final String name = "HTTP Listener";
    public static final String DATATYPE = "DataType";
    public static final String HTTP_HOST = "host";
    public static final String HTTP_PORT = "port";
    public static final String HTTP_RESPONSE = "receiverResponse";
    public static final String HTTP_BODY_ONLY = "receiverBodyOnly";
    public static final String HTTP_RESPONSE_CONTENT_TYPE = "receiverResponseContentType";
    public static final String HTTP_CHARSET = "receiverCharset";
    
    public Properties getDefaults() {
        Properties properties = new Properties();
        
        properties.put(DATATYPE, name);
        
        properties.put(HTTP_HOST, "0.0.0.0");
        properties.put(HTTP_PORT, "80");
        properties.put(HTTP_RESPONSE, "None");
        properties.put(HTTP_BODY_ONLY, "1");
        properties.put(HTTP_RESPONSE_CONTENT_TYPE, "text/plain");
        properties.put(HTTP_CHARSET, "UTF-8");
        return properties;
    }

}
