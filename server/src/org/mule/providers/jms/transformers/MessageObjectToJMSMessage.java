package org.mule.providers.jms.transformers;

import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.model.MessageObject;

public class MessageObjectToJMSMessage extends AbstractJmsTransformer {
	public Object doTransform(Object src) throws TransformerException {
		if (src instanceof MessageObject) {
			MessageObject messageObject = (MessageObject) src;
			return transformToMessage(messageObject.getEncodedData());
		}

		return null;
	}
}
