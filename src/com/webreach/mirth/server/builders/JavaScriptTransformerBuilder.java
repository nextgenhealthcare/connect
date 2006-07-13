package com.webreach.mirth.server.builders;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Step;
import com.webreach.mirth.model.Transformer;
import com.webreach.mirth.model.converters.ER7Serializer;

public class JavaScriptTransformerBuilder {
	private Logger logger = Logger.getLogger(this.getClass());
	
	public String getInboundScript(Transformer transformer) throws BuilderException {
		logger.debug("building inbound javascript transformer: steps=" + transformer.getSteps().size());
		
		ER7Serializer serializer = new ER7Serializer();
		StringBuilder builder = new StringBuilder();
		
		builder.append("var hl7 = new XML(" + serializer.serialize(transformer.getTemplate()) + ");\n");

		for (Iterator iter = transformer.getSteps().iterator(); iter.hasNext();) {
			Step step = (Step) iter.next();
			logger.debug("adding step: " + step.getScript());
			builder.append(step.getScript() + "\n");
		}
		
		builder.append("localMap.put(\"hl7\", hl7);"); 

		return builder.toString();
	}

	public String getOutboundScript(Transformer transformer) throws BuilderException {
		logger.debug("building outbound javascript transformer: steps=" + transformer.getSteps().size());
		
		StringBuilder builder = new StringBuilder();
		
		builder.append("var hl7 = new XML(message);\n");

		for (Iterator iter = transformer.getSteps().iterator(); iter.hasNext();) {
			Step step = (Step) iter.next();
			logger.debug("adding step: " + step.getScript());
			builder.append(step.getScript() + "\n");
		}
		
		builder.append("localMap.put(\"hl7\", hl7);"); 

		return builder.toString();
	}
}
