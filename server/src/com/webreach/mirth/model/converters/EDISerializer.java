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
 *   Christopher lang <chrisl@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */
package com.webreach.mirth.model.converters;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class EDISerializer implements IXMLSerializer<String> {
	private Logger logger = Logger.getLogger(this.getClass());
	private String segmentDelim = "~";
	private String elementDelim = "*";
	private String subelementDelim = ":";
	
	public static Map<String, String> getDefaultProperties() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("segmentDelimiter", "~");
		map.put("elementDelimiter", "*");
		map.put("subelementDelimiter", ":");
		return map;
	}
	
	public EDISerializer(Map ediProperties) {
		if (ediProperties == null) {
			return;
		}
		if (ediProperties.get("segmentDelimiter") != null) {
			this.segmentDelim = convertNonPrintableCharacters((String) ediProperties.get("segmentDelimiter"));

		}
		if (ediProperties.get("elementDelimiter") != null) {
			this.elementDelim = convertNonPrintableCharacters((String) ediProperties.get("elementDelimiter"));
		}
		if (ediProperties.get("subelementDelimiter") != null) {
			this.subelementDelim = convertNonPrintableCharacters((String) ediProperties.get("subelementDelimiter"));
		}
		return;
	}

	private String convertNonPrintableCharacters(String delimiter) {
		return delimiter.replaceAll("\\\\r", "\r").replaceAll("\\\\n", "\n").replaceAll("\\\\t", "\t");

	}

	public EDISerializer() {

	}

	public String fromXML(String source) throws SerializerException {
		XMLReader xr;
		try {
			xr = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			throw new SerializerException(e.getMessage());
		}
		EDIXMLHandler handler = new EDIXMLHandler(segmentDelim, elementDelim, subelementDelim);
		xr.setContentHandler(handler);
		xr.setErrorHandler(handler);
		try {
            //Parse, but first replace all spaces between brackets. This fixes pretty-printed XML we might receive
            xr.parse(new InputSource(new StringReader(source.replaceAll("</([^>]*)>\\s+<", "</$1><"))));
		} catch (Exception e) {
			throw new SerializerException(e.getMessage());
		}
		return handler.getOutput().toString();
	}

	public String toXML(String source) throws SerializerException {
		try {
			EDIReader ediReader = new EDIReader(segmentDelim, elementDelim, subelementDelim);
			StringWriter stringWriter = new StringWriter();
			XMLPrettyPrinter serializer = new XMLPrettyPrinter(stringWriter);
			serializer.setEncodeEntities(true);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				ediReader.setContentHandler(serializer);
				ediReader.parse(new InputSource(new StringReader(source)));
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

	public String getSegmentDelim() {
		return segmentDelim;
	}

	public void setSegmentDelim(String segmentDelim) {
		this.segmentDelim = segmentDelim;
	}

	public String getElementDelim() {
		return elementDelim;
	}

	public void setElementDelim(String elementDelim) {
		this.elementDelim = elementDelim;
	}

	public String getSubelementDelim() {
		return subelementDelim;
	}

	public void setSubelementDelim(String subelementDelim) {
		this.subelementDelim = subelementDelim;
	}

	private Map<String, String> getMetadata(String sourceMessage) throws SerializerException {
		DocumentSerializer docSerializer = new DocumentSerializer();
		docSerializer.setPreserveSpace(true);
		Document document = docSerializer.fromXML(sourceMessage);
		return getMetadataFromDocument(document);
	}

	public Map<String, String> getMetadataFromDocument(Document document) {
		Map<String, String> map = new HashMap<String, String>();
		String sendingFacility = "";
		if (document.getElementsByTagName("ISA.06.1") != null) {
			Node sender = document.getElementsByTagName("ISA.06.1").item(0);
			if (sender != null) {
				sendingFacility = sender.getTextContent();
			}
		}
		if (sendingFacility == null && document.getElementsByTagName("GS.02.1") != null) {
			Node sender = document.getElementsByTagName("GS.02.1").item(0);
			if (sender != null) {
				sendingFacility = sender.getTextContent();
			}
		}
		String event = document.getDocumentElement().getNodeName();
		if (document.getElementsByTagName("ST.01.1") != null) {
			Node type = document.getElementsByTagName("ST.01.1").item(0);
			if (type != null) {
				event = type.getTextContent();
			}
		}
		String version = "";
		if (document.getElementsByTagName("GS.08.1") != null) {
			Node versionNode = document.getElementsByTagName("GS.08.1").item(0);
			if (versionNode != null) {
				version = versionNode.getTextContent();
			}
		}

		map.put("version", version);
		map.put("type", event);
		map.put("source", sendingFacility);
		return map;
	}

	public Map<String, String> getMetadataFromEncoded(String source) throws SerializerException {
		String ediXML = fromXML(source);
		return getMetadata(ediXML);
	}

	public Map<String, String> getMetadataFromXML(String xmlSource) throws SerializerException {
		return getMetadata(xmlSource);
		// TODO: Make this actually use a helper string parser
	}

}
