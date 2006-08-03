package com.webreach.mirth.client.ui;

public class FunctionListItem {
	private String name;
	private String tooltip;
	private String code;

	public FunctionListItem(String name, String tooltip, String code) {
		this.name = name;
		this.tooltip = tooltip;
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTooltip() {
		return tooltip;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
}
