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
import java.util.ArrayList;
import java.util.List;

public class WSOperation  implements Serializable {
	private String name;
	private List<WSParameter> parameters;
	private String soapActionURI;
	private String namespace;
	private WSParameter header;
	private String headerNamespace;
	public WSOperation() {
		this.parameters = new ArrayList<WSParameter>();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<WSParameter> getParameters() {
		return this.parameters;
	}

	public void setParameters(List<WSParameter> parameters) {
		this.parameters = parameters;
	}

	public String getSoapActionURI() {
		return soapActionURI;
	}

	public void setSoapActionURI(String soapActionURI) {
		this.soapActionURI = soapActionURI;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public WSParameter getHeader() {
		return header;
	}

	public void setHeader(WSParameter headerParameters) {
		this.header = headerParameters;
	}

	public String getHeaderNamespace() {
		return headerNamespace;
	}

	public void setHeaderNamespace(String headerNamespace) {
		this.headerNamespace = headerNamespace;
	}
}
