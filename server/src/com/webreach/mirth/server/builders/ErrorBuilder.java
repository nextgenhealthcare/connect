package com.webreach.mirth.server.builders;

import org.mozilla.javascript.RhinoException;

import com.webreach.mirth.server.util.StackTracePrinter;

public class ErrorBuilder {

	private String lineSeperator = System.getProperty("line.separator");
	public String getErrorString(String errorType, Throwable e){
		return getErrorString(errorType, null, e);
	}
	public String getErrorString(String errorType, String customMessage, Throwable e){
		//error source is initialized to blank
		String lineSource = null;

		// if the exception occured during execution of the script, get the
		// line of code that caused the error
		if (e instanceof RhinoException) {
			RhinoException re = (RhinoException) e;
			lineSource = re.lineSource();
		}

		// construct the error message
		StringBuilder errorMessage = new StringBuilder();
		String lineSeperator = System.getProperty("line.separator");
		errorMessage.append(errorType + lineSeperator);

		if (lineSource != null) {
			errorMessage.append("ERROR SOURCE:\t" + lineSource + lineSeperator);
		}
		if (customMessage != null && customMessage.length() > 0){
			customMessage+=lineSeperator;
		}
		errorMessage.append("ERROR MESSAGE:\t" + customMessage + StackTracePrinter.stackTraceToString(e) + lineSeperator);
		return errorMessage.toString();
	}
}
