package com.webreach.mirth.server.mule.adaptors;

public class AdaptorException extends Exception {
	public AdaptorException(Throwable cause) {
		super(cause);
	}

	public AdaptorException(String message) {
		super(message);
	}

	public AdaptorException(String message, Throwable cause) {
		super(message, cause);
	}
}
