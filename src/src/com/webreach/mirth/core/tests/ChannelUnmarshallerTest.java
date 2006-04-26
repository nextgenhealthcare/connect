package com.webreach.mirth.core.tests;

import com.webreach.mirth.core.Channel;
import com.webreach.mirth.core.util.ChannelUnmarshaller;

import junit.framework.TestCase;

public class ChannelUnmarshallerTest extends TestCase {
	private String source;
	private ChannelUnmarshaller cu = null;
	
	protected void setUp() throws Exception {
		super.setUp();
		cu = new ChannelUnmarshaller();
		
		StringBuffer sourceBuffer = new StringBuffer();
		sourceBuffer.append("<channel description=\"This is a test interface.\" direction=\"INBOUND\" enabled=\"true\" initial=\"STOPPED\" name=\"Test Interface\"> <source name=\"sourceConnector\" transport=\"\"> <property name=\"key\" value=\"value\"/> <transformer type=\"MAP\"> <variable name=\"firstName\"><![CDATA[TestFirstName]]></variable> </transformer> </source> <filter><![CDATA[return true;]]></filter> <validator> <profile name=\"profile1\"><![CDATA[<XLST>]]></profile> </validator> <destinations> <destination name=\"destinationConnector1\" transport=\"\"> <property name=\"key1\" value=\"value1\"/> <property name=\"key2\" value=\"value2\"/> <transformer type=\"SCRIPT\"> <variable name=\"lastName\"><![CDATA[TestLastName]]></variable> </transformer> </destination> <destination name=\"destinationConnector2\" transport=\"\"> <property name=\"key1\" value=\"value1\"/> <property name=\"key2\" value=\"value2\"/> <transformer type=\"SCRIPT\"> <variable name=\"lastName\"><![CDATA[TestLastName]]></variable> </transformer> </destination> </destinations></channel>");
		source = sourceBuffer.toString();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testUnmarshal() {
		Channel channel = null;
		
		try {
			channel = cu.unmarshal(source);	
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals("Test Interface", channel.getName());
		assertEquals("This is a test interface.", channel.getDescription());
		assertEquals(Channel.Direction.INBOUND, channel.getDirection());
		assertEquals(Channel.Status.STOPPED, channel.getInitialStatus());
		assertEquals(true, channel.isEnabled());
		assertEquals("sourceConnector", channel.getSourceConnector().getName());
		assertEquals(1, channel.getSourceConnector().getProperties().size());
	}
}
