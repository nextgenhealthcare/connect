package com.webreach.mirth.server.mule.adaptors;

import java.util.Map;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.ER7Serializer;
import com.webreach.mirth.model.converters.HAPIMessageSerializer;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.server.controllers.ChannelController;

public class HL7v2Adaptor extends Adaptor {
	private IXMLSerializer<String> xmlSerializer;
	private HAPIMessageSerializer hapiSerializer = new HAPIMessageSerializer();

	protected void populateMessage() throws AdaptorException {
        xmlSerializer = getSerializer(this.properties);
		messageObject.setRawDataProtocol(MessageObject.Protocol.HL7V2);
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.HL7V2);
		Channel channel = ChannelController.getChannelCache().get(messageObject.getChannelId());

		try {
			if (channel.getProperties().getProperty("recv_xml_encoded") != null && channel.getProperties().getProperty("recv_xml_encoded").equalsIgnoreCase("true")) {
				messageObject.setSource(new String());
				messageObject.setType(new String());
				messageObject.setVersion(new String());
				messageObject.setTransformedData(source);
			} else {
				Message message = hapiSerializer.deserialize(source.replaceAll("\n", "\r").trim());
				Terser terser = new Terser(message);
				String sendingFacility = terser.get("/MSH-4-1");
				String event = terser.get("/MSH-9-1") + "-" + terser.get("/MSH-9-2");
				messageObject.setSource(sendingFacility);
				messageObject.setType(event);
				messageObject.setVersion(message.getVersion());
				messageObject.setTransformedData(xmlSerializer.toXML(source.replaceAll("\n", "\r").trim()));
			}
		} catch (Exception e) {
			handleException(e);
		}
	}

	@Override
	public IXMLSerializer<String> getSerializer(Map properties) {
		return new ER7Serializer(properties);
	}
}
