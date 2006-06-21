package com.webreach.mirth.model;

public class Rule {
	private String script;

	public String getScript() {
		return this.script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Rule[");
		builder.append("script=" + getScript() + ", ");
		builder.append("]");
		return builder.toString();
	}
}
