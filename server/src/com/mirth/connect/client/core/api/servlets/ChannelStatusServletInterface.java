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

@Path("/channels")
@Api("Channel Status Operations")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface ChannelStatusServletInterface extends BaseServletInterface {

    @GET
    @Path("/{channelId}/status")
    @ApiOperation("Returns the dashboard status for a single channel ID.")
    @MirthOperation(name = "getChannelStatus", display = "Get status for single channel", permission = Permissions.CHANNELS_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public DashboardStatus getChannelStatus(@Param("channelId") @ApiParam("The channel ID to return a dashboard status for.") @PathParam("channelId") String channelId) throws ClientException;

    @GET
    @Path("/statuses")
    @ApiOperation("Returns all channel dashboard statuses, or multiple statuses by channel ID.")
    @MirthOperation(name = "getChannelStatusList", display = "Get status list for specific channels", permission = Permissions.CHANNELS_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public List<DashboardStatus> getChannelStatusList(// @formatter:off
            @Param("channelIds") @ApiParam(value = "The channel IDs to return dashboard statuses for. If absent, all statuses will be returned.") @QueryParam("channelId") Set<String> channelIds,
            @Param("filter") @ApiParam(value = "The filter string to limit dashboard statuses with.") @QueryParam("filter") String filter,
            @Param("includeUndeployed") @ApiParam(value = "If true, statuses for undeployed channels will also be included.") @QueryParam("includeUndeployed") boolean includeUndeployed) throws ClientException;
    // @formatter:on

    @POST
    @Path("/statuses/_getChannelStatusList")
    @ApiOperation("Returns all channel dashboard statuses, or multiple statuses by channel ID. This is a POST request alternative to GET /statuses that may be used when there are too many channel IDs to include in the query parameters.")
    @MirthOperation(name = "getChannelStatusList", display = "Get status list for specific channels", permission = Permissions.CHANNELS_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public List<DashboardStatus> getChannelStatusListPost(// @formatter:off
            @Param("channelIds") @ApiParam(value = "The channel IDs to return dashboard statuses for. If absent, all statuses will be returned.") Set<String> channelIds,
            @Param("filter") @ApiParam(value = "The filter string to limit dashboard statuses with.") @QueryParam("filter") String filter,
            @Param("includeUndeployed") @ApiParam(value = "If true, statuses for undeployed channels will also be included.") @QueryParam("includeUndeployed") boolean includeUndeployed) throws ClientException;
    // @formatter:on

    @GET
    @Path("/statuses/initial")
    @ApiOperation("Returns a DashboardChannelInfo object containing a partial channel status list and a set of remaining channel IDs.")
    @MirthOperation(name = "getChannelStatusListInitial", display = "Get initial channel status list", permission = Permissions.CHANNELS_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public DashboardChannelInfo getDashboardChannelInfo(// @formatter:off
            @Param("fetchSize") @ApiParam(value = "Specifies the maximum number of statuses to return.", required = true, defaultValue = "100") @QueryParam("fetchSize") int fetchSize,
            @Param("filter") @ApiParam(value = "The filter string to limit dashboard statuses with.") @QueryParam("filter") String filter) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/_start")
    @ApiOperation("Starts the channel with the specified ID.")
    @MirthOperation(name = "startChannels", display = "Start channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void startChannel(// @formatter:off
            @Param("channelId") @ApiParam(value = "The channel ID to start.", required = true) @PathParam("channelId") String channelId,
            @Param("returnErrors") @ApiParam(value = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_start")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ApiOperation("Starts the channels with the specified IDs.")
    @MirthOperation(name = "startChannels", display = "Start channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void startChannels(// @formatter:off
            @Param("channelIds") @ApiParam(value = "The channel IDs to start.", required = true) @FormParam("channelId") Set<String> channelIds,
            @Param("returnErrors") @ApiParam(value = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/_stop")
    @ApiOperation("Stops the channel with the specified ID.")
    @MirthOperation(name = "stopChannels", display = "Stop channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void stopChannel(// @formatter:off
            @Param("channelId") @ApiParam(value = "The channel ID to stop.", required = true) @PathParam("channelId") String channelId,
            @Param("returnErrors") @ApiParam(value = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_stop")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ApiOperation("Stops the channels with the specified IDs.")
    @MirthOperation(name = "stopChannels", display = "Stop channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void stopChannels(// @formatter:off
            @Param("channelIds") @ApiParam(value = "The channel IDs to stop.", required = true) @FormParam("channelId") Set<String> channelIds,
            @Param("returnErrors") @ApiParam(value = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/_halt")
    @ApiOperation("Halts the channel with the specified ID.")
    @MirthOperation(name = "haltChannels", display = "Halt channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void haltChannel(// @formatter:off
            @Param("channelId") @ApiParam(value = "The channel ID to halt.", required = true) @PathParam("channelId") String channelId,
            @Param("returnErrors") @ApiParam(value = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_halt")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ApiOperation("Halts the channels with the specified IDs.")
    @MirthOperation(name = "haltChannels", display = "Halt channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void haltChannels(// @formatter:off
            @Param("channelIds") @ApiParam(value = "The channel IDs to halt.", required = true) @FormParam("channelId") Set<String> channelIds,
            @Param("returnErrors") @ApiParam(value = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/_pause")
    @ApiOperation("Pauses the channel with the specified ID.")
    @MirthOperation(name = "pauseChannels", display = "Pause channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void pauseChannel(// @formatter:off
            @Param("channelId") @ApiParam(value = "The channel ID to pause.", required = true) @PathParam("channelId") String channelId,
            @Param("returnErrors") @ApiParam(value = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_pause")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ApiOperation("Pauses the channels with the specified IDs.")
    @MirthOperation(name = "pauseChannels", display = "Pause channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void pauseChannels(// @formatter:off
            @Param("channelIds") @ApiParam(value = "The channel IDs to pause.", required = true) @FormParam("channelId") Set<String> channelIds,
            @Param("returnErrors") @ApiParam(value = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/_resume")
    @ApiOperation("Resumes the channel with the specified ID.")
    @MirthOperation(name = "resumeChannels", display = "Resume channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void resumeChannel(// @formatter:off
            @Param("channelId") @ApiParam(value = "The channel ID to resume.", required = true) @PathParam("channelId") String channelId,
            @Param("returnErrors") @ApiParam(value = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_resume")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ApiOperation("Resume the channels with the specified IDs.")
    @MirthOperation(name = "resumeChannels", display = "Resume channels", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void resumeChannels(// @formatter:off
            @Param("channelIds") @ApiParam(value = "The channel IDs to resume.", required = true) @FormParam("channelId") Set<String> channelIds,
            @Param("returnErrors") @ApiParam(value = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/connector/{metaDataId}/_start")
    @ApiOperation("Starts the connector with the specified channel and metadata ID.")
    @MirthOperation(name = "startConnectors", display = "Start connectors", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void startConnector(// @formatter:off
            @Param("channelId") @ApiParam(value = "The channel ID to start a connector for.", required = true) @PathParam("channelId") String channelId,
            @Param("metaDataId") @ApiParam(value = "The connector metadata ID to start.", required = true) @PathParam("metaDataId") Integer metaDataId,
            @Param("returnErrors") @ApiParam(value = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_startConnectors")
    @ApiOperation("Starts the connectors with the specified channel and metadata IDs.")
    @MirthOperation(name = "startConnectors", display = "Start connectors", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void startConnectors(// @formatter:off
            @Param("connectorInfo") @ApiParam(value = "A map of channel and metadata IDs to start connectors for.", required = true) Map<String, List<Integer>> connectorInfo,
            @Param("returnErrors") @ApiParam(value = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/connector/{metaDataId}/_stop")
    @ApiOperation("Stops the connector with the specified channel and metadata ID.")
    @MirthOperation(name = "stopConnectors", display = "Stop connectors", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void stopConnector(// @formatter:off
            @Param("channelId") @ApiParam(value = "The channel ID to stop a connector for.", required = true) @PathParam("channelId") String channelId,
            @Param("metaDataId") @ApiParam(value = "The connector metadata ID to stop.", required = true) @PathParam("metaDataId") Integer metaDataId,
            @Param("returnErrors") @ApiParam(value = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_stopConnectors")
    @ApiOperation("Stops the connectors with the specified channel and metadata IDs.")
    @MirthOperation(name = "stopConnectors", display = "Stop connectors", permission = Permissions.CHANNELS_START_STOP, type = ExecuteType.ABORT_PENDING)
    public void stopConnectors(// @formatter:off
            @Param("connectorInfo") @ApiParam(value = "A map of channel and metadata IDs to stop connectors for.", required = true) Map<String, List<Integer>> connectorInfo,
            @Param("returnErrors") @ApiParam(value = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on
}