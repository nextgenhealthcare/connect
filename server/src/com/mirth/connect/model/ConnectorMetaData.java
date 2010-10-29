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
import java.util.Properties;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("connectorMetaData")
public class ConnectorMetaData extends MetaData implements Serializable {
	public enum Type {
		SOURCE, DESTINATION
	}

	private String serverClassName;
	private String sharedClassName;
	private String clientClassName;
	private String serviceClassName;
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

	public String getServiceClassName() {
		return serviceClassName;
	}

	public void setServiceClassName(String serviceClassName) {
		this.serviceClassName = serviceClassName;
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
			ObjectUtils.equals(this.getName(), transport.getName()) &&
			ObjectUtils.equals(this.getAuthor(), transport.getAuthor()) &&
			ObjectUtils.equals(this.getServerClassName(), transport.getServerClassName()) &&
			ObjectUtils.equals(this.getSharedClassName(), transport.getSharedClassName()) &&
			ObjectUtils.equals(this.getClientClassName(), transport.getClientClassName()) &&
			ObjectUtils.equals(this.getServiceClassName(), transport.getServiceClassName()) &&
			ObjectUtils.equals(this.getProtocol(), transport.getProtocol()) &&
			ObjectUtils.equals(this.getPluginVersion(), transport.getPluginVersion()) &&
			ObjectUtils.equals(this.getMirthVersion(), transport.getMirthVersion()) &&
			ObjectUtils.equals(this.isEnabled(), transport.isEnabled()) &&
			ObjectUtils.equals(this.getTransformers(), transport.getTransformers()) &&
			ObjectUtils.equals(this.getType(), transport.getType()) &&
			ObjectUtils.equals(this.getDescription(), transport.getDescription()) &&
			ObjectUtils.equals(this.getProperties(), transport.getProperties());
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
		builder.append("serviceClassName=" + getServiceClassName() + ", ");
		builder.append("transformers=" + getTransformers() + ", ");
		builder.append("protocol=" + getProtocol() + ", ");
		builder.append("enabled=" + isEnabled() + ", ");
		builder.append("pluginVersion=" + getPluginVersion() + ", ");
		builder.append("mirthVersion=" + getMirthVersion() + ", ");
		builder.append("description=" + getDescription());
		builder.append("]");
		return builder.toString();
	}
}
