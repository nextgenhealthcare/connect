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

package com.webreach.mirth.connectors.jms;

import java.util.Properties;

import com.webreach.mirth.model.ComponentProperties;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

public class JMSReaderProperties implements ComponentProperties
{
	public static final String name = "JMS Reader";
	
    public static final String DATATYPE = "DataType";
    public static final String JMS_SPECIFICATION = "specification";
    public static final String JMS_DURABLE = "durable";
    public static final String JMS_CLIENT_ID = "clientId";
    public static final String JMS_USERNAME = "username";
    public static final String JMS_PASSWORD = "password";
    public static final String JMS_QUEUE = "host";
    public static final String JMS_URL = "jndiProviderUrl";
    public static final String JMS_USE_JNDI = "useJndi";
    public static final String JMS_INITIAL_FACTORY = "jndiInitialFactory";
    public static final String JMS_CONNECTION_FACTORY_JNDI = "connectionFactoryJndiName";
    public static final String JMS_CONNECTION_FACTORY_CLASS = "connectionFactoryClass";
    public static final String JMS_ACK_MODE = "acknowledgementMode";
    public static final String JMS_SELECTOR = "selector";
    public static final String JMS_ADDITIONAL_PROPERTIES = "connectionFactoryProperties";
 
    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(JMS_SPECIFICATION, "1.1");
        properties.put(JMS_DURABLE, "0");
        properties.put(JMS_CLIENT_ID, "");
        properties.put(JMS_USERNAME, "");
        properties.put(JMS_PASSWORD, "");
        properties.put(JMS_URL, "");
        properties.put(JMS_QUEUE, "");
        properties.put(JMS_INITIAL_FACTORY, "");
        properties.put(JMS_CONNECTION_FACTORY_JNDI, "");
        properties.put(JMS_CONNECTION_FACTORY_CLASS, "");
        properties.put(JMS_USE_JNDI, "0");
        properties.put(JMS_ACK_MODE, "1");
        properties.put(JMS_SELECTOR, "");
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        properties.put(JMS_ADDITIONAL_PROPERTIES, serializer.toXML(new Properties()));
        return properties;
    }
}
