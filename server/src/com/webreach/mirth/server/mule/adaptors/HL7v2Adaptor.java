package com.webreach.mirth.server.mule.adaptors;

import java.util.Map;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.model.converters.SerializerFactory;

public class HL7v2Adaptor extends Adaptor {

	protected void populateMessage() throws AdaptorException {
		messageObject.setRawDataProtocol(MessageObject.Protocol.HL7V2);
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.HL7V2);
		try {
			String xmlMessage = serializer.toXML(source.replaceAll("\n", "\r").trim());
			populateMetadataFromXML(xmlMessage);
			messageObject.setTransformedData(xmlMessage);
		} catch (Exception e) {
			handleException(e);
		}
	}

	@Override
	public IXMLSerializer<String> getSerializer(Map properties) {
		return SerializerFactory.getSerializer(Protocol.HL7V2, properties);
	}
}
