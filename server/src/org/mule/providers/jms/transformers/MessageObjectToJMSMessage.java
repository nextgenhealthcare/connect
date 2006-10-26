package org.mule.providers.jms.transformers;

import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.controllers.MessageObjectController;

public class MessageObjectToJMSMessage extends AbstractJmsTransformer {
	private MessageObjectController messageObjectController = new MessageObjectController();
	
	public Object doTransform(Object src) throws TransformerException {
		if (src instanceof MessageObject) {
			MessageObject messageObject = (MessageObject) src;
			
			// update the message status to sent
			messageObject.setStatus(MessageObject.Status.SENT);
			messageObjectController.updateMessage(messageObject);

			return transformToMessage(messageObject.getEncodedData());
		}

		return null;
	}
}
