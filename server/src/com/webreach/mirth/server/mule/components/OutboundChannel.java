package com.webreach.mirth.server.mule.components;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.Callable;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

import com.webreach.mirth.model.MessageEvent;
import com.webreach.mirth.server.controllers.MessageLogger;

public class OutboundChannel implements Callable{
	private Logger logger = Logger.getLogger(this.getClass());
	public static HashMap globalMap = new HashMap();
	
	public Object onCall(UMOEventContext eventContext) throws Exception {
		return eventContext.getTransformedMessage();
	}
	
}
