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


package com.webreach.mirth.model.converters;

import java.util.Map;
import java.util.WeakHashMap;

import org.w3c.dom.Document;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.StringConverter;
import com.thoughtworks.xstream.annotations.Annotations;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class ObjectXMLSerializer implements IXMLSerializer<Object>{
	private XStream xstream;
	private static final Map<String, String> stringCache = new WeakHashMap<String, String>();

	public ObjectXMLSerializer() {
		xstream = new XStream(new XppDriver());
        xstream.registerConverter(new StringConverter(stringCache));
		xstream.setMode(XStream.NO_REFERENCES);
	}
	public ObjectXMLSerializer(Class<?>[] aliases){
		xstream = new XStream(new XppDriver());
        xstream.registerConverter(new StringConverter(stringCache));
		Annotations.configureAliases(xstream, aliases);
		xstream.setMode(XStream.NO_REFERENCES);
	}
	
	public ObjectXMLSerializer(Class<?>[] aliases, Converter[] converters){
		xstream = new XStream(new XppDriver());
		Annotations.configureAliases(xstream, aliases);
		xstream.setMode(XStream.NO_REFERENCES);
		
		for(int i = 0; i < converters.length; i++) {
			xstream.registerConverter(converters[i]);
		}
	}
		
	public String toXML(Object source) {
		return xstream.toXML(source);
	}
	public String toXML(Object source, Class<?>[] aliases) {
		Annotations.configureAliases(xstream, aliases);
		String retval = xstream.toXML(source);
		Annotations.configureAliases(xstream, new Class<?>[]{});
		return retval;
	}
	public Object fromXML(String source) {
		return xstream.fromXML(source);
	}

	public Object fromXML(String source, Class<?>[] aliases) {
		Annotations.configureAliases(xstream, aliases);
		Object retval = xstream.fromXML(source);
		Annotations.configureAliases(xstream, new Class<?>[]{});
		return retval;
	}
	
	public Map<String, String> getMetadata() throws SerializerException {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getMetadata(Document doc) throws SerializerException {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getMetadataFromDocument(Document doc) throws SerializerException {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getMetadataFromEncoded(String source) throws SerializerException {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getMetadataFromXML(String xmlSource) throws SerializerException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
