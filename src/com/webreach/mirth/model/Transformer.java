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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A Transformer represents a script which is executed on each message passing
 * through the Connector with which the transformer is associated.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 * 
 */
public class Transformer implements Script {
	private List<Step> steps;

	public Transformer() {
		this.steps = new ArrayList<Step>();
	}

	public List<Step> getSteps() {
		return this.steps;
	}

	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}

	public String getScript() {
		StringBuilder builder = new StringBuilder();

		for (Iterator iter = steps.iterator(); iter.hasNext();) {
			Step step = (Step) iter.next();
			builder.append(step.getScript() + "\n");
		}

		return builder.toString();
	}
}
