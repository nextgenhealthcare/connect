package com.webreach.mirth.server.mule.adaptors;

import java.util.Map;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.model.converters.SerializerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Jun 25, 2007
 * Time: 2:24:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class NCPDPAdaptor extends Adaptor {
	protected void populateMessage() throws AdaptorException {

		messageObject.setRawDataProtocol(MessageObject.Protocol.NCPDP);
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.NCPDP);

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
		return SerializerFactory.getSerializer(MessageObject.Protocol.NCPDP, properties);
	}
}
