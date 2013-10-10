/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.event.Event;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.event.DeployedStateEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.donkey.server.event.EventType;
import com.mirth.connect.donkey.server.event.MessageEvent;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.filters.EventFilter;
import com.mirth.connect.server.event.AuditableEventListener;
import com.mirth.connect.server.event.EventListener;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;

public class DefaultEventController extends EventController {
    private Logger logger = Logger.getLogger(this.getClass());

    private static DefaultEventController instance = null;

    private static Map<Object, BlockingQueue<Event>> messageEventQueues = new ConcurrentHashMap<Object, BlockingQueue<Event>>();
    private static Map<Object, BlockingQueue<Event>> errorEventQueues = new ConcurrentHashMap<Object, BlockingQueue<Event>>();
    private static Map<Object, BlockingQueue<Event>> deployedStateEventQueues = new ConcurrentHashMap<Object, BlockingQueue<Event>>();
    private static Map<Object, BlockingQueue<Event>> connectionStatusEventQueues = new ConcurrentHashMap<Object, BlockingQueue<Event>>();
    private static Map<Object, BlockingQueue<Event>> serverEventQueues = new ConcurrentHashMap<Object, BlockingQueue<Event>>();
    private static Map<Object, BlockingQueue<Event>> genericEventQueues = new ConcurrentHashMap<Object, BlockingQueue<Event>>();

    private DefaultEventController() {
        addListener(new AuditableEventListener());
    }

    public static EventController create() {
        synchronized (DefaultEventController.class) {
            if (instance == null) {
                instance = new DefaultEventController();
            }

            return instance;
        }
    }

    @Override
    public void addListener(EventListener listener) {
        Set<EventType> types = listener.getEventTypes();
        BlockingQueue<Event> queue = listener.getQueue();
        
        if (types.contains(EventType.MESSAGE)) {
            messageEventQueues.put(listener, queue);
        }

        if (types.contains(EventType.ERROR)) {
            errorEventQueues.put(listener, queue);
        }

        if (types.contains(EventType.DEPLOY_STATE)) {
            deployedStateEventQueues.put(listener, queue);
        }

        if (types.contains(EventType.CONNECTION_STATUS)) {
            connectionStatusEventQueues.put(listener, queue);
        }
        
        if (types.contains(EventType.SERVER)) {
            serverEventQueues.put(listener, queue);
        }
        
        if (types.contains(EventType.GENERIC)) {
            genericEventQueues.put(listener, queue);
        }
    }

    @Override
    public void removeListener(EventListener listener) {
        messageEventQueues.remove(listener);
        errorEventQueues.remove(listener);
        deployedStateEventQueues.remove(listener);
        connectionStatusEventQueues.remove(listener);
        serverEventQueues.remove(listener);
        genericEventQueues.remove(listener);

        listener.shutdown();
    }

    @Override
    public void dispatchEvent(Event event) {
        try {
            Map<Object, BlockingQueue<Event>> queues = null;
            /*
             * Using instanceof is several thousand times faster than using a map to store the
             * different queue sets.
             */
            if (event instanceof MessageEvent) {
                queues = messageEventQueues;
            } else if (event instanceof ErrorEvent) {
                queues = errorEventQueues;
            } else if (event instanceof DeployedStateEvent) {
                queues = deployedStateEventQueues;
            } else if (event instanceof ConnectionStatusEvent) {
                queues = connectionStatusEventQueues;
            } else if (event instanceof ServerEvent) {
                queues = serverEventQueues;
            } else {
                queues = genericEventQueues;
            }

            for (BlockingQueue<Event> queue : queues.values()) {
                queue.put(event);
            }
        } catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void insertEvent(ServerEvent serverEvent) {
        logger.debug("adding event: " + serverEvent);

        try {
            SqlConfig.getSqlSessionManager().insert("Event.insertEvent", serverEvent);
        } catch (Exception e) {
            logger.error("Error adding event.", e);
        }
    }
    
    @Override
    public Integer getMaxEventId() throws ControllerException {
        try {
            return SqlConfig.getSqlSessionManager().selectOne("Event.getMaxEventId");
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }
    
    @Override
    public List<ServerEvent> getEvents(EventFilter filter, Integer offset, Integer limit) throws ControllerException {
        try {
            return SqlConfig.getSqlSessionManager().selectList("Event.searchEvents", getParameters(filter, offset, limit));
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    @Override
    public Long getEventCount(EventFilter filter) throws ControllerException {
        return SqlConfig.getSqlSessionManager().selectOne("Event.searchEventsCount", getParameters(filter, null, null));
    }
    
    @Override
    public void removeAllEvents() throws ControllerException {
        logger.debug("removing all events");

        try {
            SqlConfig.getSqlSessionManager().delete("Event.deleteAllEvents");

            if (DatabaseUtil.statementExists("Event.vacuumEventTable")) {
                SqlConfig.getSqlSessionManager().update("Event.vacuumEventTable");
            }
        } catch (PersistenceException e) {
            throw new ControllerException("Error removing all events.", e);
        }
    }
    
    private Map<String, Object> getParameters(EventFilter filter, Integer offset, Integer limit) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("maxEventId", filter.getMaxEventId());
        params.put("offset", offset);
        params.put("limit", limit);

        params.put("id", filter.getId());
        params.put("name", filter.getName());
        params.put("levels", filter.getLevels());

        if (filter.getStartDate() != null) {
            params.put("startDate", filter.getStartDate());
        }

        if (filter.getEndDate() != null) {
            params.put("endDate", filter.getEndDate());
        }

        params.put("outcome", filter.getOutcome());
        params.put("userId", filter.getUserId());
        params.put("ipAddress", filter.getIpAddress());

        return params;
    }

    @Override
    public String exportAllEvents() throws ControllerException {
        logger.debug("exporting events");

        long currentTimeMillis = System.currentTimeMillis();
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(currentTimeMillis);
        String appDataDir = ControllerFactory.getFactory().createConfigurationController().getApplicationDataDir();
        File exportDir = new File(appDataDir, "exports");
        exportDir.mkdir();
        File exportFile = new File(exportDir, currentDateTime + "-events.txt");

        try {
            FileWriter writer = new FileWriter(exportFile, true);

            // write the CSV headers to the file
            writer.write(ServerEvent.getExportHeader());
            writer.write(System.getProperty("line.separator"));

            EventFilter filter = new EventFilter();
            int maxEventId = getMaxEventId();
            filter.setMaxEventId(maxEventId);
            int interval = 10;

            List<ServerEvent> events = getEvents(filter, null, interval);
            while (!events.isEmpty()) {
                for (ServerEvent event : events) {
                    writer.write(event.toExportString());
                    
                    if (event.getId() <= maxEventId) {
                        maxEventId = event.getId() - 1;
                    }
                }
                
                filter.setMaxEventId(maxEventId);
                events = getEvents(filter, null, interval);
            }

            IOUtils.closeQuietly(writer);
            logger.debug("events exported to file: " + exportFile.getAbsolutePath());

            ServerEvent event = new ServerEvent("Sucessfully exported events");
            event.addAttribute("file", exportFile.getAbsolutePath());
            dispatchEvent(event);
        } catch (IOException e) {
            throw new ControllerException("Error exporting events to file.", e);
        }

        return exportFile.getAbsolutePath();
    }
    
    public String exportAndRemoveAllEvents() throws ControllerException {
        try {
            String exportFilePath = exportAllEvents();
            removeAllEvents();
            return exportFilePath;
        } catch (ControllerException e) {
            throw e;
        }
    }
}
