package com.webreach.mirth.server.mule.transformers;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.ER7Serializer;

public class HL7ToMessageObject extends AbstractTransformer {
	private Logger logger = Logger.getLogger(this.getClass());
	private ER7Serializer serializer = new ER7Serializer();

	public HL7ToMessageObject() {
		super();
		registerSourceType(String.class);
		setReturnClass(MessageObject.class);
	}

	@Override
	public Object doTransform(Object src) throws TransformerException {
		String rawData = (String) src;
		String uniqueId = UUID.randomUUID().toString();
		
		logger.debug("creating new message object: id=" + uniqueId);
		
		MessageObject messageObject = new MessageObject();
		messageObject.setId(uniqueId);
		messageObject.setRawData(rawData);
		messageObject.setRawDataProtocol(MessageObject.Protocol.HL7);
		messageObject.setTransformedData(sanitize(serializer.toXML(rawData)));
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.HL7);
		messageObject.setStatus(MessageObject.Status.RECEIVED);
		
		// TODO: set this based on channel properties
		messageObject.setEncrypted(false);
		
		return messageObject;
	}

	// cleans up the XML
	public String sanitize(String source) {
		source.replaceAll("&#xd;", "");
		return source;
	}
}