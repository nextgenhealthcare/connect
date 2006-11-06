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


package com.webreach.mirth.server.builders;

import java.util.ListIterator;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Filter;
import com.webreach.mirth.model.Rule;

public class JavaScriptFilterBuilder {
	private Logger logger = Logger.getLogger(this.getClass());

	public String getScript(Filter filter, Channel channel) throws BuilderException {
		logger.debug("building javascript filter: rule count=" + filter.getRules().size());
		StringBuilder builder = new StringBuilder();

		if (filter.getRules().isEmpty()) {
			logger.debug("filter is emtpy, setting to accept all messages");
			builder.append("return true;");
		} else {
			if (channel.getDirection().equals(Channel.Direction.OUTBOUND) && (filter.getTemplate() != null)) {
				builder.append("var template = new XML('" + filter.getTemplate() + "');");
			}
			
			// generate the functions
			for (ListIterator iter = filter.getRules().listIterator(); iter.hasNext();) {
				Rule rule = (Rule) iter.next();
				builder.append("function filterRule" + iter.nextIndex() + "() {" + rule.getScript() + "}\n");
			}

			builder.append("return (");

			// call each of the above functions in a big boolean expression
			for (ListIterator iter = filter.getRules().listIterator(); iter.hasNext();) {
				Rule rule = (Rule) iter.next();
				String operator = "";

				if (rule.getOperator().equals(Rule.Operator.AND)) {
					operator = " && ";
				} else if (rule.getOperator().equals(Rule.Operator.OR)) {
					operator = " || ";
				}

				builder.append(operator + "filterRule" + iter.nextIndex() + "()");
			}

			builder.append(");");
		}

		return builder.toString();
	}
}
