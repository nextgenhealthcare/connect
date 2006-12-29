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

import com.webreach.mirth.util.EqualsUtil;

public class Transport {
	public enum Type {
		LISTENER, SENDER
	};

	private String name;
	private String className;
	private String transformers;
	private String protocol;
	private Type type;
	private boolean inbound;
	private boolean outbound;

	public String getClassName() {
		return this.className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProtocol() {
		return this.protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getTransformers() {
		return this.transformers;
	}

	public void setTransformers(String transformers) {
		this.transformers = transformers;
	}

	public Type getType() {
		return this.type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public boolean isInbound() {
		return this.inbound;
	}

	public void setInbound(boolean inbound) {
		this.inbound = inbound;
	}

	public boolean isOutbound() {
		return this.outbound;
	}

	public void setOutbound(boolean outbound) {
		this.outbound = outbound;
	}
	
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		
		if (!(that instanceof Transport)) {
			return false;
		}
		
		Transport transport = (Transport) that;
		
		return
			EqualsUtil.areEqual(this.getName(), transport.getName()) &&
			EqualsUtil.areEqual(this.getClassName(), transport.getClassName()) &&
			EqualsUtil.areEqual(this.getProtocol(), transport.getProtocol()) &&
			EqualsUtil.areEqual(this.getTransformers(), transport.getTransformers()) &&
			EqualsUtil.areEqual(this.getType(), transport.getType()) &&
			EqualsUtil.areEqual(this.isInbound(), transport.isInbound()) &&
			EqualsUtil.areEqual(this.isOutbound(), transport.isOutbound());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("name=" + getName() + ", ");
		builder.append("type=" + getType().toString() + ", ");
		builder.append("className=" + getClassName() + ", ");
		builder.append("transformers=" + getTransformers() + ", ");
		builder.append("protocol=" + getProtocol() + ", ");
		builder.append("inbound=" + isInbound() + ", ");
		builder.append("outbound=" + isOutbound());
		builder.append("]");
		return builder.toString();
	}

}
