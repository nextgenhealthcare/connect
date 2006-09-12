package com.webreach.mirth.server.mule.transformers;

import org.apache.log4j.Logger;
import org.mule.impl.UMODescriptorAware;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.UMODescriptor;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.model.converters.ER7Serializer;
import com.webreach.mirth.server.mule.MessageObject;

public class HL7ToMessageObject extends AbstractTransformer implements UMODescriptorAware {
	private Logger logger = Logger.getLogger(this.getClass());
	private ER7Serializer serializer = new ER7Serializer();
	private UMODescriptor descriptor;

	public HL7ToMessageObject() {
		super();
		registerSourceType(String.class);
		setReturnClass(MessageObject.class);
	}

	public void setDescriptor(UMODescriptor descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public Object doTransform(Object src) throws TransformerException {
		logger.debug("creating new message object from source: " + src);
		String rawData = (String) src;

		MessageObject messageObject = new MessageObject();
		messageObject.setChannelId(descriptor.getName());
		messageObject.setRawData(rawData);
		messageObject.setRawDataProtocol(MessageObject.Protocol.HL7);
		messageObject.setTransformedData(sanitize(serializer.toXML(rawData)));
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.HL7);
		return messageObject;
	}

	// cleans up the XML
	public String sanitize(String source) {
		source.replaceAll("&#xd;", "");
		return source;
	}
}