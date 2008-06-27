package com.webreach.mirth.model;

import java.io.Serializable;

import com.webreach.mirth.util.EqualsUtil;

public class QueuedMessage implements Serializable {
	private MessageObject messageObject;
	private String endpointURI;
	public MessageObject getMessageObject() {
		return messageObject;
	}
	public void setMessageObject(MessageObject messageObject) {
		this.messageObject = messageObject;
	}
	public String getEndpointURI() {
		return endpointURI;
	}
	public void setEndpointUri(String endpointURI) {
		this.endpointURI = endpointURI;
	}
	
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}

		if (!(that instanceof QueuedMessage)) {
			return false;
		}

		QueuedMessage queuedMessage = (QueuedMessage) that;

		return
			EqualsUtil.areEqual(this.getEndpointURI(), queuedMessage.getEndpointURI()) &&
            EqualsUtil.areEqual(this.getMessageObject(), queuedMessage.getMessageObject());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("endpointUri=");
		builder.append(this.getEndpointURI());
		builder.append(", ");
        builder.append("messageObject=");
        builder.append(this.getMessageObject().toString());
		builder.append("]");
		return builder.toString();
	}
}
