/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

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

import com.mirth.connect.model.ncpdp.NCPDPReference;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Jun 5, 2007
 * Time: 2:46:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class NCPDPSerializer  implements IXMLSerializer<String> {
	private Logger logger = Logger.getLogger(this.getClass());
	private String segmentDelim = "\u001E";
	private String groupDelim = "\u001D";
	private String fieldDelim = "\u001C";
	private boolean useStrictValidation = false;
    public boolean validationError = false;
    
	public static Map<String, String> getDefaultProperties() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("segmentDelimiter", "0x1E");
		map.put("groupDelimiter", "0x1D");
		map.put("fieldDelimiter", "0x1C");
		map.put("useStrictValidation", "false");
		return map;
	}

    public NCPDPSerializer(Map NCPDPProperties){
		if (NCPDPProperties == null) {
			return;
		}
		if (NCPDPProperties.get("segmentDelimiter") != null) {
            String segDel = convertNonPrintableCharacters((String) NCPDPProperties.get("segmentDelimiter"));
            if(segDel.equals("0x1E")){
                this.segmentDelim = "\u001E";
            }
            else {
                this.segmentDelim = segDel;
            }

        }
		if (NCPDPProperties.get("groupDelimiter") != null) {
            String grpDel = convertNonPrintableCharacters((String) NCPDPProperties.get("groupDelimiter"));
            if(grpDel.equals("0x1D")){
                this.groupDelim = "\u001D";
            }
            else {
                this.groupDelim = grpDel;
            }
        }
		if (NCPDPProperties.get("fieldDelimiter") != null) {
            String fieldDel = convertNonPrintableCharacters((String) NCPDPProperties.get("fieldDelimiter"));
            if(fieldDel.equals("0x1C")){
                this.fieldDelim = "\u001C";
            }
            else {
                this.fieldDelim = fieldDel;
            }
        }
		if (NCPDPProperties.get("useStrictValidation") != null) {
			this.useStrictValidation = Boolean.parseBoolean((String) NCPDPProperties.get("useStrictValidation"));
		}
        return;
    }

	private String convertNonPrintableCharacters(String delimiter) {
		return delimiter.replaceAll("\\\\r", "\r").replaceAll("\\\\n", "\n").replaceAll("\\\\t", "\t");

	}

	public NCPDPSerializer() {

	}

	public String fromXML(String source) throws SerializerException {
		XMLReader xr;
		try {
			xr = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			throw new SerializerException(e.getMessage());
		}
		NCPDPXMLHandler handler = new NCPDPXMLHandler(segmentDelim, groupDelim, fieldDelim);
		xr.setContentHandler(handler);
		xr.setErrorHandler(handler);
		try {
            //Parse, but first replace all spaces between brackets. This fixes pretty-printed XML we might receive
            // change from source.replaceAll(">\\s+<", "><")
            if(useStrictValidation){
                xr.setFeature("http://xml.org/sax/features/validation", true);
                xr.setFeature("http://apache.org/xml/features/validation/schema", true);
                xr.setFeature("http://apache.org/xml/features/validation/schema-full-checking",true);
                xr.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage","http://www.w3.org/2001/XMLSchema");
                xr.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation","ncpdp.xsd");
                xr.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource","/ncpdp.xsd");
            }
            xr.parse(new InputSource(new StringReader(source.replaceAll("</([^>]*)>\\s+<", "</$1><"))));
            validationError = handler.validationError;    

        } catch (Exception e) {
			throw new SerializerException(e.getMessage());
		}
		return handler.getOutput().toString();
	}

    public String toXML(String source) throws SerializerException {
        try {
            NCPDPReader ncpdpReader = new NCPDPReader(segmentDelim, groupDelim, fieldDelim);
            StringWriter stringWriter = new StringWriter();
            XMLPrettyPrinter serializer = new XMLPrettyPrinter(stringWriter);
            ncpdpReader.setContentHandler(serializer);
            ncpdpReader.parse(new InputSource(new StringReader(source)));
            return stringWriter.toString();
        } catch (Exception e) {
            logger.error("Error converting NCPDP message to XML.", e);
        }

        return new String();
    }

    public String getSegmentDelim() {
        return segmentDelim;
    }

    public void setSegmentDelim(String segmentDelim) {
        this.segmentDelim = segmentDelim;
    }

    public String getGroupDelim() {
        return groupDelim;
    }

    public void setGroupDelim(String groupDelim) {
        this.groupDelim = groupDelim;
    }

    public String getFieldDelim() {
        return fieldDelim;
    }

    public void setFieldDelim(String fieldDelim) {
        this.fieldDelim = fieldDelim;
    }
	private Map<String, String> getMetadata(String sourceMessage) throws SerializerException {
		DocumentSerializer docSerializer = new DocumentSerializer();
		Document document = docSerializer.fromXML(sourceMessage);
		return getMetadataFromDocument(document);
	}

	public Map<String, String> getMetadataFromDocument(Document document) {
		Map<String, String> map = new HashMap<String, String>();
		String sendingFacility = "";
		if (document != null && document.getElementsByTagName("ServiceProviderId") != null) {
			Node sender = document.getElementsByTagName("ServiceProviderId").item(0);
			if (sender != null) {
				sendingFacility = sender.getTextContent();
			}
		}
		String event = "";
		if (document != null && document.getElementsByTagName("TransactionCode") != null) {
			Node type = document.getElementsByTagName("TransactionCode").item(0);
			if (type != null) {
				event = NCPDPReference.getInstance().getTransactionName(type.getTextContent());
			}
		}
		String version = "5.1";
		if (document != null && document.getElementsByTagName("VersionReleaseNumber") != null) {
			Node versionNode = document.getElementsByTagName("VersionReleaseNumber").item(0);
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
		String NCPDPXML = fromXML(source);
		return getMetadata(NCPDPXML);
	}

	public Map<String, String> getMetadataFromXML(String xmlSource) throws SerializerException {
		return getMetadata(xmlSource);
	}
}
