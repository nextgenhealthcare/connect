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

import com.mirth.connect.model.SystemEvent;
import com.mirth.connect.model.filters.SystemEventFilter;

public abstract class EventController extends Controller {
    public static EventController getInstance() {
        return ControllerFactory.getFactory().createEventController();
    }

    /**
     * Adds a new system event.
     * 
     * @param systemEvent
     * @throws ControllerException
     */
    public abstract void logSystemEvent(SystemEvent systemEvent);

    /**
     * Clears the sysem event list.
     * 
     */
    public abstract void clearSystemEvents() throws ControllerException;

    public abstract int removeSystemEvents(SystemEventFilter filter) throws ControllerException;

    public abstract int createSystemEventsTempTable(SystemEventFilter filter, String uid, boolean forceTemp) throws ControllerException;

    public abstract void removeFilterTable(String uid);

    public abstract void removeAllFilterTables();

    public abstract List<SystemEvent> getSystemEventsByPage(int page, int pageSize, int maxSystemEvents, String uid) throws ControllerException;

    public abstract List<SystemEvent> getSystemEventsByPageLimit(int page, int pageSize, int maxSystemEvents, String uid, SystemEventFilter filter) throws ControllerException;

}
