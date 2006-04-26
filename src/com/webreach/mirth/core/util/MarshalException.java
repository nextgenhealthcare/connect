package com.webreach.mirth.core.util;

public class MarshalException extends Exception {
	public MarshalException(Throwable cause) {
		super(cause);
	}
	
	public MarshalException(String message, Throwable cause) {
		super(message, cause);
	}
}
