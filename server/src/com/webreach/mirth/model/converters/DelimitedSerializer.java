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
 *   Andy Thorson <andyt@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */
package com.webreach.mirth.model.converters;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;

public class DelimitedSerializer extends SAXParser implements IXMLSerializer<String> {
	private Logger logger = Logger.getLogger(this.getClass());
	
	private DelimitedProperties props;
	private DelimitedReader delimitedBatchReader = null;
	
	public static Map<String, String> getDefaultProperties() {
		return DelimitedProperties.getDefaultProperties();
	}
	
	public DelimitedSerializer(Map delimitedProperties) {
		props = new DelimitedProperties(delimitedProperties);
	}

	public String fromXML(String source) throws SerializerException {
		
		StringBuilder builder = new StringBuilder();

		try {
			
			DelimitedXMLHandler handler = new DelimitedXMLHandler(props);
			XMLReader xr = XMLReaderFactory.createXMLReader();
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);
			xr.parse(new InputSource(new StringReader(source)));
			builder.append(handler.getOutput());
		} catch (Exception e) {
			String exceptionMessage = e.getClass().getName() + ":" + e.getMessage();
			logger.error(exceptionMessage);
			throw new SerializerException(e);
		}
		return builder.toString();
	}

	public Map<String,String> getMetadataFromDocument(Document doc) throws SerializerException {
		Map<String, String> map = new HashMap<String, String>();
		populateMetadata(map);
		return map;
	}

	public Map<String,String> getMetadataFromEncoded(String source) throws SerializerException {
		Map<String, String> map = new HashMap<String, String>();
		populateMetadata(map);
		return map;
	}

	public Map<String, String> getMetadataFromXML(String xmlSource) throws SerializerException {
		Map<String, String> map = new HashMap<String, String>();
		populateMetadata(map);
		return map;
	}

	private void populateMetadata(Map<String, String> map) {
		// There is no meaningful meta data available in the delimited text case
		// for version, type and source, so populate empty strings.
		map.put("version", "");
		map.put("type", "delimited");
		map.put("source", "");
	}
	
	public String toXML(String source) throws SerializerException {
		try {
			StringWriter stringWriter = new StringWriter();
			XMLPrettyPrinter serializer = new XMLPrettyPrinter(stringWriter);
			serializer.setEncodeEntities(true);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				DelimitedReader delimitedReader = new DelimitedReader(props);
				delimitedReader.setContentHandler(serializer);
				delimitedReader.parse(new InputSource(new StringReader(source)));
				os.write(stringWriter.toString().getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return os.toString();
		} catch (Exception e) {
			logger.error(e);
		}
		return new String();
	}

	/**
	 * Finds the next message in the input stream and returns it.
	 * 
	 * @param in The input stream (it's a BufferedReader, because operations on it require in.mark()).
	 * @param skipHeader Pass true to skip the configured number of header rows, otherwise false.
	 * @return The next message, or null if there are no more messages.
	 * @throws IOException
	 */
	public String getMessage(BufferedReader in, boolean skipHeader) throws IOException {
		
		// Allocate a batch reader if not already allocated
		if (delimitedBatchReader == null) {
			delimitedBatchReader = new DelimitedReader(props); 
		}
		return delimitedBatchReader.getMessage(in, skipHeader);
	}
}
