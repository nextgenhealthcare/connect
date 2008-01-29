package com.webreach.mirth.server.util;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

public class TemplateEvaluator {
	private Logger logger = Logger.getLogger(this.getClass());

	public String evaluate(String template, Map<String, Object> values) {
		VelocityContext context = new VelocityContext();

		// load the context
		for (Iterator iter = values.entrySet().iterator(); iter.hasNext();) {
			Entry entry = (Entry) iter.next();
			context.put(entry.getKey().toString(), entry.getValue());
		}

		StringWriter writer = new StringWriter();

		try {
			Velocity.init();
			Velocity.evaluate(context, writer, "LOG", template);
		} catch (Exception e) {
			logger.warn("could not evaluate template values", e);
		}

		return writer.toString();
	}
}
