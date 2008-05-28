package com.webreach.mirth.server.builders;

import java.util.Iterator;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Filter;
import com.webreach.mirth.model.Rule;
import com.webreach.mirth.model.Step;
import com.webreach.mirth.model.Transformer;

public class JavaScriptBuilder {
	private Logger logger = Logger.getLogger(this.getClass());

	public String getScript(Channel channel, Filter filter, Transformer transformer) throws BuilderException {
		StringBuilder builder = new StringBuilder();

		if (filter.getRules().isEmpty() && transformer.getSteps().isEmpty()) {
			// do nothing
		} else {
			logger.debug("building javascript filter: rule count=" + filter.getRules().size());

			if (filter.getRules().isEmpty()) {
				logger.debug("filter is emtpy, setting to accept all messages");
				builder.append("function doFilter() { phase = 'filter'; return true; }");
			} else {
				// generate the functions
				for (ListIterator iter = filter.getRules().listIterator(); iter.hasNext();) {
					Rule rule = (Rule) iter.next();
					builder.append("function filterRule" + iter.nextIndex() + "() {\n" + rule.getScript() + "\n}");
				}

				builder.append("function doFilter() { phase = 'filter'; return (");

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

				builder.append("); }\n");
			}

			logger.debug("building javascript transformer: step count=" + transformer.getSteps().size());
			//Set the phase and also reset the logger to transformer (it was filter before)
			builder.append("function doTransform() { phase = 'transformer'; logger = Packages.org.apache.log4j.Logger.getLogger(phase);");

			for (Iterator iter = transformer.getSteps().iterator(); iter.hasNext();) {
				Step step = (Step) iter.next();
				logger.debug("adding step: " + step.getScript());
				builder.append(step.getScript() + "\n");
			}

			builder.append("}\n");
		}

		return builder.toString();
	}
}
