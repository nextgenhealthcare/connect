/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import java.util.Properties;

import com.mirth.connect.model.ComponentProperties;
import com.mirth.connect.util.CharsetUtils;

public class TCPListenerProperties implements ComponentProperties {
    public static final String name = "TCP Listener";

    public static final String DATATYPE = "DataType";
    public static final String TCP_ADDRESS = "host";
    public static final String TCP_PORT = "port";
    public static final String TCP_RECEIVE_TIMEOUT = "receiveTimeout";
    public static final String TCP_BUFFER_SIZE = "bufferSize";
    public static final String TCP_KEEP_CONNECTION_OPEN = "keepSendSocketOpen";
    public static final String TCP_CHAR_ENCODING = "charEncoding";
    public static final String TCP_ACK_NEW_CONNECTION = "ackOnNewConnection";
    public static final String TCP_ACK_NEW_CONNECTION_IP = "ackIP";
    public static final String TCP_ACK_NEW_CONNECTION_PORT = "ackPort";
    public static final String TCP_RESPONSE_VALUE = "responseValue";
    public static final String CONNECTOR_CHARSET_ENCODING = "charsetEncoding";
    public static final String TCP_TYPE = "binary";

    public Properties getDefaults() {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(TCP_ADDRESS, "127.0.0.1");
        properties.put(TCP_PORT, "6661");
        properties.put(TCP_RECEIVE_TIMEOUT, "5000");
        properties.put(TCP_BUFFER_SIZE, "65536");
        properties.put(TCP_KEEP_CONNECTION_OPEN, "0");
        properties.put(TCP_ACK_NEW_CONNECTION, "0");
        properties.put(TCP_ACK_NEW_CONNECTION_IP, "");
        properties.put(TCP_ACK_NEW_CONNECTION_PORT, "");
        properties.put(TCP_RESPONSE_VALUE, "None");
        properties.put(CONNECTOR_CHARSET_ENCODING, CharsetUtils.DEFAULT_ENCODING);
        properties.put(TCP_TYPE, "0");
        return properties;
    }
}
