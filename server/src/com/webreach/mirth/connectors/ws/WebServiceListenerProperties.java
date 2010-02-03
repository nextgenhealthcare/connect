/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.connectors.ws;

import java.util.ArrayList;
import java.util.Properties;

import com.webreach.mirth.model.ComponentProperties;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

public class WebServiceListenerProperties implements ComponentProperties {
    public static final String name = "Web Service Listener";
    public static final String DATATYPE = "DataType";
    public static final String WEBSERVICE_HOST = "host";
    public static final String WEBSERVICE_PORT = "port";
    public static final String WEBSERVICE_CLASS_NAME = "receiverClassName";
    public static final String WEBSERVICE_SERVICE_NAME = "receiverServiceName";
    public static final String WEBSERVICE_RESPONSE_VALUE = "receiverResponseValue";
    public static final String WEBSERVICE_USERNAMES = "receiverUsernames";
    public static final String WEBSERVICE_PASSWORDS = "receiverPasswords";

    public Properties getDefaults() {
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
