package com.webreach.mirth.server.mule.components;

import org.apache.log4j.Logger;
import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.Callable;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;

import com.webreach.mirth.model.MessageEvent;
import com.webreach.mirth.server.controllers.MessageLogger;

public class ChannelComponent implements Callable {
	private Logger logger = Logger.getLogger(this.getClass());
	
	public Object onCall(UMOEventContext eventContext) throws Exception {
		logMessageEvent(eventContext);
		return eventContext.getTransformedMessage();
	}

	private void logMessageEvent(UMOEventContext eventContext) throws Exception {
		logger.info("unique message id: " + eventContext.getMessage().getUniqueId());
		
		int channelId = Integer.valueOf(eventContext.getComponentDescriptor().getName()).intValue();

		PipeParser pipeParser = new PipeParser();
		Message message = pipeParser.parse(eventContext.getMessageAsString());
		Terser terser = new Terser(message);
		String sendingFacility = terser.get("/MSH-3-1");
		String controlId = terser.get("/MSH-10");

		MessageLogger messageLogger = new MessageLogger();
		MessageEvent messageEvent = new MessageEvent();
		messageEvent.setChannelId(channelId);
		messageEvent.setSendingFacility(sendingFacility);
		messageEvent.setEvent(message.getName());
		messageEvent.setControlId(controlId);
		messageEvent.setMessage(eventContext.getMessageAsString());
		messageEvent.setStatus(MessageEvent.Status.RECEIVED);
		messageLogger.logMessageEvent(messageEvent);
	}
}
