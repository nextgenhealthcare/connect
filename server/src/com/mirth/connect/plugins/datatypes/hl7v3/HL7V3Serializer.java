/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v3;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import com.mirth.connect.donkey.model.message.SerializerException;
import com.mirth.connect.donkey.model.message.XmlSerializer;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.datatype.SerializerProperties;

public class HL7V3Serializer implements IXMLSerializer {
    
    private HL7V3SerializationProperties serializationProperties;
	
	public HL7V3Serializer(SerializerProperties properties) {
	    serializationProperties = (HL7V3SerializationProperties) properties.getSerializationProperties();
	}
	
	@Override
    public boolean isSerializationRequired(boolean toXml) {
        boolean serializationRequired = false;
        
        if (toXml) {
            if (!serializationProperties.isStripNamespaces()) {
                serializationRequired = true;
            }
        }

        return serializationRequired;
    }
	
	@Override
    public String transformWithoutSerializing(String message, XmlSerializer outboundSerializer) {
        return null;
    }
	
	@Override
	public String toXML(String source) throws SerializerException {
		return sanitize(source);
	}

	@Override
	public String fromXML(String source) throws SerializerException {
		return sanitize(source);
	}
	
	// cleans up the XML
	public String sanitize(String source) {
		return source;
	}

	public Map<String, String> getMetadata() throws SerializerException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("version", "3.0"); //TODO: Update this to real version codes
		map.put("type", "HL7v3-Message");
		map.put("source", "");
		return map;
	}

	@Override
	public Map<String, String> getMetadataFromDocument(Document doc) throws SerializerException {
		Map<String, String> map = getMetadata();
		map.put("type",doc.getDocumentElement().getNodeName());
		return map;
	}
}
