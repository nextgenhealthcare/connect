/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.List;

import com.mirth.connect.model.Event;
import com.mirth.connect.model.filters.EventFilter;

public abstract class EventController extends Controller {
    public static EventController getInstance() {
        return ControllerFactory.getFactory().createEventController();
    }

    /**
     * Adds an event.
     * 
     * @param event
     */
    public abstract void addEvent(Event event);

    /**
     * Removes all events.
     * 
     * @throws ControllerException
     */
    public abstract void removeAllEvents() throws ControllerException;

    public abstract String exportAllEvents() throws ControllerException;
    
    /**
     * Exports all events to a new CSV file, removes all the events, and returns
     * the path to the generated export file.
     * 
     * @param path
     * @throws ControllerException
     */
    public abstract String exportAndRemoveAllEvents() throws ControllerException;

    public abstract List<Event> getEventsByPage(int page, int pageSize, int maxSystemEvents, String uid) throws ControllerException;

    public abstract List<Event> getEventsByPageLimit(int page, int pageSize, int max, String uid, EventFilter filter) throws ControllerException;

    public abstract int createTempTable(EventFilter filter, String uid, boolean forceTemp) throws ControllerException;

    public abstract void removeFilterTable(String uid);

    public abstract void removeAllFilterTables();
}