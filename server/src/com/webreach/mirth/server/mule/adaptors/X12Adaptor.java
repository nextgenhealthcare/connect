package com.webreach.mirth.server.mule.adaptors;

import java.util.Map;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.model.converters.SerializerFactory;

public class X12Adaptor extends Adaptor {
	protected void populateMessage() throws AdaptorException {

		messageObject.setRawDataProtocol(MessageObject.Protocol.X12);
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.X12);

		try {
			String message = serializer.toXML(source);
			messageObject.setTransformedData(message);
			populateMetadataFromXML(message);
		} catch (Exception e) {
			handleException(e);
		}
	}

	@Override
	public IXMLSerializer<String> getSerializer(Map properties) {
		return SerializerFactory.getSerializer(MessageObject.Protocol.X12, properties);
	}
}
