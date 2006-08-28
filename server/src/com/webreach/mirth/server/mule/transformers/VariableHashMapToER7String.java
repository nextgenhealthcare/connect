package com.webreach.mirth.server.mule.transformers;

import java.util.HashMap;

import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

public class VariableHashMapToER7String extends AbstractTransformer {

	public VariableHashMapToER7String() {
		super();
		registerSourceType(HashMap.class);
		setReturnClass(String.class);
	}
	
	public Object doTransform(Object source) throws TransformerException {
		return (String) ((HashMap) source).get("HL7 ER7");
	}

}
