/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.mllp;

import java.util.Properties;

import com.mirth.connect.model.QueuedSenderProperties;
import com.mirth.connect.util.CharsetUtils;

public class LLPSenderProperties extends QueuedSenderProperties {
    public static final String name = "LLP Sender";

    public static final String DATATYPE = "DataType";
    public static final String LLP_PROTOCOL_NAME = "tcpProtocolClassName";
    public static final String LLP_PROTOCOL_NAME_VALUE = "org.mule.providers.tcp.protocols.TcpProtocol";
    public static final String LLP_ADDRESS = "host";
    public static final String LLP_PORT = "port";
    public static final String LLP_SERVER_TIMEOUT = "sendTimeout";
    public static final String LLP_BUFFER_SIZE = "bufferSize";
    public static final String LLP_KEEP_CONNECTION_OPEN = "keepSendSocketOpen";
    public static final String LLP_MAX_RETRY_COUNT = "maxRetryCount";
    public static final String LLP_CHAR_ENCODING = "charEncoding";
    public static final String LLP_START_OF_MESSAGE_CHARACTER = "messageStart";
    public static final String LLP_END_OF_MESSAGE_CHARACTER = "messageEnd";
    public static final String LLP_RECORD_SEPARATOR = "recordSeparator";
    public static final String LLP_SEGMENT_END = "segmentEnd";
    public static final String LLP_TEMPLATE = "template";
    public static final String LLP_ACK_TIMEOUT = "ackTimeout";
    public static final String LLP_QUEUE_ACK_TIMEOUT = "queueAckTimeout";
    public static final String LLP_HL7_ACK_RESPONSE = "processHl7AckResponse";
    public static final String CONNECTOR_CHARSET_ENCODING = "charsetEncoding";
    public static final String CHANNEL_ID = "replyChannelId";

    public Properties getDefaults() {
        Properties properties = super.getDefaults();
        properties.put(DATATYPE, name);
        properties.put(LLP_PROTOCOL_NAME, LLP_PROTOCOL_NAME_VALUE);
        properties.put(LLP_ADDRESS, "127.0.0.1");
        properties.put(LLP_PORT, "6660");
        properties.put(LLP_SERVER_TIMEOUT, "5000");
        properties.put(LLP_BUFFER_SIZE, "65536");
        properties.put(LLP_KEEP_CONNECTION_OPEN, "0");
        properties.put(LLP_MAX_RETRY_COUNT, "2");
        properties.put(LLP_CHAR_ENCODING, "hex");
        properties.put(LLP_START_OF_MESSAGE_CHARACTER, "0x0B");
        properties.put(LLP_END_OF_MESSAGE_CHARACTER, "0x1C");
        properties.put(LLP_RECORD_SEPARATOR, "0x0D");
        properties.put(LLP_SEGMENT_END, "0x0D");
        properties.put(LLP_ACK_TIMEOUT, "5000");
        properties.put(LLP_QUEUE_ACK_TIMEOUT, "1");
        properties.put(LLP_HL7_ACK_RESPONSE, "1");
        properties.put(CONNECTOR_CHARSET_ENCODING, CharsetUtils.DEFAULT_ENCODING);
        properties.put(LLP_TEMPLATE, "${message.encodedData}");
        properties.put(CHANNEL_ID, "sink");
        return properties;
    }
}
