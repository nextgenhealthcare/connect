package com.webreach.mirth.model;

public class Rule {
	public enum Operator {
		AND, OR, NONE
	}

	private int sequenceNumber;
	private String script;
	private Operator operator;

	public int getSequenceNumber() {
		return this.sequenceNumber;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public String getScript() {
		return this.script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public Operator getOperator() {
		return this.operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Rule[");
		builder.append("operator=" + getOperator().toString() + " ");
		builder.append("script=" + getScript() + " ");
		builder.append("]");
		return builder.toString();
	}
}
