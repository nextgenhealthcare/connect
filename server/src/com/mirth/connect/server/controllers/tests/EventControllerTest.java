/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.mirth.connect.model.Event;
import com.mirth.connect.model.filters.EventFilter;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.tools.ScriptRunner;
import com.mirth.connect.server.util.SqlConfig;

public class EventControllerTest extends TestCase {
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private List<Event> sampleEventList;

    private boolean forceTempTableCreation = true;
    private String tempTableUID = "test";

    protected void setUp() throws Exception {
        super.setUp();
        // clear all database tables
        ScriptRunner.runScript(new File("conf/" + ControllerTestSuite.database + "/" + ControllerTestSuite.database + "-database.sql"));
        // ScriptRunner.runScript("derby-database.sql");
        sampleEventList = new ArrayList<Event>();

        for (int i = 0; i < 10; i++) {
            Event sampleEvent = new Event("Sample event " + i);
            sampleEventList.add(sampleEvent);
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        SqlConfig.getSqlMapClient().update("Event.dropTempSystemEventsTable", tempTableUID);
    }

    public void testAddEvent() throws ControllerException {
        Event sampleEvent = sampleEventList.get(0);
        eventController.addEvent(sampleEvent);

        EventFilter testFilter = new EventFilter();
        eventController.createTempTable(testFilter, tempTableUID, forceTempTableCreation);

        List<Event> testEventList = eventController.getEventsByPage(-1, -1, 0, "test");

        Assert.assertEquals(1, testEventList.size());
    }

    public void testGetEvent() throws ControllerException {
        insertSampleEvents();

        EventFilter testFilter = new EventFilter();
        eventController.createTempTable(testFilter, tempTableUID, forceTempTableCreation);

        List<Event> testEventList = eventController.getEventsByPage(-1, -1, 0, "test");
        Assert.assertEquals(sampleEventList.size(), testEventList.size());
    }

    public void testRemoveEvent() throws ControllerException {
        insertSampleEvents();

        EventFilter testFilter = new EventFilter();
        eventController.createTempTable(testFilter, tempTableUID, forceTempTableCreation);

        eventController.removeAllEvents();
        List<Event> testEventList = eventController.getEventsByPage(-1, -1, 0, "test");
        Assert.assertTrue(testEventList.isEmpty());
    }

    private void insertSampleEvents() throws ControllerException {
        for (Iterator<Event> iter = sampleEventList.iterator(); iter.hasNext();) {
            Event sampleEvent = iter.next();
            eventController.addEvent(sampleEvent);
        }
    }
}