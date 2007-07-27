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

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.parser.XMLParser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public class ER7Serializer implements IXMLSerializer<String> {
	private Logger logger = Logger.getLogger(this.getClass());
	private PipeParser pipeParser;
	private XMLParser xmlParser;
	private boolean useStrictParser = false;
	private boolean useStrictValidation = false;
	private boolean handleRepetitions = false;
	private boolean encodeEntities = true;
	public ER7Serializer(Map er7Properties) {
		if (er7Properties != null && er7Properties.get("useStrictParser") != null) {
			this.useStrictParser = Boolean.parseBoolean((String) er7Properties.get("useStrictParser"));
		}
		if (er7Properties != null && er7Properties.get("useStrictValidation") != null) {
			this.useStrictValidation = Boolean.parseBoolean((String) er7Properties.get("useStrictValidation"));
		}
		if (er7Properties != null && er7Properties.get("handleRepetitions") != null) {
			this.handleRepetitions = Boolean.parseBoolean((String) er7Properties.get("handleRepetitions"));
		}
		if (er7Properties != null && er7Properties.get("encodeEntities") != null){
			this.encodeEntities = Boolean.parseBoolean((String) er7Properties.get("encodeEntities"));
		}
		if (useStrictParser) {
			initializeHapiParser();
		}
	}

	public ER7Serializer() {
		initializeHapiParser();
	}

	private void initializeHapiParser() {
		pipeParser = new PipeParser();
		xmlParser = new DefaultXMLParser();
		// Turn off strict validation if needed
		if (!this.useStrictValidation) {
			pipeParser.setValidationContext(new NoValidation());
			xmlParser.setValidationContext(new NoValidation());
		}

		xmlParser.setKeepAsOriginalNodes(new String[] { "NTE.3", "OBX.5" });
	}

	/**
	 * Returns an XML-encoded HL7 message given an ER7-enconded HL7 message.
	 * 
	 * @param source
	 *            an ER7-encoded HL7 message.
	 * @return
	 */
	public String toXML(String source) throws SerializerException {
		StringBuilder builder = new StringBuilder();
		if (useStrictParser) {
			try {
				builder.append(xmlParser.encode(pipeParser.parse(source.trim())));
			} catch (HL7Exception e) {
				throw new SerializerException(e);
			}
		} else {
			try {

				ER7Reader er7Reader = new ER7Reader(handleRepetitions);
				StringWriter stringWriter = new StringWriter();
				XMLPrettyPrinter serializer = new XMLPrettyPrinter(stringWriter);
				serializer.setEncodeEntities(encodeEntities);
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				try {
					er7Reader.setContentHandler(serializer);
					er7Reader.parse(new InputSource(new StringReader(source)));
					os.write(stringWriter.toString().getBytes());
				} catch (Exception e) {
					e.printStackTrace();
				}
				//Allow non entity encoded messages to work
				if (encodeEntities){
					builder.append(os.toString());
				}else{
					builder.append(os.toString().replaceAll("&","&amp;"));
				}
				
			} catch (Exception e) {
				String exceptionMessage = e.getClass().getName() + ":" + e.getMessage();
				logger.error(exceptionMessage);
			}
		}
		return sanitize(builder.toString());
	}

	/**
	 * Returns an ER7-encoded HL7 message given an XML-encoded HL7 message.
	 * 
	 * @param source
	 *            a XML-encoded HL7 message.
	 * @return
	 */
	public String fromXML(String source) throws SerializerException {
		StringBuilder builder = new StringBuilder();
		if (useStrictParser) {
			try {
				builder.append(pipeParser.encode(xmlParser.parse(source)));
			} catch (HL7Exception e) {
				throw new SerializerException(e);
			}
		} else {

			try {
				// The delimiters below need to come from the XML somehow...the
				// ER7 handler should take care of it
				// TODO: Ensure you get these elements from the XML
				ER7XMLHandler handler = new ER7XMLHandler("\r", "|", "^", "&", "~", "\\");
				XMLReader xr = XMLReaderFactory.createXMLReader();
				xr.setContentHandler(handler);
				xr.setErrorHandler(handler);
                //Parse, but first replace all spaces between brackets. This fixes pretty-printed XML we might receive
				xr.parse(new InputSource(new StringReader(source.replaceAll("</([^>]*)>\\s+<", "</$1><"))));
				builder.append(handler.getOutput());
			} catch (Exception e) {
				String exceptionMessage = e.getClass().getName() + ":" + e.getMessage();
				logger.error(exceptionMessage);
				throw new SerializerException(e);
			}
		}
		return builder.toString();
	}

	// cleans up the XML
	public String sanitize(String source) {
		return source;
	}

	public Map<String, String> getMetadataFromXML(String xmlSource) throws SerializerException {
		Map<String, String> map = new HashMap<String, String>();

		if (useStrictParser) {
			try {
				Message message = xmlParser.parse(xmlSource);
				Terser terser = new Terser(message);
				String sendingFacility = terser.get("/MSH-4-1");
				String event = terser.get("/MSH-9-1") + "-" + terser.get("/MSH-9-2");
				map.put("version", message.getVersion());
				map.put("type", event);
				map.put("source", sendingFacility);
			} catch (Exception e) {
				new SerializerException(e);
			}
			return map;
		} else {
			String sendingFacility = getXMLValue(xmlSource, "<MSH.4.1>", "</MSH.4.1>");
			String event = getXMLValue(xmlSource, "<MSH.9.1>", "</MSH.9.1>");
			String subType = getXMLValue(xmlSource, "<MSH.9.2>", "</MSH.9.2>");
			if (!subType.equals("")) {
				event += "-" + subType;
			}
			if (event.equals("")) {
				event = "Unknown";
			}
			String version = getXMLValue(xmlSource, "<MSH.12.1>", "</MSH.12.1>");
			map.put("version", version);
			map.put("type", event);
			map.put("source", sendingFacility);
			return map;
		}
	}

	public Map<String, String> getMetadataFromEncoded(String source) throws SerializerException {
		Map<String, String> map = new HashMap<String, String>();

		if (useStrictParser) {
			try {
				Message message = pipeParser.parse(source.replaceAll("\n", "\r").trim());
				Terser terser = new Terser(message);
				String sendingFacility = terser.get("/MSH-4-1");
				String event = terser.get("/MSH-9-1") + "-" + terser.get("/MSH-9-2");
				map.put("version", message.getVersion());
				map.put("type", event);
				map.put("source", sendingFacility);
			} catch (Exception e) {
				new SerializerException(e);
			}
			return map;
		} else {
			source = toXML(source);
			String sendingFacility = getXMLValue(source, "<MSH.4.1>", "</MSH.4.1>");
			String event = getXMLValue(source, "<MSH.9.1>", "</MSH.9.1>");
			String subType = getXMLValue(source, "<MSH.9.2>", "</MSH.9.2>");
			if (!subType.equals("")) {
				event += "-" + subType;
			}
			if (event.equals("")) {
				event = "Unknown";
			}
			String version = getXMLValue(source, "<MSH.12.1>", "</MSH.12.1>");
			map.put("version", version);
			map.put("type", event);
			map.put("source", sendingFacility);
			return map;
		}
	}

	public String getXMLValue(String source, String startTag, String endTag) {
		String returnValue = "";
		int startLoc = -1;
		if ((startLoc = source.indexOf(startTag)) != -1) {
			returnValue = source.substring(startLoc + startTag.length(), source.indexOf(endTag, startLoc));
		}
		return returnValue;
	}

	public Map<String, String> getMetadataFromDocument(Document document) {
		Map<String, String> map = new HashMap<String, String>();
		if (useStrictParser) {
			try {
				DocumentSerializer serializer = new DocumentSerializer();
				serializer.setPreserveSpace(true);
				String source = serializer.toXML(document);
				Message message = xmlParser.parse(source);
				Terser terser = new Terser(message);
				String sendingFacility = terser.get("/MSH-4-1");
				String event = terser.get("/MSH-9-1") + "-" + terser.get("/MSH-9-2");
				map.put("version", message.getVersion());
				map.put("type", event);
				map.put("source", sendingFacility);
				return map;
			} catch (Exception e) {
				logger.error(e.getMessage());
				return map;
			}
		} else {

			String sendingFacility = "";
			if (document.getElementsByTagName("MSH.4.1").getLength() > 0) {
				Node sender = document.getElementsByTagName("MSH.4.1").item(0);
				if (sender != null) {
					sendingFacility = sender.getFirstChild().getTextContent();
				}
			}
			String event = "Unknown";
			if (document.getElementsByTagName("MSH.9").getLength() > 0) {
				if (document.getElementsByTagName("MSH.9.1").getLength() > 0) {
					Node type = document.getElementsByTagName("MSH.9.1").item(0);
					if (type != null) {
						event = type.getFirstChild().getNodeValue();
						if (document.getElementsByTagName("MSH.9.2").getLength() > 0) {
							Node subtype = document.getElementsByTagName("MSH.9.2").item(0);
							event += "-" + subtype.getFirstChild().getTextContent();
						}
					}
				}
			}
			String version = "";
			if (document.getElementsByTagName("MSH.12.1").getLength() > 0) {
				Node versionNode = document.getElementsByTagName("MSH.12.1").item(0);
				if (versionNode != null) {
					version = versionNode.getFirstChild().getTextContent();
				}
			}
			map.put("version", version);
			map.put("type", event);
			map.put("source", sendingFacility);
			return map;
		}
	}
}
