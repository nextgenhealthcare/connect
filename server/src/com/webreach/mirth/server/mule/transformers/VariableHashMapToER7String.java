package com.webreach.mirth.server.mule.transformers;

import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.model.MessageObject;

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
