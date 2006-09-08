package com.webreach.mirth.client.core;

public class ListHandlerException extends Exception {
	public ListHandlerException(Throwable cause) {
		super(cause);
	}
	
	public ListHandlerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ListHandlerException(String message) {
		super(message);
	}
}
