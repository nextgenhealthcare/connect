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
	/* Sample X12Message
		ISA*00*          *00*          *ZZ*SUBMITTERS ID  *ZZ*RECEIVERS ID   *010122*1253*U*00401*000000905*1*T*:~
		GS*HC*SenderID*ReceiverID*20010122*1310*1*X*004010X098~
		ST*837*0021~
		BHT*0019*00*0123*19981015*1023*RP~
		REF*87*004010X098D~
		NM1*41*2*JAMES A SMITH, M.D.*****46*TGJ23~
		PER*IC*LINDA*TE*8015552222*EX*231~
		NM1*40*2*ABC CLEARINGHOUSE*****46*66783JJT~
		HL*1**20*1~
		NM1*85*2*JAMES A SMITH, M.D.*****24*587654321~
		N3*234 Seaway St~
		N4*Miami*FL*33111~
		HL*2*1*22*0~
		SBR*P*18*******CI~
		NM1*IL*1*SMITH*TED****MI*000221111A~
		N3*236 N MAIN ST~
		N4*MIAMI*FL*33413~
		DMG*D8*19430501*M~
		NM1*PR*2*AETNA*****PI*741234~
		CLM*26462967*0***11::1*Y*A*Y*Y*C~
		DTP*431*D8*19981003~
		REF*D9*17312345600006351~
		HI*BK:0340*BF:V7389~
		NM1*82*1*KILDARE*BEN****34*112233334~
		PRV*PE*ZZ*203BF0100Y~
		LX*1~
		SV1*HC:99024*0*UN*1***1**N~
		DTP*472*D8*19981003~
		SE*27*0021~
		GE*1*1~
		IEA*1*000000905~
	 */
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
			sendingFacility = sender.getFirstChild().getNodeValue();
		}else if (document.getElementsByTagName("GS.2") != null){
			Node sender = document.getElementsByTagName("GS.2").item(0);
			sendingFacility = sender.getFirstChild().getNodeValue();
		}				
		String event = "Unknown";
		if (document.getElementsByTagName("ST.1") != null){
			Node type = document.getElementsByTagName("ST.1").item(0);
			event = type.getFirstChild().getNodeValue();
		}
		String version = "";
		if(document.getElementsByTagName("GS.8") != null){
			Node versionNode = document.getElementsByTagName("GS.8").item(0);
			version = versionNode.getFirstChild().getNodeValue();
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
		return new X12Serializer(properties);
	}
}
