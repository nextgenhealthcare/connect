/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.velocity.runtime.RuntimeConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.filters.EventFilter;
public class DefaultEventControllerTest {
    private EventController eventController;
    private final String SERVER_ID = "0000-0000-0000";
    private final String CHANNEL_ID = "d243960c-7de3-4059-b60c-399991df1cdc";
    private final String CHANNEL_NAME = "TestChannel2";
    private final String MESSAGE_ID = "1";
    private final String PATIENT_ID = "9908889";
    private final String EVENT_NAME = "EVENT DESCRIPTION";
    private final String EXPORT_HEADER = "ID, Date and Time, Level, Outcome, Name, User ID, IP Address, Attributes, ChannelID-MessageID, ChannelName, PatientID";
    
    
    @Before
    public void setup() {
        ControllerFactory controllerFactory = mock(ControllerFactory.class);
        ConfigurationController configurationController = mock(ConfigurationController.class);
        when(controllerFactory.createConfigurationController()).thenReturn(configurationController);
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                requestStaticInjection(ControllerFactory.class);
                bind(ControllerFactory.class).toInstance(controllerFactory);
            }
        });
        injector.getInstance(ControllerFactory.class);
        Logger logger = LogManager.getLogger(RuntimeConstants.DEFAULT_RUNTIME_LOG_NAME);
        Configurator.setLevel(logger.getName(), Level.OFF);
        eventController = new TestDefaultEventController();
    }
    @SuppressWarnings("deprecation")
    @Test
    public void testExportAllEvents() throws Exception {
        String exportedEventsFilePath = eventController.exportAllEvents();
        System.out.println(FileUtils.readFileToString(new File(exportedEventsFilePath)));
        //assertions
        Assert.assertNotNull(exportedEventsFilePath);
        
        List<String> results = Files.readAllLines(Paths.get(exportedEventsFilePath));
        assertEquals(EXPORT_HEADER, results.get(0).toString());
        assertEquals(13, results.size());
        assertTrue(results.get(1).contains(EVENT_NAME));
        assertTrue(results.get(2).contains(CHANNEL_ID + " - " + MESSAGE_ID));
        assertTrue(results.get(2).contains(PATIENT_ID));
        assertTrue(results.get(2).contains(CHANNEL_NAME));
        assertFalse(results.get(2).contains(SERVER_ID));
        assertTrue(results.get(3).contains(EVENT_NAME));
        
    }
    @SuppressWarnings("unused")
    private class TestDefaultEventController extends DefaultEventController {
        private final List<ServerEvent> serverEvents = new ArrayList<ServerEvent>();
       
        
        protected TestDefaultEventController() {
            super();
            ServerEvent e1 = new ServerEvent();
            e1.setId(1);
            e1.setServerId(SERVER_ID);
            e1.setName(EVENT_NAME);
            serverEvents.add(e1);
            ServerEvent e2 = new ServerEvent();
            e2.setId(2);
            e2.setServerId(SERVER_ID);
            e2.setName(EVENT_NAME);
            Map<String, String> attributes = new HashMap<String, String>();
            attributes.put("channel", "[id=" + CHANNEL_ID + ", name=" + CHANNEL_NAME +"]");
            attributes.put("messageId", MESSAGE_ID);
            attributes.put("patientId", PATIENT_ID);
            e2.setAttributes(attributes);
            serverEvents.add(e2);
            ServerEvent e3 = new ServerEvent();
            e3.setId(3);
            e3.setServerId(SERVER_ID);
            e3.setName(EVENT_NAME);
            serverEvents.add(e3);
            ServerEvent e4 = new ServerEvent();
            e4.setId(4);
            e4.setServerId(SERVER_ID);
            e4.setName(EVENT_NAME);
            serverEvents.add(e4);
            ServerEvent e5 = new ServerEvent();
            e5.setId(5);
            e5.setServerId(SERVER_ID);
            e5.setName(EVENT_NAME);
            e5.setAttributes(attributes);
            serverEvents.add(e5);
            ServerEvent e6 = new ServerEvent();
            e6.setId(6);
            e6.setServerId(SERVER_ID);
            e6.setName(EVENT_NAME);
            serverEvents.add(e6);
            ServerEvent e7 = new ServerEvent();
            e7.setId(7);
            e7.setServerId(SERVER_ID);
            e7.setName(EVENT_NAME);
            serverEvents.add(e7);
            ServerEvent e8 = new ServerEvent();
            e8.setId(8);
            e8.setServerId(SERVER_ID);
            e8.setName(EVENT_NAME);
            e8.setAttributes(attributes);
            serverEvents.add(e8);
            ServerEvent e9 = new ServerEvent();
            e9.setId(9);
            e9.setServerId(SERVER_ID);
            e9.setName(EVENT_NAME);
            serverEvents.add(e9);
            ServerEvent e10 = new ServerEvent();
            e10.setId(10);
            e10.setServerId(SERVER_ID);
            e10.setName(EVENT_NAME);
            serverEvents.add(e10);
            ServerEvent e11 = new ServerEvent();
            e11.setId(11);
            e11.setServerId(SERVER_ID);
            e11.setName(EVENT_NAME);
            e11.setAttributes(attributes);
            serverEvents.add(e11);
            ServerEvent e12 = new ServerEvent();
            e12.setId(12);
            e12.setServerId(SERVER_ID);
            e12.setName(EVENT_NAME);
            serverEvents.add(e12);
        }
        
        @Override
        public List<ServerEvent> getEvents(EventFilter filter, Integer offset, Integer limit)
                throws ControllerException {
            List<ServerEvent> eventsToReturn = new ArrayList<>();
            int maxEventId = filter.getMaxEventId();
            // Starting from the end of the events list and working backwards, return the first
            // "limit" number of events with an id <= to the maxEventId
            for (int i = serverEvents.size() - 1; i >= 0 && eventsToReturn.size() <= limit; i--) {
                if (serverEvents.get(i).getId() <= maxEventId) {
                    eventsToReturn.add(serverEvents.get(i));
                }
            }
            return eventsToReturn;
        }
        @Override
        public Integer getMaxEventId() throws ControllerException {
            return serverEvents.get(serverEvents.size() - 1).getId();
        }
    }
}
