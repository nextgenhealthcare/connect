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

package com.webreach.mirth.connectors.tcp;

import java.util.Properties;

import com.webreach.mirth.model.ComponentProperties;

public class TCPSenderProperties implements ComponentProperties
{
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
    public static final String TCP_USE_PERSISTENT_QUEUES = "usePersistentQueues";
    public static final String TCP_ACK_TIMEOUT = "ackTimeout";
    public static final String CONNECTOR_CHARSET_ENCODING = "charsetEncoding";
    public static final String CHANNEL_ID = "replyChannelId";
    public static final String CHANNEL_NAME = "channelName";
    public static final String TCP_RECONNECT_INTERVAL = "reconnectMillisecs";
    public static final String TCP_TYPE = "binary";

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(TCP_ADDRESS, "127.0.0.1");
        properties.put(TCP_PORT, "6660");
        properties.put(TCP_SERVER_TIMEOUT, "5000");
        properties.put(TCP_BUFFER_SIZE, "65536");
        properties.put(TCP_KEEP_CONNECTION_OPEN, "0");
        properties.put(TCP_MAX_RETRY_COUNT, "50");
        properties.put(TCP_CHAR_ENCODING, "hex");
        properties.put(TCP_USE_PERSISTENT_QUEUES, "0");
        properties.put(TCP_ACK_TIMEOUT, "5000");
        properties.put(CONNECTOR_CHARSET_ENCODING, "DEFAULT_ENCODING");
        properties.put(TCP_TEMPLATE, "${message.encodedData}");
        properties.put(CHANNEL_ID, "sink");
        properties.put(CHANNEL_NAME, "None");
        properties.put(TCP_RECONNECT_INTERVAL, "10000");
        properties.put(TCP_TYPE, "0");
        return properties;
    }
}
