package com.webreach.mirth.server.builders;

import java.util.Iterator;

import com.webreach.mirth.model.Rule;
import com.webreach.mirth.model.Filter;

public class JavaScriptFilterBuilder {
	public String getScript(Filter filter) throws BuilderException {
		StringBuilder builder = new StringBuilder();
		builder.append("return (");

		for (Iterator iter = filter.getRules().iterator(); iter.hasNext();) {
			Rule constraint = (Rule) iter.next();
			builder.append(constraint.getScript() + " ");
		}

		builder.append(");");
		return builder.toString();
	}
}
