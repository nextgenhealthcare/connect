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

public class TCPListenerProperties implements ComponentProperties
{
	public static final String name = "TCP Listener";
	
    public static final String DATATYPE = "DataType";
    public static final String TCP_ADDRESS = "host";
    public static final String TCP_PORT = "port";
    public static final String TCP_RECEIVE_TIMEOUT = "receiveTimeout";
    public static final String TCP_BUFFER_SIZE = "bufferSize";
    public static final String TCP_CHAR_ENCODING = "charEncoding";
    public static final String TCP_ACK_NEW_CONNECTION = "ackOnNewConnection";
    public static final String TCP_ACK_NEW_CONNECTION_IP = "ackIP";
    public static final String TCP_ACK_NEW_CONNECTION_PORT = "ackPort";
    public static final String TCP_RESPONSE_VALUE = "responseValue";
    public static final String CONNECTOR_CHARSET_ENCODING = "charsetEncoding";
    public static final String TCP_TYPE = "binary";

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(TCP_ADDRESS, "127.0.0.1");
        properties.put(TCP_PORT, "6661");
        properties.put(TCP_RECEIVE_TIMEOUT, "5000");
        properties.put(TCP_BUFFER_SIZE, "65536");
        properties.put(TCP_ACK_NEW_CONNECTION, "0");
        properties.put(TCP_ACK_NEW_CONNECTION_IP, "");
        properties.put(TCP_ACK_NEW_CONNECTION_PORT, "");
        properties.put(TCP_RESPONSE_VALUE, "None");
        properties.put(CONNECTOR_CHARSET_ENCODING, "DEFAULT_ENCODING");
        properties.put(TCP_TYPE, "0");
        return properties;
    }
}
