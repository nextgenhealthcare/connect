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

import com.webreach.mirth.util.EqualsUtil;

/**
 * A Filter represents a list of rules which are executed on each message and
 * either accepts or rejects the message.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 * 
 */
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

		return EqualsUtil.areEqual(this.getRules(), filter.getRules());
	}
}
