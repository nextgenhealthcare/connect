/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.model;

import java.io.Serializable;

import com.webreach.mirth.util.EqualsUtil;

public class Rule implements Serializable {
	public enum Operator {
		AND, OR, NONE
	}

	private int sequenceNumber;
	private String name;
	private Object data;
	private String type;
	private String script;
	private Operator operator;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSequenceNumber() {
		return this.sequenceNumber;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public Object getData() {
		return this.data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Operator getOperator() {
		return this.operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}
	
	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getScript() {
		return this.script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		
		if (!(that instanceof Rule)) {
			return false;
		}
		
		Rule rule = (Rule) that;
		
		return
			EqualsUtil.areEqual(this.getSequenceNumber(), rule.getSequenceNumber()) &&
			EqualsUtil.areEqual(this.getName(), rule.getName()) &&
			EqualsUtil.areEqual(this.getType(), rule.getType()) &&
			EqualsUtil.areEqual(this.getData(), rule.getData()) &&
			EqualsUtil.areEqual(this.getScript(), rule.getScript()) &&
			EqualsUtil.areEqual(this.getOperator(), rule.getOperator());
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("name=" + getName() + " ");
		builder.append("operator=" + getOperator().toString() + " ");
		builder.append("script=" + getScript() + " ");
		builder.append("data=" + getData() + " ");
		builder.append("type=" + getType() + " ");
		builder.append("]");
		return builder.toString();
	}
}
