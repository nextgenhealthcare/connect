package com.webreach.mirth.server.mule.components;

import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.Callable;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.util.ACKGenerator;

public class SoapChannel implements Callable, SoapService {
	public Object onCall(UMOEventContext eventContext) throws Exception {
		return eventContext.getTransformedMessage();
	}

	public String acceptMessage(String message) {
		/*
		MessageObject mo = (MessageObject)message;
	
		ACKGenerator generator = new ACKGenerator();
		try{
			return generator.generateAckResponse(mo.getRawData());
		}catch (Exception e){
			return new String();
		}
		*/
		return message;
		
	}


}
