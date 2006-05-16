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

import java.util.HashMap;
import java.util.Map;

/**
 * A Connector represents a connection to either a source or destination. Each
 * Connector has an associated Transformer which performs the appropriate
 * transformation. A connector is also of a specific Transport type (TCP, HTTP,
 * etc.).
 * 
 * @author geraldb
 * 
 */
public class Connector {
	private String name;
	private Map<String, String> properties;
	private Transformer transformer;
	private String transport;

	public Connector() {
		properties = new HashMap<String, String>();
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

	public String getTransport() {
		return this.transport;
	}

	public void setTransport(String transport) {
		this.transport = transport;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

}
