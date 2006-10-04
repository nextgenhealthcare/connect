package org.mule.providers.pdf;

public class BBToken {
	private String value;
	private String type;

	public BBToken(String value, String type) {
		this.value = value;
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public String getType() {
		return type;
	}
}
