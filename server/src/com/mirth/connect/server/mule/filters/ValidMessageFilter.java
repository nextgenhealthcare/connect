/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.mule.filters;

import org.mule.umo.UMOFilter;
import org.mule.umo.UMOMessage;

import com.mirth.connect.model.MessageObject;

public class ValidMessageFilter implements UMOFilter {

	public boolean accept(UMOMessage message) {
		MessageObject messageObject = (MessageObject) message.getPayload();
		return messageObject.getStatus().equals(MessageObject.Status.TRANSFORMED);
	}
}
