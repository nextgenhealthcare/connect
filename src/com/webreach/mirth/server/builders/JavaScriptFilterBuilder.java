package com.webreach.mirth.server.builders;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Rule;
import com.webreach.mirth.model.Filter;

public class JavaScriptFilterBuilder {
	private Logger logger = Logger.getLogger(this.getClass());
	
	public String getScript(Filter filter) throws BuilderException {
		logger.debug("building javascript filter: rules=" + filter.getRules().size());
		
		StringBuilder builder = new StringBuilder();
		builder.append("return ");

		if (filter.getRules().isEmpty()) {
			logger.debug("filter is emtpy, setting to accept all messages");
			builder.append("true");
		} else {
			builder.append("(");
			
			for (Iterator iter = filter.getRules().iterator(); iter.hasNext();) {
				Rule rule = (Rule) iter.next();
				logger.debug("adding rule: " + rule.getScript());
				builder.append(rule.getScript() + "\n\t");
			}
			
			builder.append(")");
		}
		
		builder.append(";");
		return builder.toString();
	}
}
