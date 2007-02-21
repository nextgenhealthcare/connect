package com.webreach.mirth.server.mule.adaptors;

import com.webreach.mirth.model.MessageObject;

public class XMLAdaptor extends Adaptor {
	protected void populateMessage() throws AdaptorException {
		messageObject.setRawDataProtocol(MessageObject.Protocol.XML);
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setType("XML");
	}
}
