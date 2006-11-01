package com.webreach.mirth.client.ui.util;

import java.util.Iterator;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.Step;

public class ImportUtil {
	public static Channel convert(Channel channel) {
		if (channel.getDirection().equals(Channel.Direction.OUTBOUND)) {
			for (Iterator iter = channel.getDestinationConnectors().iterator(); iter.hasNext();) {
				Connector connector = (Connector) iter.next();
				
				for (Iterator iterator = connector.getTransformer().getSteps().iterator(); iterator.hasNext();) {
					Step step = (Step) iterator.next();
					String script = step.getScript();
					script.replaceAll("hl7_xml", "tmp");
					script.replaceAll("text()[0]", "toString()");
					step.setScript(script);
				}
			}
		}
		
		for (Iterator iter = channel.getDestinationConnectors().iterator(); iter.hasNext();) {
			Connector connector = (Connector) iter.next();

			if (connector.getProperties().getProperty("template") != null) {
				String template = connector.getProperties().getProperty("template");
				template.replaceAll("hl7_xml", "message.transformedData");
				template.replaceAll("${DATE}", "${date}");
				
				if (channel.getDirection().equals(Channel.Direction.INBOUND)) {
					template.replaceAll("hl7_er7", "message.rawData");
				} else {
					template.replaceAll("hl7_er7", "message.encodedData");
				}
				
				connector.getProperties().setProperty("template", template);
			}
		}
		
		return channel;
	}
}
