package com.webreach.mirth.server.mule;

import org.mule.impl.DefaultComponentExceptionStrategy;

import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.server.controllers.SystemLogger;
import com.webreach.mirth.server.mule.util.StackTracePrinter;

public class ExceptionStrategy extends DefaultComponentExceptionStrategy {
	protected void defaultHandler(Throwable t) {
		super.defaultHandler(t);
	}
	
	protected void logException(Throwable t) {
		SystemLogger systemLogger = new SystemLogger();
		StackTracePrinter stackTracePrinter = new StackTracePrinter();
		SystemEvent event = new SystemEvent("Exception occured in channel.");
		event.setDescription(stackTracePrinter.stackTraceToString(t));
		systemLogger.logSystemEvent(event);
	}
}
