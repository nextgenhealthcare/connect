/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.mule.components;

import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.Callable;

public class SoapChannel implements Callable, SoapService {
	public Object onCall(UMOEventContext eventContext) throws Exception {
		return eventContext.getTransformedMessage();
	}

	public String acceptMessage(java.lang.String message) {
		return message;
	}


}
