package com.webreach.mirth.server.builders;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Step;
import com.webreach.mirth.model.Transformer;
import com.webreach.mirth.model.converters.ER7Serializer;

public class JavaScriptTransformerBuilder {
	private Logger logger = Logger.getLogger(this.getClass());
	
	public String getScript(Transformer transformer, Channel channel) throws BuilderException {
		logger.debug("building outbound javascript transformer: steps=" + transformer.getSteps().size());
		
		ER7Serializer serializer = new ER7Serializer();
		StringBuilder builder = new StringBuilder();
		
		if (channel.getDirection().equals(Channel.Direction.OUTBOUND)) {
			builder.append("var hl7_xml = new XML('" + serializer.toXML(transformer.getTemplate()).replaceAll("\\n","") + "');");
			builder.append("var hl7_er7 = '" + transformer.getTemplate().replaceAll("\\r","\\\\r") + "';");
		} else {
			builder.append("var hl7_xml = new XML(message);");
			builder.append("var hl7_er7 = incomingMessage;");
		}

		for (Iterator iter = transformer.getSteps().iterator(); iter.hasNext();) {
			Step step = (Step) iter.next();
			logger.debug("adding step: " + step.getScript());
			builder.append(step.getScript() + "");
		}
		
		builder.append("channelid = '" + channel.getId() + "';");
		builder.append("localMap.put(\"HL7 ER7\", hl7_er7);"); 
		builder.append("localMap.put(\"HL7 XML\", hl7_xml);");
		
		return builder.toString();
	}
}
