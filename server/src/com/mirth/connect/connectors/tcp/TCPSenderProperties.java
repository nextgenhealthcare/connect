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

import com.mirth.connect.model.QueuedSenderProperties;
import com.mirth.connect.util.CharsetUtils;

public class TCPSenderProperties extends QueuedSenderProperties {
    public static final String name = "TCP Sender";

    public static final String DATATYPE = "DataType";
    public static final String TCP_ADDRESS = "host";
    public static final String TCP_PORT = "port";
    public static final String TCP_SERVER_TIMEOUT = "sendTimeout";
    public static final String TCP_BUFFER_SIZE = "bufferSize";
    public static final String TCP_KEEP_CONNECTION_OPEN = "keepSendSocketOpen";
    public static final String TCP_MAX_RETRY_COUNT = "maxRetryCount";
    public static final String TCP_CHAR_ENCODING = "charEncoding";
    public static final String TCP_TEMPLATE = "template";
    public static final String TCP_ACK_TIMEOUT = "ackTimeout";
    public static final String CONNECTOR_CHARSET_ENCODING = "charsetEncoding";
    public static final String CHANNEL_ID = "replyChannelId";
    public static final String TCP_TYPE = "binary";

    public Properties getDefaults() {
        Properties properties = super.getDefaults();
        properties.put(DATATYPE, name);
        properties.put(TCP_ADDRESS, "127.0.0.1");
        properties.put(TCP_PORT, "6660");
        properties.put(TCP_SERVER_TIMEOUT, "5000");
        properties.put(TCP_BUFFER_SIZE, "65536");
        properties.put(TCP_KEEP_CONNECTION_OPEN, "0");
        properties.put(TCP_MAX_RETRY_COUNT, "2");
        properties.put(TCP_CHAR_ENCODING, "hex");
        properties.put(TCP_ACK_TIMEOUT, "5000");
        properties.put(CONNECTOR_CHARSET_ENCODING, CharsetUtils.DEFAULT_ENCODING);
        properties.put(TCP_TEMPLATE, "${message.encodedData}");
        properties.put(CHANNEL_ID, "sink");
        properties.put(TCP_TYPE, "0");
        return properties;
    }
}
