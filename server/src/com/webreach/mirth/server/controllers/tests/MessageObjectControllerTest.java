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
import java.util.UUID;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.tools.ScriptRunner;

public class MessageObjectControllerTest extends TestCase {
	private MessageObjectController messageObjectController = new MessageObjectController();
	private ConfigurationController configurationController = new ConfigurationController();
	private List<MessageObject> sampleMessageObjectList;
	private String channelId;
	
	protected void setUp() throws Exception {
		super.setUp();
		// clear all database tables
		ScriptRunner.runScript("database.sql");

		// initialize the configuration controller to cache encryption key
		configurationController.initialize();

		sampleMessageObjectList = new ArrayList<MessageObject>();
		channelId = UUID.randomUUID().toString();
		
		for (int i = 0; i < 10; i++) {
			MessageObject sampleMessageObject = new MessageObject();
			sampleMessageObject.setId(UUID.randomUUID().toString());
			sampleMessageObject.setChannelId(channelId);
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

			sampleMessageObject.setVariableMap(sampleVariableMap);
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
		List<MessageObject> testMessageObjectList = messageObjectController.getMessages(testFilter);
		MessageObject testMessageObject = testMessageObjectList.get(0);

		Assert.assertEquals(1, testMessageObjectList.size());
		Assert.assertEquals(sampleMessageObject, testMessageObject);
	}

	public void testGetMessages() throws ControllerException {
		insertSampleMessages();
		
		MessageObjectFilter testFilter = new MessageObjectFilter();
		testFilter.setChannelId(channelId);
		List<MessageObject> testMessageObjectList = messageObjectController.getMessages(testFilter);

		for (Iterator iter = sampleMessageObjectList.iterator(); iter.hasNext();) {
			MessageObject sampleMessageObject = (MessageObject) iter.next();
			Assert.assertTrue(testMessageObjectList.contains(sampleMessageObject));
		}
	}

	public void testGetMessageCount() throws ControllerException {
		insertSampleMessages();
		
		MessageObjectFilter testFilter = new MessageObjectFilter();
		testFilter.setChannelId(channelId);
		List<MessageObject> testMessageObjectList = messageObjectController.getMessages(testFilter);
		
		Assert.assertEquals(testMessageObjectList.size(), messageObjectController.getMessageCount(testFilter));
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
		List<MessageObject> testMessageObjectList = messageObjectController.getMessages(testFilter);

		Assert.assertEquals(sampleMessageObjectList.size() - 1, testMessageObjectList.size());
	}

	public void testClearMessages() throws ControllerException {
		insertSampleMessages();
		messageObjectController.clearMessages(channelId);

		MessageObjectFilter testFilter = new MessageObjectFilter();
		testFilter.setChannelId(channelId);
		List<MessageObject> testMessageObjectList = messageObjectController.getMessages(testFilter);

		Assert.assertEquals(0, testMessageObjectList.size());
	}
	
	private void insertSampleMessages() {
		for (Iterator iter = sampleMessageObjectList.iterator(); iter.hasNext();) {
			MessageObject messageObject = (MessageObject) iter.next();
			messageObjectController.updateMessage(messageObject);
		}
	}
}
