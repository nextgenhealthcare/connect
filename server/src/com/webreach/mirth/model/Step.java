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

public class Step implements Serializable {
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
	
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		
		if (!(that instanceof Step)) {
			return false;
		}
		
		Step step = (Step) that;
		
		return
			EqualsUtil.areEqual(this.getSequenceNumber(), step.getSequenceNumber()) &&
			EqualsUtil.areEqual(this.getName(), step.getName()) &&
			EqualsUtil.areEqual(this.getScript(), step.getScript()) &&
			EqualsUtil.areEqual(this.getType(), step.getType());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("sequenceNumber=" + getSequenceNumber() + ", ");
		builder.append("name=" + getName() + ", ");
		builder.append("script=" + getScript() + ", ");
		builder.append("type=" + getType() + ", ");
		builder.append("data=" + getData().toString());
		builder.append("]");
		return builder.toString();
	}
}
