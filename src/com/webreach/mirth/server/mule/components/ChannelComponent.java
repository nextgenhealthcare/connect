package com.webreach.mirth.server.mule.components;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.Callable;

public class ChannelComponent implements Callable {
	private Logger logger = Logger.getLogger(this.getClass());
	public static HashMap globalMap = new HashMap();
	
	public Object onCall(UMOEventContext eventContext) throws Exception {
		return eventContext.getTransformedMessage();
	}

}
