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
import com.webreach.mirth.model.converters.EDISerializer;
import com.webreach.mirth.model.converters.ER7Serializer;
import com.webreach.mirth.model.converters.HAPIMessageSerializer;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.model.converters.X12Serializer;
import com.webreach.mirth.server.controllers.ChannelController;

public class EDIAdaptor extends Adaptor {
	/* Sample EDIFact Message
		UNB+IATB:1+6XPPC+LHPPC+940101:0950+1’
		UNH+1+PAORES:93:1:IA’
		MSG+1:45’
		IFT+3+XYZCOMPANY AVAILABILITY’
		ERC+A7V:1:AMD’
		IFT+3+NO MORE FLIGHTS’
		ODI’
		TVL+240493:1000::1220+FRA+JFK+DL+400+C’
		PDI++C:3+Y::3+F::1’
		APD+74C:0:::6++++++6X’
		TVL+240493:1740::2030+JFK+MIA+DL+081+C'
		PDI++C:4’
		APD+EM2:0:1630::6+++++++DA’
		UNT+13+1’
		UNZ+1+1’
	 */
	private EDISerializer ediSerializer;
	protected void populateMessage() throws AdaptorException {
		ediSerializer = new EDISerializer(properties);
		messageObject.setRawDataProtocol(MessageObject.Protocol.EDI);
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.EDI);
		Channel channel = ChannelController.getChannelCache().get(messageObject.getChannelId());

		try {
			if (channel.getProperties().getProperty("recv_xml_encoded") != null && channel.getProperties().getProperty("recv_xml_encoded").equalsIgnoreCase("true")) {
				messageObject.setSource(new String());
				messageObject.setType(new String());
				messageObject.setVersion(new String());
				messageObject.setTransformedData(source);
			} else {
				String message = ediSerializer.toXML(source);
				messageObject= populateMetadata(messageObject, message);
				messageObject.setTransformedData(message);
			}
		} catch (Exception e) {
			handleException(e);
		}
	}
	//TODO: This should probably be an abstract method...
	private MessageObject populateMetadata(MessageObject messageObject, String xmlMessage) {
		DocumentSerializer docSerializer = new DocumentSerializer();
		docSerializer.setPreserveSpace(true);
		Document document = docSerializer.fromXML(xmlMessage);
		String sendingFacility = "";
		if (document.getElementsByTagName("ISA.6") != null){
			Node sender = document.getElementsByTagName("ISA.6").item(0);
			sendingFacility = sender.getNodeValue();
		}else if (document.getElementsByTagName("GS.2") != null){
			Node sender = document.getElementsByTagName("GS.2").item(0);
			sendingFacility = sender.getNodeValue();
		}				
		String event = "Unknown";
		if (document.getElementsByTagName("ST.1") != null){
			Node type = document.getElementsByTagName("ST.1").item(0);
			event = type.getNodeValue();
		}
		String version = "";
		if(document.getElementsByTagName("GS.8") != null){
			Node versionNode = document.getElementsByTagName("GS.8").item(0);
			version = versionNode.getNodeValue();
		}
		messageObject.setSource(sendingFacility);
		messageObject.setType(event);
		messageObject.setVersion(version);
		return messageObject;
	}

	protected MessageObject doConvertMessage(MessageObject messageObject, String template, String channelId, boolean encryptData) throws AdaptorException {
		try {
			if (template != null){
				messageObject = populateMetadata(messageObject, template);
				messageObject.setTransformedData(template);
			}
		} catch (Exception e) {
			handleException(e);
		}
		return messageObject;
	}

	@Override
	public IXMLSerializer<String> getSerializer(Map properties) {
		return new EDISerializer(properties);
	}
}
