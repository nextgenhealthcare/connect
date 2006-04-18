package com.webreach.mirth.core.tests;

import com.webreach.mirth.core.Channel;
import com.webreach.mirth.core.Connector;
import com.webreach.mirth.core.Filter;
import com.webreach.mirth.core.Validator;

import junit.framework.TestCase;

public class ChannelTest extends TestCase {

	private Channel channel;
	
	protected void setUp() throws Exception {
		super.setUp();
		channel = new Channel();
		channel.setName("Test Interface");
		channel.setDescription("This is a test interface.");
		channel.setEnabled(true);
		channel.setDirection(Channel.Direction.INBOUND);
		channel.setType(Channel.Type.ROUTER);
		channel.setModified(false);
		channel.setFilter(new Filter());
		channel.setSourceConnector(new Connector());
		channel.getDestinationConnectors().add(new Connector());
		channel.setValidator(new Validator());
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testSerialize() {
		
	}
}
