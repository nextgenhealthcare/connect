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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A Filter represents a list of rules which are executed on each message and
 * either accepts or rejects the message.
 * 
 */

@XStreamAlias("filter")
public class Filter implements Serializable {
	private List<Rule> rules;

	public Filter() {
		this.rules = new ArrayList<Rule>();
	}

	public List<Rule> getRules() {
		return this.rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}

		if (!(that instanceof Filter)) {
			return false;
		}

		Filter filter = (Filter) that;

		return ObjectUtils.equals(this.getRules(), filter.getRules());
	}
}
