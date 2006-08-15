package com.webreach.mirth.server;

public class MirthException extends Exception {
	public MirthException(Throwable cause) {
		super(cause);
	}

	public MirthException(String message) {
		super(message);
	}

	public MirthException(String message, Throwable cause) {
		super(message, cause);
	}
}
