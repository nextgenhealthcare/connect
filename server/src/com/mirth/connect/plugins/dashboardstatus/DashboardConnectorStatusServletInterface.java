/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.dashboardstatus;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Extension Services")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface DashboardConnectorStatusServletInterface extends BaseServletInterface {

    public static final String PLUGIN_POINT = "Dashboard Connector Status Service";
    public static final String PERMISSION_VIEW = "View Connection Status";

    @GET
    @Path("/connectorStates")
    @Operation(summary = "Retrieves all dashboard connector states.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "dashboardConnectorStateMap", ref = "../apiexamples/dashboard_connector_state_map_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "dashboardConnectorStateMap", ref = "../apiexamples/dashboard_connector_state_map_json") }) })
    @MirthOperation(name = "getStates", display = "Get dashboard connector states", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public Map<String, Object[]> getConnectorStateMap(// @formatter:off
    		@Param("serverId") @Parameter(description = "The server ID to retrieve connector statuses for. Connector Statuses across all servers are retrieved is this parameter is not specified.") @QueryParam("serverId") String serverId
	) throws ClientException;
    //@formatter:on

    @GET
    @Path("/channelStates")
    @Operation(summary = "Retrieves all dashboard channel states.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "dashboardChannelStateMap", ref = "../apiexamples/dashboard_channel_state_map_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "dashboardChannelStateMap", ref = "../apiexamples/dashboard_channel_state_map_json") }) })
    @MirthOperation(name = "getChannelStates", display = "Get dashboard channel states", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public Map<String, String> getChannelStates() throws ClientException;

    @GET
    @Path("/channelStates/{channelId}")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Retrieves a single dashboard channel state.")
    @MirthOperation(name = "getChannelState", display = "Get dashboard channel state", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public String getChannelState(@Param("channelId") @Parameter(description = "The channel ID to return a dashboard status for.") @PathParam("channelId") String channelId) throws ClientException;

    @GET
    @Path("/connectionLogs")
    @Operation(summary = "Retrieves connection logs for all channels.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "connectionLogItemLinkedList", ref = "../apiexamples/connection_log_item_linked_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "connectionLogItemLinkedList", ref = "../apiexamples/connection_log_item_linked_list_json") }) })
    @MirthOperation(name = "getConnectionInfoLogs", display = "Get channel connection logs", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public LinkedList<ConnectionLogItem> getAllChannelLogs(//@formatter:off
    		@Param("serverId") @Parameter(description = "The server ID to retrieve logs for. Logs for all servers are retrieved is this parameter is not specified.") @QueryParam("serverId") String serverId,
    		@Param("fetchSize") @Parameter(description = "Specifies the maximum number of log items to return.", required = true, schema = @Schema(defaultValue = "100")) @QueryParam("fetchSize") int fetchSize,
    		@Param("lastLogId") @Parameter(description = "The last log ID the client retrieved. Only log items with a greater ID will be returned.") @QueryParam("lastLogId") Long lastLogId) throws ClientException;
    //@formatter:on

    @GET
    @Path("/connectionLogs/{channelId}")
    @Operation(summary = "Retrieves connection logs for a specific channel.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "connectionLogItemLinkedList", ref = "../apiexamples/connection_log_item_linked_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "connectionLogItemLinkedList", ref = "../apiexamples/connection_log_item_linked_list_json") }) })
    @MirthOperation(name = "getConnectionInfoLogs", display = "Get channel connection logs", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public LinkedList<ConnectionLogItem> getChannelLog(// @formatter:off
    		@Param("serverId") @Parameter(description = "The server ID to retrieve logs for. Logs for all servers are retrieved is this parameter is not specified.") @QueryParam("serverId") String serverId,
    		@Param("channelId") @Parameter(description = "The channel ID to retrieve logs for.", required = true) @PathParam("channelId") String channelId,
    		@Param("fetchSize") @Parameter(description = "Specifies the maximum number of log items to return.", required = true, schema = @Schema(defaultValue = "100")) @QueryParam("fetchSize") int fetchSize,
    		@Param("lastLogId") @Parameter(description = "The last log ID the client retrieved. Only log items with a greater ID will be returned.") @QueryParam("lastLogId") Long lastLogId) throws ClientException;
    //@formatter:on

}