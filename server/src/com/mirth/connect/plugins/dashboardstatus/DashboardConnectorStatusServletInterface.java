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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
    public Map<String, Object[]> getConnectorStateMap(// @formatter:off
    		@Param("serverId") @ApiParam(value = "The server ID to retrieve connector statuses for. Connector Statuses across all servers are retrieved is this parameter is not specified.") @QueryParam("serverId") String serverId
	) throws ClientException;
    //@formatter:on

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
    public LinkedList<ConnectionLogItem> getAllChannelLogs(//@formatter:off
    		@Param("serverId") @ApiParam(value = "The server ID to retrieve logs for. Logs for all servers are retrieved is this parameter is not specified.") @QueryParam("serverId") String serverId,
    		@Param("fetchSize") @ApiParam(value = "Specifies the maximum number of log items to return.", required = true, defaultValue = "100") @QueryParam("fetchSize") int fetchSize,
    		@Param("lastLogId") @ApiParam(value = "The last log ID the client retrieved. Only log items with a greater ID will be returned.") @QueryParam("lastLogId") Long lastLogId) throws ClientException;
    //@formatter:on

    @GET
    @Path("/connectionLogs/{channelId}")
    @ApiOperation("Retrieves connection logs for a specific channel.")
    @MirthOperation(name = "getConnectionInfoLogs", display = "Get channel connection logs", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public LinkedList<ConnectionLogItem> getChannelLog(// @formatter:off
    		@Param("serverId") @ApiParam(value = "The server ID to retrieve logs for. Logs for all servers are retrieved is this parameter is not specified.") @QueryParam("serverId") String serverId,
    		@Param("channelId") @ApiParam(value = "The channel ID to retrieve logs for.", required = true) @PathParam("channelId") String channelId,
    		@Param("fetchSize") @ApiParam(value = "Specifies the maximum number of log items to return.", required = true, defaultValue = "100") @QueryParam("fetchSize") int fetchSize,
    		@Param("lastLogId") @ApiParam(value = "The last log ID the client retrieved. Only log items with a greater ID will be returned.") @QueryParam("lastLogId") Long lastLogId) throws ClientException;
    //@formatter:on

}