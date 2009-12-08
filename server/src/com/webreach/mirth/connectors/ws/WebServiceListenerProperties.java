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

package com.webreach.mirth.connectors.ws;

import java.util.ArrayList;
import java.util.Properties;

import com.webreach.mirth.model.ComponentProperties;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

public class WebServiceListenerProperties implements ComponentProperties
{
	public static final String name = "Web Service Listener";	
    public static final String DATATYPE = "DataType";
    public static final String WEBSERVICE_HOST = "host";
    public static final String WEBSERVICE_PORT = "port";
    public static final String WEBSERVICE_CLASS_NAME = "receiverClassName";
    public static final String WEBSERVICE_SERVICE_NAME = "receiverServiceName";
    public static final String WEBSERVICE_RESPONSE_VALUE = "receiverResponseValue";
    public static final String WEBSERVICE_USERNAMES = "receiverUsernames";
    public static final String WEBSERVICE_PASSWORDS = "receiverPasswords";

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(WEBSERVICE_HOST, "0.0.0.0");
        properties.put(WEBSERVICE_PORT, "8081");
        properties.put(WEBSERVICE_CLASS_NAME, "com.webreach.mirth.connectors.ws.DefaultAcceptMessage");
        properties.put(WEBSERVICE_SERVICE_NAME, "Mirth");
        properties.put(WEBSERVICE_RESPONSE_VALUE, "None");
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        properties.put(WEBSERVICE_USERNAMES, serializer.toXML(new ArrayList<String>()));
        properties.put(WEBSERVICE_PASSWORDS, serializer.toXML(new ArrayList<String>()));
        return properties;
    }
}
