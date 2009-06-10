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
 *   Chris Lang <chrisl@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.model.converters;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;


public class DefaultXMLSerializer implements IXMLSerializer<String> {

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

	public String toXML(String source) throws SerializerException {
		return sanitize(source);
	}


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
