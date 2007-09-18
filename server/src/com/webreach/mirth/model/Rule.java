/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


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
