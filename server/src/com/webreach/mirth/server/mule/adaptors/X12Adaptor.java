package com.webreach.mirth.server.mule.adaptors;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.DocumentSerializer;
import com.webreach.mirth.model.converters.ER7Serializer;
import com.webreach.mirth.model.converters.HAPIMessageSerializer;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.model.converters.X12Serializer;
import com.webreach.mirth.server.controllers.ChannelController;

public class X12Adaptor extends Adaptor {
	private X12Serializer x12Serializer;

	private HAPIMessageSerializer hapiSerializer = new HAPIMessageSerializer();

	protected void populateMessage() throws AdaptorException {
		x12Serializer = new X12Serializer(properties);
		messageObject.setRawDataProtocol(MessageObject.Protocol.X12);
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.X12);
		Channel channel = ChannelController.getChannelCache().get(messageObject.getChannelId());

		try {
			if (channel.getProperties().getProperty("recv_xml_encoded") != null && channel.getProperties().getProperty("recv_xml_encoded").equalsIgnoreCase("true")) {
				messageObject.setSource(new String());
				messageObject.setType(new String());
				messageObject.setVersion(new String());
				messageObject.setTransformedData(source);
			} else {
				String message = x12Serializer.toXML(source);
				DocumentSerializer docSerializer = new DocumentSerializer();
				docSerializer.setPreserveSpace(true);
				Document document = docSerializer.fromXML(message);
				Node sender = document.getElementsByTagName("ISA.6").item(0);
				String sendingFacility = sender.getNodeValue();
				Node type = document.getElementsByTagName("ST.1").item(0);
				String event = type.getNodeValue();
				Node versionNode = document.getElementsByTagName("GS.8").item(0);
				String version = versionNode.getNodeValue();
				// String event = terser.get("/MSH-9-1") + "-" +
				// terser.get("/MSH-9-2");
				messageObject.setSource(sendingFacility);
				messageObject.setType(event);
				messageObject.setVersion(version);
				messageObject.setTransformedData(message);
			}
		} catch (Exception e) {
			handleException(e);
		}
	}

	protected MessageObject doConvertMessage(MessageObject messageObject, String template, String channelId, boolean encryptData) throws AdaptorException {
		try {
			if (template != null){
				x12Serializer = new X12Serializer(properties);
				DocumentSerializer docSerializer = new DocumentSerializer();
				docSerializer.setPreserveSpace(true);
				Document document = docSerializer.fromXML(template);
				Node sender = document.getElementsByTagName("ISA.6").item(0).getFirstChild();
				String sendingFacility = sender.getNodeValue();
				Node type = document.getElementsByTagName("ST.1").item(0).getFirstChild();
				String event = type.getNodeValue();
				Node versionNode = document.getElementsByTagName("GS.8").item(0).getFirstChild();
				String version = versionNode.getNodeValue();
				// String event = terser.get("/MSH-9-1") + "-" +
				// terser.get("/MSH-9-2");
				messageObject.setSource(sendingFacility);
				messageObject.setType(event);
				messageObject.setVersion(version);
				messageObject.setTransformedData(template);
			}
			
		} catch (Exception e) {
			handleException(e);
		}
		return messageObject;
	}

	@Override
	public IXMLSerializer<String> getSerializer(Map properties) {
		return new X12Serializer(properties);
	}
}
