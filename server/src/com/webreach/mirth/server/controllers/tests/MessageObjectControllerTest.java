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


package com.webreach.mirth.server.controllers.tests;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.tools.ScriptRunner;

public class MessageObjectControllerTest extends TestCase {
	private MessageObjectController messageObjectController = new MessageObjectController();
	private ConfigurationController configurationController = new ConfigurationController();
	private ChannelController channelController = new ChannelController();
	private List<MessageObject> sampleMessageObjectList;
	private String channelId;
	
	protected void setUp() throws Exception {
		super.setUp();
		// clear all database tables
		ScriptRunner.runScript("derby-database.sql");

		// initialize the configuration controller to cache encryption key
		configurationController.initialize();

		sampleMessageObjectList = new ArrayList<MessageObject>();
		channelId = UUID.randomUUID().toString();
		
		Channel sampleChannel = new Channel();
		sampleChannel.setId(channelId);
		sampleChannel.setName("Sample Channel");
		sampleChannel.setDescription("This is a sample channel");
		sampleChannel.setEnabled(true);
		sampleChannel.setVersion(configurationController.getVersion());
		sampleChannel.setRevision(0);
		sampleChannel.setSourceConnector(new Connector());
		sampleChannel.setPreprocessingScript("return 1;");
		
		Properties sampleProperties = new Properties();
		sampleProperties.setProperty("testProperty", "true");
		sampleChannel.setProperties(sampleProperties);
		
		channelController.updateChannel(sampleChannel, true);
		
		for (int i = 0; i < 10; i++) {
			MessageObject sampleMessageObject = new MessageObject();
			sampleMessageObject.setId(UUID.randomUUID().toString());
			sampleMessageObject.setChannelId(channelId);
			
			sampleMessageObject.setSource("SendingFacility" + i);
			sampleMessageObject.setType("ADT-A0" + i);
			
			sampleMessageObject.setConnectorName("SampleConnector");
			sampleMessageObject.setDateCreated(Calendar.getInstance());
			sampleMessageObject.setVersion("2.3.1");
			sampleMessageObject.setEncrypted(false);
			sampleMessageObject.setStatus(MessageObject.Status.TRANSFORMED);

			sampleMessageObject.setEncodedData("sample encoded data");
			sampleMessageObject.setEncodedDataProtocol(MessageObject.Protocol.XML);

			sampleMessageObject.setRawData("<raw_data></raw_data>");
			sampleMessageObject.setRawDataProtocol(MessageObject.Protocol.XML);

			sampleMessageObject.setTransformedData("sample transformed data");
			sampleMessageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);

			Map<String, String> sampleVariableMap = new HashMap<String, String>();
			sampleVariableMap.put("testVariable", "testValue");

			sampleMessageObject.setConnectorMap(sampleVariableMap);
			sampleMessageObject.setErrors("invalid header");
			
			sampleMessageObjectList.add(sampleMessageObject);
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testUpdateMessage() throws ControllerException {
		MessageObject sampleMessageObject = sampleMessageObjectList.get(0);
		messageObjectController.updateMessage(sampleMessageObject);
		
		MessageObjectFilter testFilter = new MessageObjectFilter();
		testFilter.setId(sampleMessageObject.getId());
		messageObjectController.createMessagesTempTable(testFilter, "test");
		List<MessageObject> testMessageObjectList = messageObjectController.getMessagesByPage(-1, -1, 0, "test");
		MessageObject testMessageObject = testMessageObjectList.get(0);

		Assert.assertEquals(1, testMessageObjectList.size());
		Assert.assertEquals(sampleMessageObject, testMessageObject);
	}

	public void testGetMessages() throws ControllerException {
		insertSampleMessages();
		
		MessageObjectFilter testFilter = new MessageObjectFilter();
		testFilter.setChannelId(channelId);
		testFilter.setStartDate(Calendar.getInstance());
		testFilter.setEndDate(Calendar.getInstance());
		
		messageObjectController.createMessagesTempTable(testFilter, "test");
		List<MessageObject> testMessageObjectList = messageObjectController.getMessagesByPage(-1, -1, 0, "test");

		for (Iterator iter = sampleMessageObjectList.iterator(); iter.hasNext();) {
			MessageObject sampleMessageObject = (MessageObject) iter.next();
			Assert.assertTrue(testMessageObjectList.contains(sampleMessageObject));
		}
	}

	public void testGetMessagesByFilter() throws ControllerException {
		insertSampleMessages();

		List<MessageObject> testMessageObjectList = null;
		MessageObjectFilter testFilter = null;
		
		testFilter = new MessageObjectFilter();
		testFilter.setChannelId(channelId);
		testFilter.setStartDate(Calendar.getInstance());
		testFilter.setEndDate(Calendar.getInstance());
		messageObjectController.createMessagesTempTable(testFilter, "test");
		testMessageObjectList = messageObjectController.getMessagesByPage(-1, -1, 0, "test");
		Assert.assertEquals(10, testMessageObjectList.size());

		testFilter = new MessageObjectFilter();
		testFilter.setType("ADT-A01");
		testFilter.setSource("SendingFacility1");
		messageObjectController.createMessagesTempTable(testFilter, "test");
		testMessageObjectList = messageObjectController.getMessagesByPage(-1, -1, 0, "test");
		Assert.assertEquals(1, testMessageObjectList.size());
	}

	public void testGetMessageCount() throws ControllerException {
		insertSampleMessages();
		
		MessageObjectFilter testFilter = new MessageObjectFilter();
		testFilter.setChannelId(channelId);
		messageObjectController.createMessagesTempTable(testFilter, "test");
		List<MessageObject> testMessageObjectList = messageObjectController.getMessagesByPage(-1, -1, 0, "test");
		Assert.assertEquals(testMessageObjectList.size(), messageObjectController.createMessagesTempTable(testFilter, "test"));
	}

	public void testRemoveMessages() throws ControllerException {
		insertSampleMessages();

		MessageObject sampleMessageObject = sampleMessageObjectList.get(0);
		MessageObjectFilter testRemoveFilter = new MessageObjectFilter();
		testRemoveFilter.setId(sampleMessageObject.getId());
		testRemoveFilter.setStatus(MessageObject.Status.TRANSFORMED);
		messageObjectController.removeMessages(testRemoveFilter);

		MessageObjectFilter testFilter = new MessageObjectFilter();
		testFilter.setChannelId(channelId);
		messageObjectController.createMessagesTempTable(testFilter, "test");
		List<MessageObject> testMessageObjectList = messageObjectController.getMessagesByPage(-1, -1, 0, "test");

		Assert.assertEquals(sampleMessageObjectList.size() - 1, testMessageObjectList.size());
	}

	public void testClearMessages() throws ControllerException {
		insertSampleMessages();
		messageObjectController.clearMessages(channelId);

		MessageObjectFilter testFilter = new MessageObjectFilter();
		testFilter.setChannelId(channelId);
		messageObjectController.createMessagesTempTable(testFilter, "test");
		List<MessageObject> testMessageObjectList = messageObjectController.getMessagesByPage(-1, -1, 0, "test");

		Assert.assertEquals(0, testMessageObjectList.size());
	}
	
	private void insertSampleMessages() {
		for (Iterator iter = sampleMessageObjectList.iterator(); iter.hasNext();) {
			MessageObject messageObject = (MessageObject) iter.next();
			messageObjectController.updateMessage(messageObject);
		}
	}
}
