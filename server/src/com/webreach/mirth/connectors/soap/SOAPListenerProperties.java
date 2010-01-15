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

package com.webreach.mirth.connectors.soap;

import java.util.Properties;

import com.webreach.mirth.model.ComponentProperties;

public class SOAPListenerProperties implements ComponentProperties
{
	public static final String name = "SOAP Listener";	
    public static final String DATATYPE = "DataType";
    public static final String SOAP_HOST = "host";
    public static final String SOAP_LISTENER_ADDRESS = "listenerAddress";
    public static final String SOAP_EXTERNAL_ADDRESS = "externalAddress";
    public static final String USE_LISTENER_ADDRESS = "useListenerAddress";
    public static final String SOAP_SERVICE_NAME = "serviceName";
    public static final String SOAP_PORT = "port";
    public static final String SOAP_CONTENT_TYPE = "Content-Type";
    public static final String SOAP_RESPONSE_VALUE = "responseValue";
    public static final String SOAP_RECEIVE_TIMEOUT = "keepAliveTimeout";
    public static final String SOAP_KEEP_CONNECTION_OPEN = "keepAlive";

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(SOAP_HOST, "axis:soap://localhost:8081/services");
        properties.put(SOAP_LISTENER_ADDRESS, "localhost");
        properties.put(SOAP_EXTERNAL_ADDRESS, "localhost");
        properties.put(USE_LISTENER_ADDRESS, "1");
        properties.put(SOAP_PORT, "8081");
        properties.put(SOAP_SERVICE_NAME, "Mirth");
        properties.put(SOAP_CONTENT_TYPE, "text/xml");
        properties.put(SOAP_RESPONSE_VALUE, "None");
        return properties;
    }
}
