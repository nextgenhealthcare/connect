package com.webreach.mirth.server.mule.adaptors;

import java.io.StringReader;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.DocumentSerializer;
import com.webreach.mirth.model.converters.ER7Serializer;
import com.webreach.mirth.model.converters.HAPIMessageSerializer;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.server.controllers.ChannelController;

public class HL7v2Adaptor extends Adaptor {
	
	protected void populateMessage() throws AdaptorException {
		messageObject.setRawDataProtocol(MessageObject.Protocol.HL7V2);
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.HL7V2);
		try {
			String xmlMessage = serializer.toXML(source.replaceAll("\n", "\r").trim());
			populateMetadata(source);
			messageObject.setTransformedData(xmlMessage);
		} catch (Exception e) {
			handleException(e);
		}
	}

	@Override
	public IXMLSerializer<String> getSerializer(Map properties) {
		return new ER7Serializer(properties);
	}
}
