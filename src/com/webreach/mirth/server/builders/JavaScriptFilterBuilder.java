package com.webreach.mirth.server.builders;

import java.util.Iterator;

import com.webreach.mirth.model.Rule;
import com.webreach.mirth.model.Filter;

public class JavaScriptFilterBuilder {
	public String getScript(Filter filter) throws BuilderException {
		StringBuilder builder = new StringBuilder();
		builder.append("return ");

		if (filter.getRules().isEmpty()) {
			builder.append("true");
		} else {
			builder.append("(");
			
			for (Iterator iter = filter.getRules().iterator(); iter.hasNext();) {
				Rule rule = (Rule) iter.next();
				builder.append(rule.getScript() + " ");
			}
			
			builder.append(")");
		}
		
		builder.append(";");
		return builder.toString();
	}
}
