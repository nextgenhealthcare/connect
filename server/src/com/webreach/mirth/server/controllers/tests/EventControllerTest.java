/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.controllers.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.filters.SystemEventFilter;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.EventController;
import com.webreach.mirth.server.tools.ScriptRunner;

public class EventControllerTest extends TestCase {
	private EventController eventController = ControllerFactory.getFactory().createEventController();
	private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
	private List<SystemEvent> sampleEventList;

	protected void setUp() throws Exception {
		super.setUp();
		// clear all database tables
		ScriptRunner.runScript("derby-database.sql");
		sampleEventList = new ArrayList<SystemEvent>();
		
		for(int i = 0; i < 10; i++) {
			SystemEvent sampleEvent = new SystemEvent("Sample event " + i);
			sampleEventList.add(sampleEvent);
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAddEvent() throws ControllerException {
		SystemEvent sampleEvent = sampleEventList.get(0);
		eventController.logSystemEvent(sampleEvent);
		
		SystemEventFilter testFilter = new SystemEventFilter();
        eventController.createSystemEventsTempTable(testFilter, "test", false);
		
		List<SystemEvent> testEventList = eventController.getSystemEventsByPage(-1, -1, 0, "test");

		Assert.assertEquals(1, testEventList.size());
	}

	public void testGetEvent() throws ControllerException {
		insertSampleEvents();
		
		SystemEventFilter testFilter = new SystemEventFilter();
        eventController.createSystemEventsTempTable(testFilter, "test", false);
		
		List<SystemEvent> testEventList = eventController.getSystemEventsByPage(-1, -1, 0, "test");
		Assert.assertEquals(sampleEventList.size(), testEventList.size());
	}

	public void testRemoveEvent() throws ControllerException {
		insertSampleEvents();
		
		SystemEventFilter testFilter = new SystemEventFilter();
        eventController.createSystemEventsTempTable(testFilter, "test", false);
		
		eventController.clearSystemEvents();
		List<SystemEvent> testEventList = eventController.getSystemEventsByPage(-1, -1, 0, "test");
		Assert.assertTrue(testEventList.isEmpty());
	}

	private void insertSampleEvents() throws ControllerException {
		for (Iterator iter = sampleEventList.iterator(); iter.hasNext();) {
			SystemEvent sampleEvent = (SystemEvent) iter.next();
			eventController.logSystemEvent(sampleEvent);
		}
	}
}