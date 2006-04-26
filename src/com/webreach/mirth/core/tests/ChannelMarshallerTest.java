package com.webreach.mirth.core.tests;

import java.io.StringWriter;

import com.sun.tools.xjc.generator.validator.StringOutputStream;
import com.webreach.mirth.core.Channel;
import com.webreach.mirth.core.Connector;
import com.webreach.mirth.core.Filter;
import com.webreach.mirth.core.Transformer;
import com.webreach.mirth.core.Validator;
import com.webreach.mirth.core.util.ChannelMarshaller;

import junit.framework.TestCase;

public class ChannelMarshallerTest extends TestCase {
	private Channel channel;

	protected void setUp() throws Exception {
		super.setUp();
		Transformer sourceTransformer = new Transformer();
		sourceTransformer.setType(Transformer.Type.MAP);
		sourceTransformer.getVariables().put("firstName", "TestFirstName");

		Transformer destinationTransformer = new Transformer();
		destinationTransformer.setType(Transformer.Type.SCRIPT);
		destinationTransformer.getVariables().put("lastName", "TestLastName");
		
		Connector sourceConnector = new Connector();
		sourceConnector.setName("sourceConnector");
		sourceConnector.getProperties().put("key", "value");
		sourceConnector.setTransformer(sourceTransformer);
		
		Connector destinationConnector1 = new Connector();
		destinationConnector1.setName("destinationConnector1");
		destinationConnector1.getProperties().put("key1", "value1");
		destinationConnector1.getProperties().put("key2", "value2");
		destinationConnector1.setTransformer(destinationTransformer);
		
		Connector destinationConnector2 = new Connector();
		destinationConnector2.setName("destinationConnector2");
		destinationConnector2.getProperties().put("key1", "value1");
		destinationConnector2.getProperties().put("key2", "value2");
		destinationConnector2.setTransformer(destinationTransformer);
		
		Filter filter = new Filter();
		filter.setScript("return true;");
		
		Validator validator = new Validator();
		validator.getProfiles().put("profile1", "<XLST>");
		
		channel = new Channel();
		channel.setName("Test Interface");
		channel.setDescription("This is a test interface.");
		channel.setEnabled(true);
		channel.setDirection(Channel.Direction.INBOUND);
		channel.setInitialStatus(Channel.Status.STOPPED);
		channel.setType(Channel.Type.ROUTER);
		channel.setModified(false);
		
		channel.setSourceConnector(sourceConnector);

		channel.setFilter(filter);
		channel.setValidator(validator);

		channel.getDestinationConnectors().add(destinationConnector1);
		channel.getDestinationConnectors().add(destinationConnector2);
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testMarshal() {
		try {
			ChannelMarshaller cm = new ChannelMarshaller();
			StringWriter stringWriter = new StringWriter();
			cm.marshal(channel, new StringOutputStream(stringWriter));
			System.out.println(stringWriter.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
