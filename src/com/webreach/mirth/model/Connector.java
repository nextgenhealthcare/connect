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

import java.util.Properties;

/**
 * A Connector represents a connection to either a source or destination. Each
 * Connector has an associated Filter and Transformer. A connector is also of a
 * specific Transport type (TCP, HTTP, etc.).
 * 
 * @author geraldb
 * 
 */
public class Connector {
	private String name;
	private Properties properties;
	private Transformer transformer;
	private Filter filter;
	private String transportName;

	public Connector() {
		properties = new Properties();
	}

	public Connector(String name) {
		setName(name);
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

	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("Connector[");
		buffer.append("name=" + getName() + ", ");
		buffer.append("transportName=" + getTransportName() + ", ");
		buffer.append("properties=" + getProperties());
		buffer.append("]");
		return buffer.toString();
	}

}
