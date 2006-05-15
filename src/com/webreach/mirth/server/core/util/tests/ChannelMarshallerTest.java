/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.server.core.util.tests;

import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.Filter;
import com.webreach.mirth.model.Transformer;
import com.webreach.mirth.model.Validator;
import com.webreach.mirth.model.bind.ChannelMarshaller;

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
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			cm.marshal(channel, outputStream);
			System.out.println(outputStream.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
