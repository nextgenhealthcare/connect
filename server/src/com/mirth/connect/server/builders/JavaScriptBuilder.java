/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.builders;

import java.io.File;
import java.io.IOException;
import java.util.ListIterator;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Filter;
import com.mirth.connect.model.Rule;
import com.mirth.connect.model.Step;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.server.util.FileUtil;

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
				for (ListIterator<Rule> iter = filter.getRules().listIterator(); iter.hasNext();) {
					Rule rule = iter.next();

					if (rule.getType().equalsIgnoreCase("External Script")) {
						try {
						    File externalScriptFile = new File(rule.getScript());
							builder.append("function filterRule" + iter.nextIndex() + "() {\n" + FileUtils.readFileToString(externalScriptFile) + "\n}");
						} catch (IOException e) {
							throw new BuilderException("Could not add script file.", e);
						}
					} else {
						builder.append("function filterRule" + iter.nextIndex() + "() {\n" + rule.getScript() + "\n}");
					}
				}

				builder.append("function doFilter() { phase = 'filter'; return (");

				// call each of the above functions in a big boolean expression
				for (ListIterator<Rule> iter = filter.getRules().listIterator(); iter.hasNext();) {
					Rule rule = iter.next();
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
			builder.append("function doTransform() { phase = 'transformer'; logger = Packages.org.apache.log4j.Logger.getLogger(phase);\n\n\n");

			for (Step step : transformer.getSteps()) {
				logger.debug("adding step: " + step.getName());
				
				if (step.getType().equalsIgnoreCase("External Script")) {
				    try {
	                    builder.append("\n" + FileUtils.readFileToString(new File(step.getScript())) + "\n");
				    } catch (IOException e) {
				        throw new BuilderException("Could not add script file.", e);
				    }
				} else {
				    builder.append(step.getScript() + "\n");    
				}
			}

			builder.append("\n}\n");
		}

		return builder.toString();
	}
}
