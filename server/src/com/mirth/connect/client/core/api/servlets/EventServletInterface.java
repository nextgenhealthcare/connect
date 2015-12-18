/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api.servlets;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.Permissions;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;
import com.mirth.connect.client.core.api.Param;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.ServerEvent.Level;
import com.mirth.connect.model.ServerEvent.Outcome;
import com.mirth.connect.model.filters.EventFilter;

@Path("/events")
@Api("Events")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface EventServletInterface extends BaseServletInterface {

    @GET
    @Path("/maxEventId")
    @ApiOperation("Returns the maximum event ID currently in the database.")
    @MirthOperation(name = "getMaxEventId", display = "Get max event ID", permission = Permissions.EVENTS_VIEW, auditable = false)
    public Integer getMaxEventId() throws ClientException;

    @GET
    @Path("/{eventId}")
    @ApiOperation("Retrieves an event by ID.")
    @MirthOperation(name = "getEvent", display = "Get event by ID", permission = Permissions.EVENTS_VIEW, auditable = false, abortable = true)
    public ServerEvent getEvent(@Param("eventId") @ApiParam(value = "The ID of the event.", required = true) @PathParam("eventId") Integer eventId) throws ClientException;

    @POST
    @Path("/_search")
    @ApiOperation("Search for events by specific filter criteria.")
    @MirthOperation(name = "getEvents", display = "Get events", permission = Permissions.EVENTS_VIEW, auditable = false, abortable = true)
    public List<ServerEvent> getEvents(// @formatter:off
            @Param("filter") @ApiParam(value = "The EventFilter object to use to query events by.", required = true) EventFilter filter,
            @Param("offset") @ApiParam(value = "Used for pagination, determines where to start in the search results.", defaultValue = "0") @QueryParam("offset") Integer offset,
            @Param("limit") @ApiParam(value = "Used for pagination, determines the maximum number of results to return.", defaultValue = "20") @QueryParam("limit") Integer limit) throws ClientException;
    // @formatter:on

    @GET
    @Path("/")
    @ApiOperation("Search for events by specific filter criteria.")
    @MirthOperation(name = "getEvents", display = "Get events", permission = Permissions.EVENTS_VIEW, auditable = false, abortable = true)
    public List<ServerEvent> getEvents(// @formatter:off
            @Param("maxEventId") @ApiParam(value = "The maximum event ID to query.") @QueryParam("maxEventId") Integer maxEventId,
            @Param("minEventId") @ApiParam(value = "The minimum event ID to query.") @QueryParam("minEventId") Integer minEventId,
            @Param("levels") @ApiParam(value = "The type of events to query.") @QueryParam("level") Set<Level> levels,
            @Param("startDate") @ApiParam(value = "The earliest event date to query by. Example: 1985-10-26T09:00:00.000-0700") @QueryParam("startDate") Calendar startDate,
            @Param("endDate") @ApiParam(value = "The latest event date to query by. Example: 2015-10-21T07:28:00.000-0700") @QueryParam("endDate") Calendar endDate,
            @Param("name") @ApiParam(value = "Searches the event name for this string.") @QueryParam("name") String name,
            @Param("outcome") @ApiParam(value = "Searches on whether the event outcome was successful or not.") @QueryParam("outcome") Outcome outcome,
            @Param("userId") @ApiParam(value = "The user ID to query events by.") @QueryParam("userId") Integer userId,
            @Param("ipAddress") @ApiParam(value = "The IP address that originated the event.") @QueryParam("ipAddress") String ipAddress,
            @Param("serverId") @ApiParam(value = "The ID of the server that the event was created from.") @QueryParam("serverId") String serverId,
            @Param("offset") @ApiParam(value = "Used for pagination, determines where to start in the search results.", defaultValue = "0") @QueryParam("offset") Integer offset,
            @Param("limit") @ApiParam(value = "Used for pagination, determines the maximum number of results to return.", defaultValue = "20") @QueryParam("limit") Integer limit) throws ClientException;
    // @formatter:on

    @POST
    @Path("/count/_search")
    @ApiOperation("Count number for events by specific filter criteria.")
    @MirthOperation(name = "getEventCount", display = "Get events results count", permission = Permissions.EVENTS_VIEW, auditable = false, abortable = true)
    public Long getEventCount(@Param("filter") @ApiParam(value = "The EventFilter object to use to query events by.", required = true) EventFilter filter) throws ClientException;

    @GET
    @Path("/count")
    @ApiOperation("Count number for events by specific filter criteria.")
    @MirthOperation(name = "getEventCount", display = "Get events results count", permission = Permissions.EVENTS_VIEW, auditable = false, abortable = true)
    public Long getEventCount(// @formatter:off
            @Param("maxEventId") @ApiParam(value = "The maximum event ID to query.") @QueryParam("maxEventId") Integer maxEventId,
            @Param("minEventId") @ApiParam(value = "The minimum event ID to query.") @QueryParam("minEventId") Integer minEventId,
            @Param("levels") @ApiParam(value = "The type of events to query.") @QueryParam("level") Set<Level> levels,
            @Param("startDate") @ApiParam(value = "The earliest event date to query by. Example: 1985-10-26T09:00:00.000-0700") @QueryParam("startDate") Calendar startDate,
            @Param("endDate") @ApiParam(value = "The latest event date to query by. Example: 2015-10-21T07:28:00.000-0700") @QueryParam("endDate") Calendar endDate,
            @Param("name") @ApiParam(value = "Searches the event name for this string.") @QueryParam("name") String name,
            @Param("outcome") @ApiParam(value = "Searches on whether the event outcome was successful or not.") @QueryParam("outcome") Outcome outcome,
            @Param("userId") @ApiParam(value = "The user ID to query events by.") @QueryParam("userId") Integer userId,
            @Param("ipAddress") @ApiParam(value = "The IP address that originated the event.") @QueryParam("ipAddress") String ipAddress,
            @Param("serverId") @ApiParam(value = "The ID of the server that the event was created from.") @QueryParam("serverId") String serverId) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_export")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation("Exports all events to the application data directory on the server.")
    @MirthOperation(name = "exportAllEvents", display = "Export all events", permission = Permissions.EVENTS_VIEW)
    public String exportAllEvents() throws ClientException;

    @DELETE
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation("Remove all events.")
    @MirthOperation(name = "removeAllEvents", display = "Remove all events", permission = Permissions.EVENTS_REMOVE, abortable = true)
    public String removeAllEvents(@Param("export") @ApiParam(value = "If true, messages will be exported into the application data directory on the server before being removed.", defaultValue = "true") @QueryParam("export") boolean export) throws ClientException;
}