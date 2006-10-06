package com.webreach.mirth.server.mule.transformers;

import org.apache.log4j.Logger;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.ER7Serializer;
import com.webreach.mirth.model.converters.HAPIMessageSerializer;
import com.webreach.mirth.model.converters.SerializerException;
import com.webreach.mirth.server.mule.util.StackTracePrinter;

public class HL7ToMessageObject extends AbstractTransformer {
	private Logger logger = Logger.getLogger(this.getClass());
	private ER7Serializer xmlSerializer = new ER7Serializer();
	private HAPIMessageSerializer hapiSerializer = new HAPIMessageSerializer();
	private StackTracePrinter stackTracePrinter = new StackTracePrinter();

	public HL7ToMessageObject() {
		super();
		registerSourceType(String.class);
		setReturnClass(MessageObject.class);
	}

	@Override
	public Object doTransform(Object src) throws TransformerException {
		// trim removes any uneeded whitespace at the beginning and end of the
		// message
		String rawData = ((String) src).trim();
		MessageObject messageObject = new MessageObject();
		messageObject.setRawData(rawData);
		messageObject.setRawDataProtocol(MessageObject.Protocol.HL7);

		try {
			messageObject.setVersion(hapiSerializer.deserialize(rawData).getVersion());
			messageObject.setTransformedData(sanitize(xmlSerializer.toXML(rawData)));
		} catch (SerializerException e) {
			logger.warn("error transforming message", e);
			messageObject.setErrors(stackTracePrinter.stackTraceToString(e));
		}

		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.HL7);
		messageObject.setStatus(MessageObject.Status.RECEIVED);
		return messageObject;
	}

	// cleans up the XML
	private String sanitize(String source) {
		return source.replaceAll("\n", "\r"); // FIXME(newline) find a better
												// solution
	}
}