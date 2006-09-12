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
