package com.webreach.mirth.model;

import java.io.Serializable;

import org.mule.umo.endpoint.UMOEndpointURI;

import com.webreach.mirth.util.EqualsUtil;

public class QueuedMessage implements Serializable {
	private MessageObject messageObject;
	private UMOEndpointURI endpointUri;
	
	public MessageObject getMessageObject() {
		return messageObject;
	}
	public void setMessageObject(MessageObject messageObject) {
		this.messageObject = messageObject;
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
			EqualsUtil.areEqual(this.getEndpointUri(), queuedMessage.getEndpointUri()) &&
            EqualsUtil.areEqual(this.getMessageObject(), queuedMessage.getMessageObject());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("endpointUri=");
		builder.append(this.getEndpointUri());
		builder.append(", ");
        builder.append("messageObject=");
        builder.append(this.getMessageObject().toString());
		builder.append("]");
		return builder.toString();
	}
	public UMOEndpointURI getEndpointUri() {
		return endpointUri;
	}
	public void setEndpointUri(UMOEndpointURI endpointUri) {
		this.endpointUri = endpointUri;
	}
}
