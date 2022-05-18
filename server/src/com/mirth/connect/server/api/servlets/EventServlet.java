/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.servlets;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.collections4.CollectionUtils;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.client.core.api.servlets.EventServletInterface;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.ServerEvent.Level;
import com.mirth.connect.model.ServerEvent.Outcome;
import com.mirth.connect.model.filters.EventFilter;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;

public class EventServlet extends MirthServlet implements EventServletInterface {

    private static final EventController eventController = ControllerFactory.getFactory().createEventController();

    public EventServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc);
    }

    @Override
    public Integer getMaxEventId() {
        try {
            return eventController.getMaxEventId();
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public ServerEvent getEvent(Integer eventId) {
        try {
            EventFilter filter = new EventFilter();
            filter.setId(eventId);
            List<ServerEvent> events = eventController.getEvents(filter, 0, 1);
            if (CollectionUtils.isEmpty(events)) {
                throw new MirthApiException(Status.NOT_FOUND);
            }
            return events.iterator().next();
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public List<ServerEvent> getEvents(EventFilter filter, Integer offset, Integer limit) {
        try {
            return eventController.getEvents(filter, offset, limit);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public List<ServerEvent> getEvents(Integer maxEventId, Integer minEventId, Set<Level> levels, Calendar startDate, Calendar endDate, String name, Outcome outcome, Integer userId, String patientId, String ipAddress, String serverId, Integer offset, Integer limit) {
        EventFilter filter = new EventFilter();
        filter.setMaxEventId(maxEventId);
        filter.setMinEventId(minEventId);
        if (CollectionUtils.isNotEmpty(levels)) {
            filter.setLevels(levels);
        }
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setName(name);
        filter.setOutcome(outcome);
        filter.setUserId(userId);
        filter.setPatientId(patientId);
        filter.setIpAddress(ipAddress);
        filter.setServerId(serverId);

        try {
            return eventController.getEvents(filter, offset, limit);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public Long getEventCount(EventFilter filter) {
        try {
            return eventController.getEventCount(filter);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public Long getEventCount(Integer maxEventId, Integer minEventId, Set<Level> levels, Calendar startDate, Calendar endDate, String name, Outcome outcome, Integer userId, String patientId, String ipAddress, String serverId) {
        EventFilter filter = new EventFilter();
        filter.setMaxEventId(maxEventId);
        filter.setMinEventId(minEventId);
        filter.setLevels(levels);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setName(name);
        filter.setOutcome(outcome);
        filter.setUserId(userId);
        filter.setPatientId(patientId);
        filter.setIpAddress(ipAddress);
        filter.setServerId(serverId);

        try {
            return eventController.getEventCount(filter);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public String exportAllEvents() {
        try {
            return eventController.exportAllEvents();
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public String removeAllEvents(boolean export) {
        try {
            if (export) {
                // Add file path of export and audit after removal
                String exportPath = eventController.exportAndRemoveAllEvents();
                parameterMap.put("file", exportPath);
                isUserAuthorized();
                return exportPath;
            } else {
                eventController.removeAllEvents();
                // Audit after removal
                isUserAuthorized();
                return null;
            }
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }
}