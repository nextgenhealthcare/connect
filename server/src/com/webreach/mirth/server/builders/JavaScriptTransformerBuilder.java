package com.webreach.mirth.server.builders;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Step;
import com.webreach.mirth.model.Transformer;

public class JavaScriptTransformerBuilder {
	private Logger logger = Logger.getLogger(this.getClass());

	public String getScript(Transformer transformer, Channel channel) throws BuilderException {
		logger.debug("building javascript transformer: step count=" + transformer.getSteps().size());
		StringBuilder builder = new StringBuilder();

		for (Iterator iter = transformer.getSteps().iterator(); iter.hasNext();) {
			Step step = (Step) iter.next();
			logger.debug("adding step: " + step.getScript());
			builder.append(step.getScript());
		}

		return builder.toString();
	}
}
