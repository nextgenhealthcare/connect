package com.webreach.mirth.server.tools;

public class StringToken extends Token {
	private String value;

	public StringToken(String value) {
		super(value);
		this.value = value;
	}

	boolean equalsIgnoreCase(String s) {
		return value.equalsIgnoreCase(s);
	}
}
