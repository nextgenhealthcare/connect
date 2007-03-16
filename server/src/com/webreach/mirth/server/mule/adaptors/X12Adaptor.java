package com.webreach.mirth.server.mule.adaptors;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.DocumentSerializer;
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
	


	protected void populateMessage() throws AdaptorException {
	
		messageObject.setRawDataProtocol(MessageObject.Protocol.X12);
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.X12);

		try {
			String message = serializer.toXML(source);
			messageObject.setTransformedData(message);
			populateMetadata(message);
		} catch (Exception e) {
			handleException(e);
		}
	}

	


	@Override
	public IXMLSerializer<String> getSerializer(Map properties) {
		return new X12Serializer(properties);
	}
}
