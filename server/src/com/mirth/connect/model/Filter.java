/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A Filter represents a list of rules which are executed on each message and
 * either accepts or rejects the message.
 * 
 */

@XStreamAlias("filter")
public class Filter implements Serializable, Migratable, Purgable {
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

    public void migrate3_0_1(DonkeyElement filter) {
        for (DonkeyElement rule : filter.getChildElement("rules").getChildElements()) {
            rule.getChildElement("data").removeAttribute("class");
        }
    }

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {}

    @Override
    public void migrate3_2_0(DonkeyElement element) {}

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("rules", PurgeUtil.purgeList(rules));
        return purgedProperties;
    }
}
