package com.webreach.mirth.core.util;

public class ConfigurationException extends Exception {
	public ConfigurationException() {
		super();
	}
	
	public ConfigurationException(Throwable cause) {
		super(cause);
	}
	
	public ConfigurationException(String message) {
		super(message);
	}
	
	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}
