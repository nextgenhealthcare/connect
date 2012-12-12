/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters.edi;

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

import com.mirth.connect.donkey.model.message.SerializerException;
import com.mirth.connect.donkey.model.message.XmlSerializer;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.converters.XMLPrettyPrinter;
import com.mirth.connect.util.ErrorConstants;
import com.mirth.connect.util.ErrorMessageBuilder;

public class EDISerializer implements IXMLSerializer {
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

    @Override
    public boolean isTransformerRequired() {
        boolean transformerRequired = false;
        //TODO determine which properties are required for transformer
        if (!segmentDelim.equals("~") || !elementDelim.equals("*") || !subelementDelim.equals(":")) {
            transformerRequired = true;
        }

        return transformerRequired;
    }
	
    @Override
    public String transformWithoutSerializing(String message, XmlSerializer outboundSerializer) {
        return message;
    }

	@Override
	public String fromXML(String source) throws SerializerException {
		XMLReader xr;
		try {
			xr = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			throw new SerializerException(e.getMessage(), ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_500, "Error converting XML to EDI", e));
		}
		EDIXMLHandler handler = new EDIXMLHandler(segmentDelim, elementDelim, subelementDelim);
		xr.setContentHandler(handler);
		xr.setErrorHandler(handler);
		try {
            //Parse, but first replace all spaces between brackets. This fixes pretty-printed XML we might receive
            xr.parse(new InputSource(new StringReader(source.replaceAll("</([^>]*)>\\s+<", "</$1><"))));
		} catch (Exception e) {
			throw new SerializerException(e.getMessage(), ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_500, "Error converting XML to EDI", e));
		}
		return handler.getOutput().toString();
	}

	@Override
    public String toXML(String source) throws SerializerException {
        try {
            EDIReader ediReader = new EDIReader(segmentDelim, elementDelim, subelementDelim);
            StringWriter stringWriter = new StringWriter();
            XMLPrettyPrinter serializer = new XMLPrettyPrinter(stringWriter);
            serializer.setEncodeEntities(true);
            ediReader.setContentHandler(serializer);
            ediReader.parse(new InputSource(new StringReader(source)));
            return stringWriter.toString();
        } catch (Exception e) {
            //TODO is this supposed to throw a SerializerException?
            logger.error("Error converting EDI message to XML.", e);
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

	@Override
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

}
