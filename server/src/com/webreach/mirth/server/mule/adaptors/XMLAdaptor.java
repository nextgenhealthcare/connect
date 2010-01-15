package com.webreach.mirth.server.mule.adaptors;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.webreach.mirth.model.MessageObject;

public class XMLAdaptor extends Adaptor {
	protected void populateMessage(boolean emptyFilterAndTransformer) throws AdaptorException {
		messageObject.setRawDataProtocol(MessageObject.Protocol.XML);
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setType("XML");
		messageObject.setTransformedData(source);
		
		if (emptyFilterAndTransformer) {
			messageObject.setEncodedData(source);
		}
		
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document xmlDoc = docBuilder.parse(new InputSource(new StringReader(source)));
			messageObject.setSource(new String());
			messageObject.setType(xmlDoc.getDocumentElement().getNodeName());
			messageObject.setVersion("1.0");
		} catch (Exception e) {
			handleException(e);
		}
		
	}
}
