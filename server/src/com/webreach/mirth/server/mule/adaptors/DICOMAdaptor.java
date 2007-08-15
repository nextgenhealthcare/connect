package com.webreach.mirth.server.mule.adaptors;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.model.converters.SerializerFactory;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Aug 7, 2007
 * Time: 1:28:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class DICOMAdaptor extends Adaptor {
	protected void populateMessage() throws AdaptorException {

		messageObject.setRawDataProtocol(MessageObject.Protocol.DICOM);
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.DICOM);

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
		return SerializerFactory.getSerializer(MessageObject.Protocol.DICOM, properties);
	}
}
