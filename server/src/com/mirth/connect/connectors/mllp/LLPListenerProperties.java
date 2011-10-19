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

import com.mirth.connect.model.ComponentProperties;
import com.mirth.connect.util.CharsetUtils;

public class LLPListenerProperties implements ComponentProperties {
    public static final String name = "LLP Listener";

    public static final String DATATYPE = "DataType";
    public static final String LLP_PROTOCOL_NAME = "tcpProtocolClassName";
    public static final String LLP_PROTOCOL_NAME_VALUE = "org.mule.providers.tcp.protocols.TcpProtocol";
    public static final String LLP_SERVER_MODE = "serverMode";
    public static final String LLP_ADDRESS = "host";
    public static final String LLP_PORT = "port";
    public static final String LLP_RECONNECT_INTERVAL = "reconnectInterval";
    public static final String LLP_RECEIVE_TIMEOUT = "receiveTimeout";
    public static final String LLP_BUFFER_SIZE = "bufferSize";
    public static final String LLP_KEEP_CONNECTION_OPEN = "keepSendSocketOpen";
    public static final String LLP_CHAR_ENCODING = "charEncoding";
    public static final String LLP_START_OF_MESSAGE_CHARACTER = "messageStart";
    public static final String LLP_END_OF_MESSAGE_CHARACTER = "messageEnd";
    public static final String LLP_RECORD_SEPARATOR = "recordSeparator";
    public static final String LLP_SEND_ACK = "sendACK";
    public static final String LLP_SEGMENT_END = "segmentEnd";
    public static final String LLP_ACKCODE_SUCCESSFUL = "ackCodeSuccessful";
    public static final String LLP_ACKMSG_SUCCESSFUL = "ackMsgSuccessful";
    public static final String LLP_ACKCODE_ERROR = "ackCodeError";
    public static final String LLP_ACKMSG_ERROR = "ackMsgError";
    public static final String LLP_ACKCODE_REJECTED = "ackCodeRejected";
    public static final String LLP_ACKMSG_REJECTED = "ackMsgRejected";
    public static final String LLP_ACK_MSH_15 = "checkMSH15";
    public static final String LLP_ACK_NEW_CONNECTION = "ackOnNewConnection";
    public static final String LLP_ACK_NEW_CONNECTION_IP = "ackIP";
    public static final String LLP_ACK_NEW_CONNECTION_PORT = "ackPort";
    public static final String LLP_RESPONSE_FROM_TRANSFORMER = "responseFromTransformer";
    public static final String LLP_RESPONSE_VALUE = "responseValue";
    public static final String LLP_WAIT_FOR_END_OF_MESSAGE_CHAR = "waitForEndOfMessageCharacter";
    public static final String LLP_USE_STRICT_LLP = "useStrictLLP";
    public static final String LLP_PROCESS_BATCH_FILES = "processBatchFiles";
    public static final String CONNECTOR_CHARSET_ENCODING = "charsetEncoding";

    public Properties getDefaults() {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(LLP_PROTOCOL_NAME, LLP_PROTOCOL_NAME_VALUE);
        properties.put(LLP_SERVER_MODE, "1");
        properties.put(LLP_ADDRESS, "127.0.0.1");
        properties.put(LLP_PORT, "6661");
        properties.put(LLP_RECONNECT_INTERVAL, "5000");
        properties.put(LLP_RECEIVE_TIMEOUT, "0");
        properties.put(LLP_BUFFER_SIZE, "65536");
        properties.put(LLP_KEEP_CONNECTION_OPEN, "0");
        properties.put(LLP_CHAR_ENCODING, "hex");
        properties.put(LLP_START_OF_MESSAGE_CHARACTER, "0x0B");
        properties.put(LLP_END_OF_MESSAGE_CHARACTER, "0x1C");
        properties.put(LLP_RECORD_SEPARATOR, "0x0D");
        properties.put(LLP_SEGMENT_END, "0x0D");
        properties.put(LLP_SEND_ACK, "1");
        properties.put(LLP_ACKCODE_SUCCESSFUL, "AA");
        properties.put(LLP_ACKMSG_SUCCESSFUL, "");
        properties.put(LLP_ACKCODE_ERROR, "AE");
        properties.put(LLP_ACKMSG_ERROR, "An Error Occured Processing Message.");
        properties.put(LLP_ACKCODE_REJECTED, "AR");
        properties.put(LLP_ACKMSG_REJECTED, "Message Rejected.");
        properties.put(LLP_ACK_MSH_15, "0");
        properties.put(LLP_ACK_NEW_CONNECTION, "0");
        properties.put(LLP_ACK_NEW_CONNECTION_IP, "");
        properties.put(LLP_ACK_NEW_CONNECTION_PORT, "");
        properties.put(LLP_RESPONSE_FROM_TRANSFORMER, "0");
        properties.put(LLP_RESPONSE_VALUE, "None");
        properties.put(LLP_WAIT_FOR_END_OF_MESSAGE_CHAR, "0");
        properties.put(LLP_USE_STRICT_LLP, "1");
        properties.put(LLP_PROCESS_BATCH_FILES, "0");
        properties.put(CONNECTOR_CHARSET_ENCODING, CharsetUtils.DEFAULT_ENCODING);
        return properties;
    }
}
