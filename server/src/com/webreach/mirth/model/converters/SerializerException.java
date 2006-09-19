package com.webreach.mirth.model.converters;

public class SerializerException extends Exception {
	public SerializerException(Throwable cause) {
		super(cause);
	}
	
	public SerializerException(String message, Throwable cause) {
		super(message, cause);
	}

	public SerializerException(String message) {
		super(message);
	}
}
