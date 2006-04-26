package com.webreach.mirth.core.util;

public class UnmarshalException extends Exception {
	public UnmarshalException(Throwable cause) {
		super(cause);
	}
	
	public UnmarshalException(String message) {
		super(message);
	}
	
	public UnmarshalException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
