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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.util.EqualsUtil;

/**
 * A Transformer represents a script which is executed on each message passing
 * through the Connector with which the transformer is associated.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 * 
 */
public class Transformer implements Serializable {
	private List<Step> steps;
	private String inboundTemplate;
	private String outboundTemplate;
	private Protocol inboundProtocol;
	private Protocol outboundProtocol;
	private Properties inboundProperties;
	private Properties outboundProperties;
	
	public Transformer() {
		this.steps = new ArrayList<Step>();
	}

	public Protocol getInboundProtocol() {
		return this.inboundProtocol;
	}

	public void setInboundProtocol(Protocol inboundProtocol) {
		this.inboundProtocol = inboundProtocol;
	}

	public String getInboundTemplate() {
		return inboundTemplate;
	}

	public void setInboundTemplate(String inboundTemplate) {
		this.inboundTemplate = inboundTemplate;
	}

	public Protocol getOutboundProtocol() {
		return outboundProtocol;
	}

	public void setOutboundProtocol(Protocol outboundProtocol) {
		this.outboundProtocol = outboundProtocol;
	}

	public String getOutboundTemplate() {
		return outboundTemplate;
	}

	public void setOutboundTemplate(String outboundTemplate) {
		this.outboundTemplate = outboundTemplate;
	}

	public List<Step> getSteps() {
		return this.steps;
	}

	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}

	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}

		if (!(that instanceof Transformer)) {
			return false;
		}

		Transformer transformer = (Transformer) that;

		return
			EqualsUtil.areEqual(this.getSteps(), transformer.getSteps()) &&
			EqualsUtil.areEqual(this.getInboundTemplate(), transformer.getInboundTemplate()) &&
			EqualsUtil.areEqual(this.getOutboundTemplate(), transformer.getOutboundTemplate()) &&
			EqualsUtil.areEqual(this.getInboundProtocol(), transformer.getInboundProtocol()) &&
			EqualsUtil.areEqual(this.getOutboundProtocol(), transformer.getOutboundProtocol()) &&
			EqualsUtil.areEqual(this.getInboundProperties(), transformer.getInboundProperties()) &&
			EqualsUtil.areEqual(this.getOutboundProperties(), transformer.getOutboundProperties());
	}

	public Properties getInboundProperties()
	{
		return inboundProperties;
	}

	public void setInboundProperties(Properties inboundProperties)
	{
		this.inboundProperties = inboundProperties;
	}

	public Properties getOutboundProperties()
	{
		return outboundProperties;
	}

	public void setOutboundProperties(Properties outboundProperties)
	{
		this.outboundProperties = outboundProperties;
	}
}
