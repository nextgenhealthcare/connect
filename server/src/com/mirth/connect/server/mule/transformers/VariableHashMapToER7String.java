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

import com.mirth.connect.model.MessageObject;

public class VariableHashMapToER7String extends AbstractTransformer {
	public VariableHashMapToER7String() {
		super();
		registerSourceType(MessageObject.class);
		setReturnClass(String.class);
	}
	
	public Object doTransform(Object source) throws TransformerException {
		MessageObject messageObject = (MessageObject) source;
		return messageObject.getTransformedData();
	}

}
