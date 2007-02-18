package com.webreach.mirth.server.mule.adaptors;

import java.util.Map;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.DefaultXMLSerializer;
import com.webreach.mirth.model.converters.IXMLSerializer;

public class XMLAdaptor extends Adaptor {
	protected void populateMessage() throws AdaptorException {
		messageObject.setRawDataProtocol(MessageObject.Protocol.XML);
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setType("XML");
	}
}
