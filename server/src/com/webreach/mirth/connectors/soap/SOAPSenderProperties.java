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

import com.webreach.mirth.connectors.ConnectorProperties;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.ws.WSDefinition;

public class SOAPSenderProperties implements ConnectorProperties
{ 
    public static final String name = "SOAP Sender";

    public static final String SOAP_ENVELOPE_HEADER = "<?xml version=\"1.0\" encoding=\"utf-16\"?>\n<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n";
    public static final String SOAP_BODY_HEADER = "<soap:Body soap:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n";
    public static final String SOAP_BODY_FOOTER = "</soap:Body>\n";
    public static final String SOAP_ENVELOPE_FOOTER = "</soap:Envelope>";
    public static final String DATATYPE = "DataType";
    public static final String SOAP_HOST = "host";
    public static final String SOAP_SERVICE_ENDPOINT = "serviceEndpoint";
    public static final String SOAP_URL = "wsdlUrl";
    public static final String SOAP_METHOD = "method";
    public static final String SOAP_DEFINITION = "definition";
    public static final String SOAP_DEFAULT_DROPDOWN = "Press Get Methods";
    public static final String SOAP_ENVELOPE = "soapEnvelope";
    public static final String SOAP_ACTION_URI = "soapActionURI";
    public static final String CHANNEL_ID = "replyChannelId";
    public static final String CHANNEL_NAME = "channelName";

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(SOAP_URL, "");
        properties.put(SOAP_SERVICE_ENDPOINT, "");
        properties.put(SOAP_METHOD, SOAP_DEFAULT_DROPDOWN);
		ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        properties.put(SOAP_DEFINITION, (String) serializer.toXML(new WSDefinition()));
        properties.put(SOAP_HOST, "axis:?method=Press Get Methods");
        properties.put(SOAP_ENVELOPE, "");
        properties.put(SOAP_ACTION_URI, "");
        properties.put(CHANNEL_ID, "sink");
        properties.put(CHANNEL_NAME, "None");
        return properties;
    }
}
