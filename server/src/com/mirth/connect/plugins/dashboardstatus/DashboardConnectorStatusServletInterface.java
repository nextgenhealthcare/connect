/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.dashboardstatus;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.LinkedList;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.Operation.ExecuteType;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;
import com.mirth.connect.client.core.api.Param;

@Path("/extensions/dashboardstatus")
@Api("Extension Services")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface DashboardConnectorStatusServletInterface extends BaseServletInterface {

    public static final String PLUGIN_POINT = "Dashboard Connector Status Service";
    public static final String PERMISSION_VIEW = "View Connection Status";

    @GET
    @Path("/connectorStates")
    @ApiOperation("Retrieves all dashboard connector states.")
    @MirthOperation(name = "getStates", display = "Get dashboard connector states", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public Map<String, Object[]> getConnectorStateMap() throws ClientException;

    @GET
    @Path("/channelStates")
    @ApiOperation("Retrieves all dashboard channel states.")
    @MirthOperation(name = "getChannelStates", display = "Get dashboard channel states", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public Map<String, String> getChannelStates() throws ClientException;

    @GET
    @Path("/channelStates/{channelId}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation("Retrieves a single dashboard channel state.")
    @MirthOperation(name = "getChannelState", display = "Get dashboard channel state", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public String getChannelState(@Param("channelId") @ApiParam("The channel ID to return a dashboard status for.") @PathParam("channelId") String channelId) throws ClientException;

    @GET
    @Path("/connectionLogs")
    @ApiOperation("Retrieves connection logs for all channels.")
    @MirthOperation(name = "getConnectionInfoLogs", display = "Get channel connection logs", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public LinkedList<String[]> getAllChannelLogs() throws ClientException;

    @GET
    @Path("/connectionLogs/{channelName}")
    @ApiOperation("Retrieves connection logs for a specific channel.")
    @MirthOperation(name = "getConnectionInfoLogs", display = "Get channel connection logs", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public LinkedList<String[]> getChannelLog(@Param("channelName") @ApiParam(value = "The name of the channel to retrieve logs for.", required = true) @PathParam("channelName") String channelName) throws ClientException;

    @POST
    @Path("/connectionLogs/_startSession")
    @ApiOperation("Starts a connection log session if one isn't already running. Returns true if a session was created.")
    @MirthOperation(name = "startSession", display = "Start connection log session", type = ExecuteType.ASYNC, auditable = false)
    public boolean startSession() throws ClientException;

    @POST
    @Path("/connectionLogs/_stopSession")
    @ApiOperation("Tells the server to stop tracking connection log entries for the current session.")
    @MirthOperation(name = "stopSession", display = "Stop connection log session", type = ExecuteType.ASYNC, auditable = false)
    public void stopSession() throws ClientException;
}