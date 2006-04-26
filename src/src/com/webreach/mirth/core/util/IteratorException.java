package com.webreach.mirth.core.util;

public class IteratorException extends RuntimeException {
	public IteratorException() {
		super();
	}
	
	public IteratorException(String message) {
		super(message);
	}
	
	public IteratorException(Exception e) {
		super(e);
	}
}
