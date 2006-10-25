package com.webreach.mirth.server.builders;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Step;
import com.webreach.mirth.model.Transformer;

public class JavaScriptTransformerBuilder {
	private Logger logger = Logger.getLogger(this.getClass());
	
	public String getScript(Transformer transformer, Channel channel) throws BuilderException {
		logger.debug("building outbound javascript transformer: step count=" + transformer.getSteps().size());
		StringBuilder builder = new StringBuilder();

		// if the channel is outbound, add the template to the transformer
		if (channel.getDirection().equals(Channel.Direction.OUTBOUND) && (transformer.getTemplate() != null) && (transformer.getTemplate().length() > 0)) {
			builder.append("var template = new XML('" + transformer.getTemplate() + "');");
		}else if (channel.getDirection().equals(Channel.Direction.OUTBOUND) && ((transformer.getTemplate() == null) || (transformer.getTemplate().length() == 0))){
			builder.append("var template = '';");
		}

		for (Iterator iter = transformer.getSteps().iterator(); iter.hasNext();) {
			Step step = (Step) iter.next();
			logger.debug("adding step: " + step.getScript());
			builder.append(step.getScript());
		}
		
		return builder.toString();
	}
}
