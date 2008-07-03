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

import com.webreach.mirth.model.QueuedSenderProperties;

public class LLPSenderProperties extends QueuedSenderProperties
{
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
    public static final String LLP_HL7_ACK_RESPONSE = "hl7AckResponse";
    public static final String CONNECTOR_CHARSET_ENCODING = "charsetEncoding";
    public static final String CHANNEL_ID = "replyChannelId";
    public static final String CHANNEL_NAME = "channelName";
    
    public Properties getDefaults()
    {
        Properties properties = super.getDefaults();
        properties.put(DATATYPE, name);
        properties.put(LLP_PROTOCOL_NAME, LLP_PROTOCOL_NAME_VALUE);
        properties.put(LLP_ADDRESS, "127.0.0.1");
        properties.put(LLP_PORT, "6660");
        properties.put(LLP_SERVER_TIMEOUT, "5000");
        properties.put(LLP_BUFFER_SIZE, "65536");
        properties.put(LLP_KEEP_CONNECTION_OPEN, "0");
        properties.put(LLP_MAX_RETRY_COUNT, "50");
        properties.put(LLP_CHAR_ENCODING, "hex");
        properties.put(LLP_START_OF_MESSAGE_CHARACTER, "0x0B");
        properties.put(LLP_END_OF_MESSAGE_CHARACTER, "0x1C");
        properties.put(LLP_RECORD_SEPARATOR, "0x0D");
        properties.put(LLP_SEGMENT_END, "0x0D");
        properties.put(LLP_ACK_TIMEOUT, "5000");
        properties.put(LLP_HL7_ACK_RESPONSE, "1");
        properties.put(CONNECTOR_CHARSET_ENCODING, "DEFAULT_ENCODING");
        properties.put(LLP_TEMPLATE, "${message.encodedData}");
        properties.put(CHANNEL_ID, "sink");
        properties.put(CHANNEL_NAME, "None");
        return properties;
    }
}
