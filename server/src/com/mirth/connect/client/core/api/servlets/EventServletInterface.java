/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api.servlets;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Events")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface EventServletInterface extends BaseServletInterface {

    @GET
    @Path("/maxEventId")
    @Operation(summary = "Returns the maximum event ID currently in the database.")
    @MirthOperation(name = "getMaxEventId", display = "Get max event ID", permission = Permissions.EVENTS_VIEW, auditable = false)
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public Integer getMaxEventId() throws ClientException;

    @GET
    @Path("/{eventId}")
    @Operation(summary = "Retrieves an event by ID.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "serverEvent", ref = "../apiexamples/server_event_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "serverEvent", ref = "../apiexamples/server_event_json") }) })
    @MirthOperation(name = "getEvent", display = "Get event by ID", permission = Permissions.EVENTS_VIEW, auditable = false, abortable = true)
    public ServerEvent getEvent(@Param("eventId") @Parameter(description = "The ID of the event.", required = true) @PathParam("eventId") Integer eventId) throws ClientException;

    @POST
    @Path("/_search")
    @Operation(summary = "Search for events by specific filter criteria.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "serverEventList", ref = "../apiexamples/server_event_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "serverEventList", ref = "../apiexamples/server_event_list_json") }) })
    @MirthOperation(name = "getEvents", display = "Get events", permission = Permissions.EVENTS_VIEW, auditable = false, abortable = true)
    public List<ServerEvent> getEvents(// @formatter:off
            @Param("filter") @RequestBody(description = "The EventFilter object to use to query events by.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "filter", ref = "../apiexamples/event_filter_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "filter", ref = "../apiexamples/event_filter_json") }) }) EventFilter filter,
            @Param("offset") @Parameter(description = "Used for pagination, determines where to start in the search results.", schema = @Schema(defaultValue = "0")) @QueryParam("offset") Integer offset,
            @Param("limit") @Parameter(description = "Used for pagination, determines the maximum number of results to return.", schema = @Schema(defaultValue = "20")) @QueryParam("limit") Integer limit) throws ClientException;
    // @formatter:on

    @GET
    @Path("/")
    @Operation(summary = "Search for events by specific filter criteria.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "serverEventList", ref = "../apiexamples/server_event_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "serverEventList", ref = "../apiexamples/server_event_list_json") }) })
    @MirthOperation(name = "getEvents", display = "Get events", permission = Permissions.EVENTS_VIEW, auditable = false, abortable = true)
    public List<ServerEvent> getEvents(// @formatter:off
            @Param("maxEventId") @Parameter(description = "The maximum event ID to query.") @QueryParam("maxEventId") Integer maxEventId,
            @Param("minEventId") @Parameter(description = "The minimum event ID to query.") @QueryParam("minEventId") Integer minEventId,
            @Param("levels") @Parameter(description = "The type of events to query.") @QueryParam("level") Set<Level> levels,
            @Param("startDate") @Parameter(description = "The earliest event date to query by. Example: 1985-10-26T09:00:00.000-0700") @QueryParam("startDate") Calendar startDate,
            @Param("endDate") @Parameter(description = "The latest event date to query by. Example: 2015-10-21T07:28:00.000-0700") @QueryParam("endDate") Calendar endDate,
            @Param("name") @Parameter(description = "Searches the event name for this string.") @QueryParam("name") String name,
            @Param("outcome") @Parameter(description = "Searches on whether the event outcome was successful or not.") @QueryParam("outcome") Outcome outcome,
            @Param("userId") @Parameter(description = "The user ID to query events by.") @QueryParam("userId") Integer userId,
            @Param("ipAddress") @Parameter(description = "The IP address that originated the event.") @QueryParam("ipAddress") String ipAddress,
            @Param("serverId") @Parameter(description = "The ID of the server that the event was created from.") @QueryParam("serverId") String serverId,
            @Param("offset") @Parameter(description = "Used for pagination, determines where to start in the search results.", schema = @Schema(defaultValue = "0")) @QueryParam("offset") Integer offset,
            @Param("limit") @Parameter(description = "Used for pagination, determines the maximum number of results to return.", schema = @Schema(defaultValue = "20")) @QueryParam("limit") Integer limit) throws ClientException;
    // @formatter:on

    @POST
    @Path("/count/_search")
    @Operation(summary = "Count number for events by specific filter criteria.")
    @MirthOperation(name = "getEventCount", display = "Get events results count", permission = Permissions.EVENTS_VIEW, auditable = false, abortable = true)
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public Long getEventCount(@Param("filter") @RequestBody(description = "The EventFilter object to use to query events by.", required = true, content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "filter", ref = "../apiexamples/event_filter_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "filter", ref = "../apiexamples/event_filter_json") }) }) EventFilter filter) throws ClientException;

    @GET
    @Path("/count")
    @Operation(summary = "Count number for events by specific filter criteria.")
    @MirthOperation(name = "getEventCount", display = "Get events results count", permission = Permissions.EVENTS_VIEW, auditable = false, abortable = true)
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public Long getEventCount(// @formatter:off
            @Param("maxEventId") @Parameter(description = "The maximum event ID to query.") @QueryParam("maxEventId") Integer maxEventId,
            @Param("minEventId") @Parameter(description = "The minimum event ID to query.") @QueryParam("minEventId") Integer minEventId,
            @Param("levels") @Parameter(description = "The type of events to query.") @QueryParam("level") Set<Level> levels,
            @Param("startDate") @Parameter(description = "The earliest event date to query by. Example: 1985-10-26T09:00:00.000-0700") @QueryParam("startDate") Calendar startDate,
            @Param("endDate") @Parameter(description = "The latest event date to query by. Example: 2015-10-21T07:28:00.000-0700") @QueryParam("endDate") Calendar endDate,
            @Param("name") @Parameter(description = "Searches the event name for this string.") @QueryParam("name") String name,
            @Param("outcome") @Parameter(description = "Searches on whether the event outcome was successful or not.") @QueryParam("outcome") Outcome outcome,
            @Param("userId") @Parameter(description = "The user ID to query events by.") @QueryParam("userId") Integer userId,
            @Param("ipAddress") @Parameter(description = "The IP address that originated the event.") @QueryParam("ipAddress") String ipAddress,
            @Param("serverId") @Parameter(description = "The ID of the server that the event was created from.") @QueryParam("serverId") String serverId) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_export")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Exports all events to the application data directory on the server.")
    @MirthOperation(name = "exportAllEvents", display = "Export all events", permission = Permissions.EVENTS_VIEW)
    public String exportAllEvents() throws ClientException;

    @DELETE
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Remove all events.")
    @MirthOperation(name = "removeAllEvents", display = "Remove all events", permission = Permissions.EVENTS_REMOVE, abortable = true)
    public String removeAllEvents(@Param("export") @Parameter(description = "If true, messages will be exported into the application data directory on the server before being removed.", schema = @Schema(defaultValue = "true")) @QueryParam("export") boolean export) throws ClientException;
}