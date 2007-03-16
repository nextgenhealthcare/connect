package com.webreach.mirth.server.mule.adaptors;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.DocumentSerializer;
import com.webreach.mirth.model.converters.EDISerializer;
import com.webreach.mirth.model.converters.IXMLSerializer;
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
	

	protected void populateMessage() throws AdaptorException {
		
		messageObject.setRawDataProtocol(MessageObject.Protocol.EDI);
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.EDI);

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
		return new EDISerializer(properties);
	}
}
