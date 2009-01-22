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


package com.webreach.mirth.model.ws;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.wsif.schema.SchemaType;

public class WSDefinition implements Serializable {
	private Map<String, WSOperation> operations;
	private Map<String, SchemaType> complexTypes;
	private String serviceEndpointURI;

	public WSDefinition() {
		this.operations = new HashMap<String, WSOperation>();
	}

	public WSOperation getOperation(String name) {
		return getOperations().get(name);
	}

	public Map<String, WSOperation> getOperations() {
		return this.operations;
	}

	public Map<String, SchemaType> getComplexTypes() {
		return complexTypes;
	}

	public void setComplexTypes(Map<String, SchemaType> complexTypes) {
		this.complexTypes = complexTypes;
	}

	public String getServiceEndpointURI() {
		return serviceEndpointURI;
	}

	public void setServiceEndpointURI(String serviceEndpointURI) {
		this.serviceEndpointURI = serviceEndpointURI;
	}

}
