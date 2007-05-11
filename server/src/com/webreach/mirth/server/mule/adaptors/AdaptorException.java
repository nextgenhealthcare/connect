package com.webreach.mirth.server.mule.adaptors;

public class AdaptorException extends Exception {
	private static final long serialVersionUID = 1L;

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
