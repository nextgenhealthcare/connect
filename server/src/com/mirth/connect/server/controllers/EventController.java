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

    public abstract void addEvent(Event event);

    public abstract void clearEvents() throws ControllerException;

    public abstract List<Event> getEventsByPage(int page, int pageSize, int maxSystemEvents, String uid) throws ControllerException;

    public abstract List<Event> getEventsByPageLimit(int page, int pageSize, int maxSystemEvents, String uid, EventFilter filter) throws ControllerException;

    public abstract int createEventTempTable(EventFilter filter, String uid, boolean forceTemp) throws ControllerException;

    public abstract void removeEventFilterTable(String uid);

    public abstract void removeAllEventFilterTables();
}