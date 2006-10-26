package com.webreach.mirth.server.builders;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Step;
import com.webreach.mirth.model.Transformer;
import com.webreach.mirth.model.converters.ER7Serializer;
import com.webreach.mirth.model.converters.SerializerException;

public class JavaScriptTransformerBuilder {
	private Logger logger = Logger.getLogger(this.getClass());

	public String getScript(Transformer transformer, Channel channel) throws BuilderException {
		logger.debug("building outbound javascript transformer: step count=" + transformer.getSteps().size());
		StringBuilder builder = new StringBuilder();

		// if the channel is outbound, add the template to the transformer
		if (channel.getDirection().equals(Channel.Direction.OUTBOUND) && (transformer.getTemplate() != null) && (!transformer.getTemplate().equals(""))) {
			ER7Serializer serializer = new ER7Serializer();
			String template = new String();
			try {
				template = serializer.toXML(transformer.getTemplate());
			} catch (SerializerException e) {
				throw new BuilderException(e);
			}
			builder.append("var template = new XML('" + template.replaceAll("&amp;", "&").replaceAll("\\n","") + "');");
		}

		for (Iterator iter = transformer.getSteps().iterator(); iter.hasNext();) {
			Step step = (Step) iter.next();
			logger.debug("adding step: " + step.getScript());
			builder.append(step.getScript());
		}

		return builder.toString();
	}
}
