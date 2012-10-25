/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters.xml;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import com.mirth.connect.donkey.model.message.SerializerException;
import com.mirth.connect.model.converters.IXMLSerializer;


public class DefaultXMLSerializer implements IXMLSerializer {

	private boolean stripNamespaces = true;
	
	public static Map<String, String> getDefaultProperties() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("stripNamespaces", "true");
		return map;
	}
	
	public DefaultXMLSerializer(Map xmlProperties) {
		if (xmlProperties != null && xmlProperties.get("stripNamespaces") != null) {
			this.stripNamespaces = Boolean.parseBoolean((String) xmlProperties.get("stripNamespaces"));
		}
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

	private Map<String, String> getMetadata() throws SerializerException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("version", "1.0");
		map.put("type", "XML-Message");
		map.put("source", "");
		return map;
	}

	public Map<String, String> getMetadataFromDocument(Document doc) throws SerializerException {
		return getMetadata();
	}

	public Map<String, String> getMetadataFromEncoded(String source) throws SerializerException {
		return getMetadata();
	}

	public Map<String, String> getMetadataFromXML(String xmlSource) throws SerializerException {
		return getMetadata();
	}
}
