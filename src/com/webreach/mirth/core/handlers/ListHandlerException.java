package com.webreach.mirth.core.handlers;

public class ListHandlerException extends RuntimeException {
	public ListHandlerException() {
		super();
	}
	
	public ListHandlerException(String message) {
		super(message);
	}
	
	public ListHandlerException(Exception e) {
		super(e);
	}
}
