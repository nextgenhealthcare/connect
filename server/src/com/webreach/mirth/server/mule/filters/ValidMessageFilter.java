package com.webreach.mirth.server.mule.filters;

import org.mule.umo.UMOFilter;
import org.mule.umo.UMOMessage;

import com.webreach.mirth.model.MessageObject;

public class ValidMessageFilter implements UMOFilter {

	public boolean accept(UMOMessage message) {
		MessageObject messageObject = (MessageObject) message.getPayload();
		return messageObject.getStatus().equals(MessageObject.Status.TRANSFORMED);
	}
}
