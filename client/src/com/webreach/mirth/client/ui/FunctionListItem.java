package com.webreach.mirth.client.ui;

public class FunctionListItem {
	private String _name;
	private String _tooltip;
	private String _code;
	
	public String getCode() {
		return _code;
	}

	public void setCode(String code) {
		_code = code;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public String getTooltip() {
		return _tooltip;
	}

	public void setTooltip(String tooltip) {
		_tooltip = tooltip;
	}

	public FunctionListItem(String name, String tooltip, String code){
		_name = name;
		_tooltip = tooltip;
		_code = code;
	}
}
