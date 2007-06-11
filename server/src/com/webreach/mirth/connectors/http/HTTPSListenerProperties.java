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

package com.webreach.mirth.connectors.http;

import java.util.Properties;

public class HTTPSListenerProperties
{
    public static final String name = "HTTPS Listener";
	
    public static final String DATATYPE = "DataType";
    public static final String HTTPS_ADDRESS = "host";
    public static final String HTTPS_PORT = "port";
    public static final String HTTPS_RECEIVE_TIMEOUT = "receiveTimeout";
    public static final String HTTPS_BUFFER_SIZE = "bufferSize";
    public static final String HTTPS_KEEP_CONNECTION_OPEN = "keepSendSocketOpen";
    public static final String HTTPS_START_OF_MESSAGE_CHARACTER = "messageStart";
    public static final String HTTPS_END_OF_MESSAGE_CHARACTER = "messageEnd";
    public static final String HTTPS_FIELD_SEPARATOR = "fieldSeparator";
    public static final String HTTPS_RECORD_SEPARATOR = "recordSeparator";
    public static final String HTTPS_SEND_ACK = "sendACK";
    public static final String HTTPS_KEY_STORE = "keyStore";
    public static final String HTTPS_KEY_STORE_PASSWORD = "storePassword";
    public static final String HTTPS_KEY_STORE_TYPE = "keytoreType";
    public static final String HTTPS_KEY_MANAGER_ALGORITHM = "keyManagerAlgorithm";
    public static final String HTTPS_KEY_MANAGER_FACTORY = "keyManagerFactory";
    public static final String HTTPS_PROTOCOL_HANDLER = "protocolHandler";
    public static final String HTTPS_REQUIRE_CLIENT_AUTHENTICATION = "requireClientAuthentication";
    public static final String HTTPS_SECURITY_PROVIDER = "provider";
    public static final String HTTPS_CLIENT_KEYSTORE = "clientKeystore";
    public static final String HTTPS_CLIENT_KEYSTORE_PASSWORD = "clientKeystorePassword";
    public static final String HTTPS_TRUST_KEYSTORE = "trustStore";
    public static final String HTTPS_TRUST_KEYSTORE_PASSWORD = "trustStorePassword";
    public static final String HTTPS_EXPLICIT_TRUST_STORE_ONLY = "explicitTrustStoreOnly";

    public static Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(HTTPS_ADDRESS, "127.0.0.1");
        properties.put(HTTPS_PORT, "6660");
        properties.put(HTTPS_RECEIVE_TIMEOUT, "5000");
        properties.put(HTTPS_BUFFER_SIZE, "65536");
        properties.put(HTTPS_KEEP_CONNECTION_OPEN, "0");
        properties.put(HTTPS_KEY_STORE, ".keystore");
        properties.put(HTTPS_KEY_STORE_PASSWORD, "");
        properties.put(HTTPS_KEY_STORE_TYPE, "KeyStore.getDefaultType()");
        properties.put(HTTPS_KEY_MANAGER_ALGORITHM, "SunX509");
        properties.put(HTTPS_PROTOCOL_HANDLER, "com.sun.net.ssl.internal.www.protocol");
        properties.put(HTTPS_REQUIRE_CLIENT_AUTHENTICATION, "1");
        properties.put(HTTPS_SECURITY_PROVIDER, "com.sun.net.ssl.internal.ssl.Provider");
        properties.put(HTTPS_CLIENT_KEYSTORE, "");
        properties.put(HTTPS_CLIENT_KEYSTORE_PASSWORD, "");
        properties.put(HTTPS_TRUST_KEYSTORE, "");
        properties.put(HTTPS_TRUST_KEYSTORE_PASSWORD, "");
        properties.put(HTTPS_EXPLICIT_TRUST_STORE_ONLY, "0");
        properties.put(HTTPS_KEY_MANAGER_FACTORY, "");
        properties.put(HTTPS_START_OF_MESSAGE_CHARACTER, "0x0B");
        properties.put(HTTPS_END_OF_MESSAGE_CHARACTER, "0x1C");
        properties.put(HTTPS_FIELD_SEPARATOR, "0x7C");
        properties.put(HTTPS_RECORD_SEPARATOR, "0x0D");
        properties.put(HTTPS_SEND_ACK, "1");
        return properties;
    }
}
