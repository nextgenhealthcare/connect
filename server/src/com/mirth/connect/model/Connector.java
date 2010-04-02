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

import com.mirth.connect.util.EqualsUtil;

/**
 * A Connector represents a connection to either a source or destination. Each
 * Connector has an associated Filter and Transformer. A connector is also of a
 * specific Transport type (TCP, HTTP, etc.).
 * 
 * 
 */
public class Connector implements Serializable {
	public enum Mode {
		SOURCE, DESTINATION
	}
	
	private String name;
	private Properties properties;
	private Transformer transformer;
	private Filter filter;
	private String transportName;
	private Mode mode;
	private boolean enabled;
	private String version;

	public Connector() {
		this.properties = new Properties();
	}

	public Connector(String name) {
		this.properties = new Properties();
		this.name = name;
	}
	
	public Mode getMode() {
		return this.mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Transformer getTransformer() {
		return this.transformer;
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}

	public Filter getFilter() {
		return this.filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public String getTransportName() {
		return this.transportName;
	}

	public void setTransportName(String transportName) {
		this.transportName = transportName;
	}

	public Properties getProperties() {
		return this.properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		
		if (!(that instanceof Connector)) {
			return false;
		}
		
		Connector connector = (Connector) that;
		
		return
			EqualsUtil.areEqual(this.getName(), connector.getName()) &&
			EqualsUtil.areEqual(this.getProperties(), connector.getProperties()) &&
			EqualsUtil.areEqual(this.getTransformer(), connector.getTransformer()) &&
			EqualsUtil.areEqual(this.getFilter(), connector.getFilter()) &&
			EqualsUtil.areEqual(this.isEnabled(), connector.isEnabled()) &&
			EqualsUtil.areEqual(this.getTransportName(), connector.getTransportName());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("name=" + getName() + ", ");
		builder.append("transportName=" + getTransportName() + ", ");
		builder.append("properties=" + getProperties());
		builder.append("enabled=" + isEnabled());
		builder.append("]");
		return builder.toString();
	}

}
