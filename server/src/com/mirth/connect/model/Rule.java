/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("rule")
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
			ObjectUtils.equals(this.getSequenceNumber(), rule.getSequenceNumber()) &&
			ObjectUtils.equals(this.getName(), rule.getName()) &&
			ObjectUtils.equals(this.getType(), rule.getType()) &&
			ObjectUtils.equals(this.getData(), rule.getData()) &&
			ObjectUtils.equals(this.getScript(), rule.getScript()) &&
			ObjectUtils.equals(this.getOperator(), rule.getOperator());
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
