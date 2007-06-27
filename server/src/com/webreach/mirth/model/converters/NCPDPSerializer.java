package com.webreach.mirth.model.converters;

import org.apache.log4j.Logger;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.Map;
import java.util.HashMap;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;

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

    public NCPDPSerializer(Map NCPDPProperties){
		if (NCPDPProperties == null) {
			return;
		}
		if (NCPDPProperties.get("segmentDelimiter") != null) {
			this.segmentDelim = convertNonPrintableCharacters((String) NCPDPProperties.get("segmentDelimiter"));

		}
		if (NCPDPProperties.get("groupDelimiter") != null) {
			this.groupDelim = convertNonPrintableCharacters((String) NCPDPProperties.get("groupDelimiter"));
		}
		if (NCPDPProperties.get("fieldDelimiter") != null) {
			this.fieldDelim = convertNonPrintableCharacters((String) NCPDPProperties.get("fieldDelimiter"));
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
            xr.parse(new InputSource(new StringReader(source)));
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
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
                ncpdpReader.setContentHandler(serializer);
                ncpdpReader.parse(new InputSource(new StringReader(source)));
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
		docSerializer.setPreserveSpace(true);
		Document document = docSerializer.fromXML(sourceMessage);
		return getMetadataFromDocument(document);
	}

	public Map<String, String> getMetadataFromDocument(Document document) {
		Map<String, String> map = new HashMap<String, String>();
		String sendingFacility = "";
		if (document.getElementsByTagName("ServiceProviderId") != null) {
			Node sender = document.getElementsByTagName("ServiceProviderId").item(0);
			if (sender != null) {
				sendingFacility = sender.getTextContent();
			}
		}
		String event = document.getDocumentElement().getNodeName();
		if (document.getElementsByTagName("TransactionCode") != null) {
			Node type = document.getElementsByTagName("TransactionCode").item(0);
			if (type != null) {
				event = type.getTextContent();
			}
		}
		String version = "";
		if (document.getElementsByTagName("VersionReleaseNumber") != null) {
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
