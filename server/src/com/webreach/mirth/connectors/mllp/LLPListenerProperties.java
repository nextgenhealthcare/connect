/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.webreach.mirth.connectors.mllp;

import java.util.Properties;

import com.webreach.mirth.model.ComponentProperties;

public class LLPListenerProperties implements ComponentProperties
{
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

    public Properties getDefaults()
    {
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
        properties.put(CONNECTOR_CHARSET_ENCODING, "DEFAULT_ENCODING");
        return properties;
    }
}
