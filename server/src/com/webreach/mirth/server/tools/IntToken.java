package com.webreach.mirth.server.tools;

public class IntToken extends Token {
	private int value;

	public IntToken(String value) {
		super(value);
		this.value = Integer.parseInt(value);
	}

	int getValue() {
		return value;
	}
}