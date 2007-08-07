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
import java.util.Properties;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.webreach.mirth.util.EqualsUtil;

@XStreamAlias("connectorMetaData")
public class ConnectorMetaData extends MetaData implements Serializable {
	public enum Type {
		LISTENER, SENDER
	};

	private String serverClassName;
	private String sharedClassName;
	private String clientClassName;
	private String transformers;
	private String protocol;

	private Type type;
	@XStreamAlias("mule-properties")
	private Properties properties;

	public String getServerClassName() {
		return this.serverClassName;
	}

	public void setServerClassName(String serverClassName) {
		this.serverClassName = serverClassName;
	}

	public String getSharedClassName() {
		return sharedClassName;
	}

	public void setSharedClassName(String sharedClassName) {
		this.sharedClassName = sharedClassName;
	}

	public String getClientClassName() {
		return clientClassName;
	}

	public void setClientClassName(String clientClassName) {
		this.clientClassName = clientClassName;
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
	
	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}

		if (!(that instanceof ConnectorMetaData)) {
			return false;
		}

		ConnectorMetaData transport = (ConnectorMetaData) that;

		return
			EqualsUtil.areEqual(this.getName(), transport.getName()) &&
			EqualsUtil.areEqual(this.getAuthor(), transport.getAuthor()) &&
			EqualsUtil.areEqual(this.getServerClassName(), transport.getServerClassName()) &&
			EqualsUtil.areEqual(this.getSharedClassName(), transport.getSharedClassName()) &&
			EqualsUtil.areEqual(this.getClientClassName(), transport.getClientClassName()) &&
			EqualsUtil.areEqual(this.getProtocol(), transport.getProtocol()) &&
			EqualsUtil.areEqual(this.getPluginVersion(), transport.getPluginVersion()) &&
			EqualsUtil.areEqual(this.getMirthVersion(), transport.getMirthVersion()) &&
			EqualsUtil.areEqual(this.isEnabled(), transport.isEnabled()) &&
			EqualsUtil.areEqual(this.getTransformers(), transport.getTransformers()) &&
			EqualsUtil.areEqual(this.getType(), transport.getType()) &&
			EqualsUtil.areEqual(this.getDescription(), transport.getDescription()) &&
			EqualsUtil.areEqual(this.getProperties(), transport.getProperties()) &&
			EqualsUtil.areEqual(this.getUpdateUrl(), transport.getUpdateUrl());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("name=" + getName() + ", ");
		builder.append("author=" + getAuthor() + ", ");
		builder.append("type=" + getType().toString() + ", ");
		builder.append("serverClassName=" + getServerClassName() + ", ");
		builder.append("sharedClassName=" + getSharedClassName() + ", ");
		builder.append("clientClassName=" + getClientClassName() + ", ");
		builder.append("transformers=" + getTransformers() + ", ");
		builder.append("protocol=" + getProtocol() + ", ");
		builder.append("enabled=" + isEnabled() + ", ");
		builder.append("pluginVersion=" + getPluginVersion() + ", ");
		builder.append("mirthVersion=" + getMirthVersion() + ", ");
		builder.append("updateUrl=" + getUpdateUrl() + ", ");
		builder.append("description=" + getDescription());
		builder.append("]");
		return builder.toString();
	}
}
