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

public class ChannelComponent implements Callable {
	private Logger logger = Logger.getLogger(this.getClass());
	public static HashMap globalMap = new HashMap();
	
	public Object onCall(UMOEventContext eventContext) throws Exception {
		logMessageEvent(eventContext);
		return eventContext.getTransformedMessage();
	}

	private void logMessageEvent(UMOEventContext eventContext) throws Exception {
		logger.debug("logging message:\n" + eventContext.getMessageAsString());
		
		int channelId = Integer.valueOf(eventContext.getComponentDescriptor().getName()).intValue();

		PipeParser pipeParser = new PipeParser();
		pipeParser.setValidationContext(new NoValidation());
		Message message = pipeParser.parse(eventContext.getMessageAsString());
		Terser terser = new Terser(message);
		String sendingFacility = terser.get("/MSH-3-1");
		String controlId = terser.get("/MSH-10");
		String event = terser.get("/MSH-9-1") + "-" + terser.get("/MSH-9-2") + " (" + message.getVersion() + ")";

		MessageLogger messageLogger = new MessageLogger();
		MessageEvent messageEvent = new MessageEvent();
		messageEvent.setChannelId(channelId);
		messageEvent.setSendingFacility(sendingFacility);
		messageEvent.setEvent(event);
		messageEvent.setControlId(controlId);
		messageEvent.setMessage(eventContext.getMessageAsString());
		messageEvent.setStatus(MessageEvent.Status.RECEIVED);
		messageLogger.logMessageEvent(messageEvent);
	}
}
