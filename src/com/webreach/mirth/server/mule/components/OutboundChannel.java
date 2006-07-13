package com.webreach.mirth.server.mule.components;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mule.umo.UMOEventContext;

public class OutboundChannel {
	private Logger logger = Logger.getLogger(this.getClass());
	public static HashMap globalMap = new HashMap();
	
	public Object onCall(UMOEventContext eventContext) throws Exception {
		logger.debug("logging outbound message:\n" + eventContext.getMessageAsString());
		return eventContext.getTransformedMessage();
	}
}
