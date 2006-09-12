package com.webreach.mirth.server.mule.components;

import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.Callable;

public class Channel implements Callable {
	public Object onCall(UMOEventContext eventContext) throws Exception {
		return eventContext.getTransformedMessage();
	}
}
