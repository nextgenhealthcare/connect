package com.webreach.mirth.client.ui;

public class FunctionListItem {
	private String name;
	private String tooltip;
	private String code;
	private CodeSnippetType type;

	public FunctionListItem(String name, String tooltip, String code, CodeSnippetType type) {
		this.name = name;
		this.tooltip = tooltip;
		this.code = code;
		this.type = type;
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

	public CodeSnippetType getType() {
		return type;
	}

	public void setType(CodeSnippetType type) {
		this.type = type;
	}
}
