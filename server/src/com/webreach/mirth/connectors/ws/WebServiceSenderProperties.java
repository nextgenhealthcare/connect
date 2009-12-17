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

import com.webreach.mirth.model.QueuedSenderProperties;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

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
        return properties;
    }
}
