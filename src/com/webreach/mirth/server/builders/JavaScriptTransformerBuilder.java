package com.webreach.mirth.server.builders;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Step;
import com.webreach.mirth.model.Transformer;
import com.webreach.mirth.model.converters.ER7Serializer;

public class JavaScriptTransformerBuilder {
	private static final String LOCALMAP_PUT_HL7_ER7 = "localMap.put(\"HL7 ER7\", hl7_er7);";
	private static final String LOCALMAP_PUT_HL7_XML = "localMap.put(\"HL7 XML\", hl7_xml);";
	private Logger logger = Logger.getLogger(this.getClass());
	
	public String getOutboundScript(Transformer transformer, int channelId) throws BuilderException {
		logger.debug("building outbound javascript transformer: steps=" + transformer.getSteps().size());
		
		ER7Serializer serializer = new ER7Serializer();
		StringBuilder builder = new StringBuilder();
		
		builder.append("var hl7_xml = new XML('" + serializer.serialize(transformer.getTemplate()).replaceAll("\\n","") + "');");
		builder.append("var hl7_er7 = '" + transformer.getTemplate().replaceAll("\\r","\\\\r") + "';");
		for (Iterator iter = transformer.getSteps().iterator(); iter.hasNext();) {
			Step step = (Step) iter.next();
			logger.debug("adding step: " + step.getScript());
			builder.append(step.getScript() + "");
		}
		builder.append("channelid = '" + channelId + "';");
		builder.append(LOCALMAP_PUT_HL7_XML); 
		builder.append(LOCALMAP_PUT_HL7_ER7); 
		return builder.toString();
	}

	public String getInboundScript(Transformer transformer, int channelId) throws BuilderException {
		logger.debug("building inbound javascript transformer: steps=" + transformer.getSteps().size());
		
		StringBuilder builder = new StringBuilder();
		builder.append("var hl7_xml = new XML(message);");
		builder.append("var hl7_er7 = incomingMessage;");
		for (Iterator iter = transformer.getSteps().iterator(); iter.hasNext();) {
			Step step = (Step) iter.next();
			logger.debug("adding step: " + step.getScript());
			builder.append(step.getScript() + "");
		}
		builder.append("channelid = '" + channelId + "';");
		builder.append(LOCALMAP_PUT_HL7_ER7); 
		builder.append(LOCALMAP_PUT_HL7_XML); 
		return builder.toString();
	}
}
