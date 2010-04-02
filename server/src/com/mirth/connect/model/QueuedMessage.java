/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;

import org.mule.umo.endpoint.UMOEndpointURI;

import com.mirth.connect.util.EqualsUtil;

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
