/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api.servlets;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.Operation.ExecuteType;
import com.mirth.connect.client.core.Permissions;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;
import com.mirth.connect.client.core.api.Param;
import com.mirth.connect.model.DashboardChannelInfo;
import com.mirth.connect.model.DashboardStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/channels")
@Tag(name = "Channel Status Operations")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface ChannelStatusServletInterface extends BaseServletInterface {

    @GET
    @Path("/{channelId}/status")
    @Operation(summary = "Returns the dashboard status for a single channel ID.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "dashboard_status", ref = "../apiexamples/dashboard_status_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "dashboard_status", ref = "../apiexamples/dashboard_status_json") }) })
    @MirthOperation(name = "getChannelStatus", display = "Get status for single channel", permission = Permissions.DASHBOARD_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public DashboardStatus getChannelStatus(@Param("channelId") @Parameter(description = "The channel ID to return a dashboard status for.") @PathParam("channelId") String channelId) throws ClientException;

    @GET
    @Path("/statuses")
    @Operation(summary = "Returns all channel dashboard statuses, or multiple statuses by channel ID.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "dashboard_status_list", ref = "../apiexamples/dashboard_status_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "dashboard_status_list", ref = "../apiexamples/dashboard_status_list_json") }) })
    @MirthOperation(name = "getChannelStatusList", display = "Get status list for specific channels", permission = Permissions.DASHBOARD_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public List<DashboardStatus> getChannelStatusList(// @formatter:off
            @Param("channelIds") @Parameter(description = "The channel IDs to return dashboard statuses for. If absent, all statuses will be returned.") @QueryParam("channelId") Set<String> channelIds,
            @Param("filter") @Parameter(description = "The filter string to limit dashboard statuses with.") @QueryParam("filter") String filter,
            @Param("includeUndeployed") @Parameter(description = "If true, statuses for undeployed channels will also be included.") @QueryParam("includeUndeployed") boolean includeUndeployed) throws ClientException;
    // @formatter:on

    @POST
    @Path("/statuses/_getChannelStatusList")
    @Operation(summary = "Returns all channel dashboard statuses, or multiple statuses by channel ID. This is a POST request alternative to GET /statuses that may be used when there are too many channel IDs to include in the query parameters.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "dashboard_status_list", ref = "../apiexamples/dashboard_status_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "dashboard_status_list", ref = "../apiexamples/dashboard_status_list_json") }) })
    @MirthOperation(name = "getChannelStatusList", display = "Get status list for specific channels", permission = Permissions.DASHBOARD_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public List<DashboardStatus> getChannelStatusListPost(// @formatter:off
            @Param("channelIds") 
            @RequestBody(description = "The channel IDs to return dashboard statuses for. If absent, all statuses will be returned.", content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "channel_set", ref = "../apiexamples/guid_set_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "channel_set", ref = "../apiexamples/guid_set_json") }) })
            Set<String> channelIds,
            
            @Param("filter") @Parameter(description = "The filter string to limit dashboard statuses with.") @QueryParam("filter") String filter,
            @Param("includeUndeployed") @Parameter(description = "If true, statuses for undeployed channels will also be included.") @QueryParam("includeUndeployed") boolean includeUndeployed) throws ClientException;
    // @formatter:on

    @GET
    @Path("/statuses/initial")
    @Operation(summary = "Returns a DashboardChannelInfo object containing a partial channel status list and a set of remaining channel IDs.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "dashboard_channel_info", ref = "../apiexamples/dashboard_channel_info_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "dashboard_channel_info", ref = "../apiexamples/dashboard_channel_info_json") }) })
    @MirthOperation(name = "getChannelStatusListInitial", display = "Get initial channel status list", permission = Permissions.DASHBOARD_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public DashboardChannelInfo getDashboardChannelInfo(// @formatter:off
            @Param("fetchSize") @Parameter(description = "Specifies the maximum number of statuses to return.", required = true, schema = @Schema(defaultValue = "100")) @QueryParam("fetchSize") int fetchSize,
            @Param("filter") @Parameter(description = "The filter string to limit dashboard statuses with.") @QueryParam("filter") String filter) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/_start")
    @Operation(summary = "Starts the channel with the specified ID.")
    @MirthOperation(name = "startChannels", display = "Start channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void startChannel(// @formatter:off
            @Param("channelId") @Parameter(description = "The channel ID to start.", required = true) @PathParam("channelId") String channelId,
            @Param("returnErrors") @Parameter(description = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_start")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Starts the channels with the specified IDs. " + SWAGGER_ARRAY_DISCLAIMER)
    @MirthOperation(name = "startChannels", display = "Start channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void startChannels(// @formatter:off
            @Param("channelIds") @Parameter(description = "The channel IDs to start.", required = true) @FormParam("channelId") Set<String> channelIds,
            @Param("returnErrors") @Parameter(description = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/_stop")
    @Operation(summary = "Stops the channel with the specified ID.")
    @MirthOperation(name = "stopChannels", display = "Stop channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void stopChannel(// @formatter:off
            @Param("channelId") @Parameter(description = "The channel ID to stop.", required = true) @PathParam("channelId") String channelId,
            @Param("returnErrors") @Parameter(description = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_stop")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Stops the channels with the specified IDs. " + SWAGGER_ARRAY_DISCLAIMER)
    @MirthOperation(name = "stopChannels", display = "Stop channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void stopChannels(// @formatter:off
            @Param("channelIds") @Parameter(description = "The channel IDs to stop.", required = true) @FormParam("channelId") Set<String> channelIds,
            @Param("returnErrors") @Parameter(description = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/_halt")
    @Operation(summary = "Halts the channel with the specified ID.")
    @MirthOperation(name = "haltChannels", display = "Halt channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void haltChannel(// @formatter:off
            @Param("channelId") @Parameter(description = "The channel ID to halt.", required = true) @PathParam("channelId") String channelId,
            @Param("returnErrors") @Parameter(description = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_halt")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Halts the channels with the specified IDs. " + SWAGGER_ARRAY_DISCLAIMER)
    @MirthOperation(name = "haltChannels", display = "Halt channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void haltChannels(// @formatter:off
            @Param("channelIds") @Parameter(description = "The channel IDs to halt.", required = true) @FormParam("channelId") Set<String> channelIds,
            @Param("returnErrors") @Parameter(description = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/_pause")
    @Operation(summary = "Pauses the channel with the specified ID.")
    @MirthOperation(name = "pauseChannels", display = "Pause channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void pauseChannel(// @formatter:off
            @Param("channelId") @Parameter(description = "The channel ID to pause.", required = true) @PathParam("channelId") String channelId,
            @Param("returnErrors") @Parameter(description = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_pause")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Pauses the channels with the specified IDs. " + SWAGGER_ARRAY_DISCLAIMER)
    @MirthOperation(name = "pauseChannels", display = "Pause channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void pauseChannels(// @formatter:off
            @Param("channelIds") @Parameter(description = "The channel IDs to pause.", required = true) @FormParam("channelId") Set<String> channelIds,
            @Param("returnErrors") @Parameter(description = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/_resume")
    @Operation(summary = "Resumes the channel with the specified ID.")
    @MirthOperation(name = "resumeChannels", display = "Resume channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void resumeChannel(// @formatter:off
            @Param("channelId") @Parameter(description = "The channel ID to resume.", required = true) @PathParam("channelId") String channelId,
            @Param("returnErrors") @Parameter(description = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_resume")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Resume the channels with the specified IDs. " + SWAGGER_ARRAY_DISCLAIMER)
    @MirthOperation(name = "resumeChannels", display = "Resume channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void resumeChannels(// @formatter:off
            @Param("channelIds") @Parameter(description = "The channel IDs to resume.", required = true) @FormParam("channelId") Set<String> channelIds,
            @Param("returnErrors") @Parameter(description = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/connector/{metaDataId}/_start")
    @Operation(summary = "Starts the connector with the specified channel and metadata ID.")
    @MirthOperation(name = "startConnectors", display = "Start connectors", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void startConnector(// @formatter:off
            @Param("channelId") @Parameter(description = "The channel ID to start a connector for.", required = true) @PathParam("channelId") String channelId,
            @Param("metaDataId") @Parameter(description = "The connector metadata ID to start.", required = true) @PathParam("metaDataId") Integer metaDataId,
            @Param("returnErrors") @Parameter(description = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_startConnectors")
    @Operation(summary = "Starts the connectors with the specified channel and metadata IDs.")
    @MirthOperation(name = "startConnectors", display = "Start connectors", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void startConnectors(// @formatter:off
            @Param("connectorInfo") 
            @RequestBody(description = "A map of channel and metadata IDs to start connectors for.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "start_connector_map", ref = "../apiexamples/start_connector_map_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "start_connector_map", ref = "../apiexamples/start_connector_map_json") }) })
            Map<String, List<Integer>> connectorInfo,
            
            @Param("returnErrors") @Parameter(description = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/connector/{metaDataId}/_stop")
    @Operation(summary = "Stops the connector with the specified channel and metadata ID.")
    @MirthOperation(name = "stopConnectors", display = "Stop connectors", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void stopConnector(// @formatter:off
            @Param("channelId") @Parameter(description = "The channel ID to stop a connector for.", required = true) @PathParam("channelId") String channelId,
            @Param("metaDataId") @Parameter(description = "The connector metadata ID to stop.", required = true) @PathParam("metaDataId") Integer metaDataId,
            @Param("returnErrors") @Parameter(description = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_stopConnectors")
    @Operation(summary = "Stops the connectors with the specified channel and metadata IDs.")
    @MirthOperation(name = "stopConnectors", display = "Stop connectors", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void stopConnectors(// @formatter:off
            @Param("connectorInfo") 
            @RequestBody(description = "A map of channel and metadata IDs to stop connectors for.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "start_connector_map", ref = "../apiexamples/start_connector_map_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "start_connector_map", ref = "../apiexamples/start_connector_map_json") }) })
            Map<String, List<Integer>> connectorInfo,
            
            @Param("returnErrors") @Parameter(description = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on
}