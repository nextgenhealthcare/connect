/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.mule.transformers;

import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

/**
 * Transforms HTTP request to String object
 * 
 */
public class HttpRequestToString extends AbstractTransformer {
	public HttpRequestToString() {
		super();
		this.registerSourceType(String.class);
		this.registerSourceType(byte[].class);
		setReturnClass(String.class);
	}

    public Object doTransform(Object src) throws TransformerException {
		String param;
		
		if (src instanceof byte[]) {
			param = new String((byte[]) src);
		} else {
			param = src.toString();
		}
		
		//int splitIndex = param.indexOf("=");
	
		//if (splitIndex > -1) {
			// remove the question mark from a GET
			return param;//.substring(splitIndex + 1);
		//} else {
			// throw new TransformerException(org.mule.config.i18n.Message.createStaticMessage("Failed to parse HTTP string: " + param), this);
			//return param;
	//	}
    }
}
