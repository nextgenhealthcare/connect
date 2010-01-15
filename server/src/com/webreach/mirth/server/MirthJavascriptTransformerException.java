package com.webreach.mirth.server;

import org.mozilla.javascript.RhinoException;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.server.controllers.ControllerFactory;

public class MirthJavascriptTransformerException extends Exception {
	private static final long serialVersionUID = 1L;
	String message;
	Exception exception;
	private String lineSeperator = System.getProperty("line.separator");

	public MirthJavascriptTransformerException(RhinoException e, String channelId, String connectorName, int lineOffset, String scriptSource, String sourceCode) {
		super(e);
		exception = e;

		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(lineSeperator);
		if (channelId != null) {
			Channel channel = ControllerFactory.getFactory().createChannelController().getChannelCache().get(channelId);
			if (channel != null) {
				sBuilder.append("CHANNEL:\t").append(channel.getName());
				sBuilder.append(lineSeperator);
			}
		}
		if (connectorName != null) {
			sBuilder.append("CONNECTOR:\t" + connectorName);
			sBuilder.append(lineSeperator);
		}
		if (scriptSource != null){
			sBuilder.append("SCRIPT SOURCE:\t" + scriptSource);
			sBuilder.append(lineSeperator);
		}
        if(sourceCode != null){
            sBuilder.append("SOURCE CODE:\t" + sourceCode);
            sBuilder.append(lineSeperator);
        }
		sBuilder.append("LINE NUMBER:\t").append(e.lineNumber() - lineOffset);
		sBuilder.append(lineSeperator);
		if (e.lineSource() != null){
			sBuilder.append("LINE SOURCE:\t").append(e.lineSource());
			sBuilder.append(lineSeperator);
		}
		sBuilder.append("DETAILS:\t" + e.details());
		message = sBuilder.toString();
	}

	public Throwable getCause() {
		return exception.getCause();
	}

	public String getMessage() {
		return message;
	}

	public String getLocalizedMessage() {
		return message;
	}

	public StackTraceElement[] getStackTrace() {
		return exception.getStackTrace();
	}

}
