package com.webreach.mirth.server.mule.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;

public class StackTracePrinter {
	private Logger logger = Logger.getLogger(this.getClass());
	
	public String stackTraceToString(Throwable exception) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		exception.printStackTrace(printWriter);
		String exceptionString = stringWriter.toString();
		
		try {
			stringWriter.close();	
		} catch (Exception e) {
			logger.warn(e);
		}
		
		printWriter.close();
		return exceptionString;
	}
}
