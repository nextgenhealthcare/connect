package com.webreach.mirth.model;

public class Step {
	private int sequenceNumber;
	private String name;
	private String script;
	private String type;
	private Object data;

	public Object getData() {
		return this.data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScript() {
		return this.script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public int getSequenceNumber() {
		return this.sequenceNumber;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Step[");
		builder.append("sequenceNumber=" + getSequenceNumber() + ", ");
		builder.append("name=" + getName() + ", ");
		builder.append("script=" + getScript() + ", ");
		builder.append("type=" + getType() + ", ");
		builder.append("data=" + getData().toString());
		builder.append("]");
		return builder.toString();
	}
}
