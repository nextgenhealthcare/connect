/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

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
