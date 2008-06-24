package com.webreach.mirth.server.mule.adaptors;

import java.util.Map;

import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.model.converters.SerializerFactory;

public class HL7v2Adaptor extends Adaptor {

	protected void populateMessage(boolean emptyFilterAndTransformer) throws AdaptorException {
		messageObject.setRawDataProtocol(com.webreach.mirth.model.MessageObject.Protocol.HL7V2);
		messageObject.setTransformedDataProtocol(com.webreach.mirth.model.MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(com.webreach.mirth.model.MessageObject.Protocol.HL7V2);
		
		try {
			if (emptyFilterAndTransformer) {
				populateMetadataFromEncoded(source);
				messageObject.setEncodedData(source);
			} else {
				String xmlMessage = serializer.toXML(source);
				populateMetadataFromXML(xmlMessage);
				messageObject.setTransformedData(xmlMessage);
			}
		} catch (Exception e) {
			handleException(e);
		}
	}

	@Override
	public IXMLSerializer<String> getSerializer(Map properties) {
		return SerializerFactory.getSerializer(Protocol.HL7V2, properties);
	}
}
