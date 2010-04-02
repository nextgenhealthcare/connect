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

import com.webreach.mirth.model.ComponentProperties;

public class HTTPListenerProperties implements ComponentProperties {
    public static final String name = "HTTP Listener";

    public static final String DATATYPE = "DataType";
    public static final String HTTP_ADDRESS = "host";
    public static final String HTTP_PORT = "port";
    public static final String HTTP_RECEIVE_TIMEOUT = "keepAliveTimeout";
    public static final String HTTP_BUFFER_SIZE = "bufferSize";
    public static final String HTTP_KEEP_CONNECTION_OPEN = "keepAlive";
    public static final String HTTP_RESPONSE_VALUE = "responseValue";
    public static final String HTTP_EXTENDED_PAYLOAD = "extendedPayload";
    public static final String HTTP_PAYLOAD_ENCODING = "payloadEncoding";
    public static final String PAYLOAD_ENCODING_NONE = "None";
    public static final String PAYLOAD_ENCODING_ENCODE = "Encode";
    public static final String PAYLOAD_ENCODING_DECODE = "Decode";
    public static final String HTTP_APPEND_PAYLOAD = "appendPayload";

    public Properties getDefaults() {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(HTTP_ADDRESS, "127.0.0.1");
        properties.put(HTTP_PORT, "80");
        properties.put(HTTP_RECEIVE_TIMEOUT, "5000");
        properties.put(HTTP_BUFFER_SIZE, "65536");
        properties.put(HTTP_KEEP_CONNECTION_OPEN, "0");
        properties.put(HTTP_RESPONSE_VALUE, "None");
        properties.put(HTTP_EXTENDED_PAYLOAD, "1");
        properties.put(HTTP_PAYLOAD_ENCODING, PAYLOAD_ENCODING_NONE);
        properties.put(HTTP_APPEND_PAYLOAD, "0");
        return properties;
    }
}
