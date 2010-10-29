/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.util.ArrayList;
import java.util.Properties;

import com.mirth.connect.model.QueuedSenderProperties;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class WebServiceSenderProperties extends QueuedSenderProperties {
    public static final String name = "Web Service Sender";

    public static final String DATATYPE = "DataType";
    public static final String WEBSERVICE_HOST = "host";
    public static final String WEBSERVICE_WSDL_CACHE_ID = "dispatcherWsdlCacheId";
    public static final String WEBSERVICE_WSDL_OPERATIONS = "dispatcherWsdlOperations";
    public static final String WEBSERVICE_REPLY_CHANNEL_ID = "dispatcherReplyChannelId";
    public static final String WEBSERVICE_WSDL_URL = "dispatcherWsdlUrl";
    public static final String WEBSERVICE_SERVICE = "dispatcherService";
    public static final String WEBSERVICE_PORT = "dispatcherPort";
    public static final String WEBSERVICE_OPERATION = "dispatcherOperation";
    public static final String WEBSERVICE_USE_AUTHENTICATION = "dispatcherUseAuthentication";
    public static final String WEBSERVICE_USERNAME = "dispatcherUsername";
    public static final String WEBSERVICE_PASSWORD = "dispatcherPassword";
    public static final String WEBSERVICE_ENVELOPE = "dispatcherEnvelope";
    public static final String WEBSERVICE_ONE_WAY = "dispatcherOneWay";
    public static final String WEBSERVICE_USE_MTOM = "dispatcherUseMtom";
    public static final String WEBSERVICE_ATTACHMENT_NAMES = "dispatcherAttachmentNames";
    public static final String WEBSERVICE_ATTACHMENT_CONTENTS = "dispatcherAttachmentContents";
    public static final String WEBSERVICE_ATTACHMENT_TYPES = "dispatcherAttachmentTypes";
    public static final String WEBSERVICE_SOAP_ACTION = "dispatcherSoapAction";
    
    public static final String WEBSERVICE_DEFAULT_DROPDOWN = "Press Get Operations";

    public Properties getDefaults() {
        Properties properties = super.getDefaults();
        properties.put(DATATYPE, name);
        properties.put(WEBSERVICE_HOST, "");
        properties.put(WEBSERVICE_WSDL_CACHE_ID, "");
        properties.put(WEBSERVICE_WSDL_URL, "");
        properties.put(WEBSERVICE_SERVICE, "");
        properties.put(WEBSERVICE_PORT, "");
        properties.put(WEBSERVICE_OPERATION, WEBSERVICE_DEFAULT_DROPDOWN);
        properties.put(WEBSERVICE_USE_AUTHENTICATION, "0");
        properties.put(WEBSERVICE_USERNAME, "");
        properties.put(WEBSERVICE_PASSWORD, "");
        properties.put(WEBSERVICE_ENVELOPE, "");
        properties.put(WEBSERVICE_ONE_WAY, "0");
        properties.put(WEBSERVICE_USE_MTOM, "0");
        properties.put(WEBSERVICE_REPLY_CHANNEL_ID, "sink");

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();

        ArrayList<String> defaultOperations = new ArrayList<String>();
        defaultOperations.add(WEBSERVICE_DEFAULT_DROPDOWN);
        properties.put(WEBSERVICE_WSDL_OPERATIONS, serializer.toXML(defaultOperations));

        properties.put(WEBSERVICE_ATTACHMENT_NAMES, serializer.toXML(new ArrayList<String>()));
        properties.put(WEBSERVICE_ATTACHMENT_CONTENTS, serializer.toXML(new ArrayList<String>()));
        properties.put(WEBSERVICE_ATTACHMENT_TYPES, serializer.toXML(new ArrayList<String>()));
        
        properties.put(WEBSERVICE_SOAP_ACTION, "");
        return properties;
    }
}
