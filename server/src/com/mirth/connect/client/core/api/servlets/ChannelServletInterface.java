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
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelHeader;
import com.mirth.connect.model.ChannelSummary;

@Path("/channels")
@Api("Channels")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface ChannelServletInterface extends BaseServletInterface {

    @POST
    @Path("/")
    @ApiOperation("Creates a new channel.")
    @MirthOperation(name = "createChannel", display = "Create channel", permission = Permissions.CHANNELS_MANAGE)
    public boolean createChannel(@Param("channel") @ApiParam(value = "The Channel object to create.", required = true) Channel channel) throws ClientException;

    @GET
    @Path("/")
    @ApiOperation("Retrieve a list of all channels, or multiple channels by ID.")
    @MirthOperation(name = "getChannels", display = "Get channels", permission = Permissions.CHANNELS_VIEW)
    public List<Channel> getChannels(@Param("channelIds") @ApiParam(value = "The IDs of the channels to retrieve. If absent, all channels will be retrieved.") @QueryParam("channelId") Set<String> channelIds, @Param("pollingOnly") @ApiParam(value = "If true, only channels with polling source connectors will be returned.") @QueryParam("pollingOnly") boolean pollingOnly) throws ClientException;

    @POST
    @Path("/_getChannels")
    @ApiOperation("Retrieve a list of all channels, or multiple channels by ID. This is a POST request alternative to GET /channels that may be used when there are too many channel IDs to include in the query parameters.")
    @MirthOperation(name = "getChannels", display = "Get channels", permission = Permissions.CHANNELS_VIEW)
    public List<Channel> getChannelsPost(@Param("channelIds") @ApiParam(value = "The IDs of the channels to retrieve. If absent, all channels will be retrieved.") Set<String> channelIds, @Param("pollingOnly") @ApiParam(value = "If true, only channels with polling source connectors will be returned.") @QueryParam("pollingOnly") boolean pollingOnly) throws ClientException;

    @GET
    @Path("/{channelId}")
    @ApiOperation("Retrieve a single channel by ID.")
    @MirthOperation(name = "getChannel", display = "Get channel", permission = Permissions.CHANNELS_VIEW)
    public Channel getChannel(@Param("channelId") @ApiParam(value = "The ID of the channel to retrieve.", required = true) @PathParam("channelId") String channelId) throws ClientException;

    @GET
    @Path("/{channelId}/connectorNames")
    @ApiOperation("Returns all connector names for a channel.")
    @MirthOperation(name = "getConnectorNames", display = "Get connector names", permission = Permissions.CHANNELS_VIEW, auditable = false)
    public Map<Integer, String> getConnectorNames(@Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId) throws ClientException;

    @GET
    @Path("/{channelId}/metaDataColumns")
    @ApiOperation("Returns all metadata columns for a channel.")
    @MirthOperation(name = "getMetaDataColumns", display = "Get metadata columns", permission = Permissions.CHANNELS_VIEW, auditable = false)
    public List<MetaDataColumn> getMetaDataColumns(@Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId) throws ClientException;

    @POST
    @Path("/_getSummary")
    @ApiOperation("Returns a list of channel summaries, indicating to a client which channels have changed (been updated, deleted, undeployed, etc.). If a channel was modified, the entire Channel object will be returned.")
    @MirthOperation(name = "getChannelSummary", display = "Get channel summary", permission = Permissions.CHANNELS_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public List<ChannelSummary> getChannelSummary(// @formatter:off
    		@Param("cachedChannels") @ApiParam(value = "A map of ChannelHeader objects telling the server the state of the client-side channel cache.", required = true) Map<String, ChannelHeader> cachedChannels,
    		@Param("ignoreNewChannels") @ApiParam(value = "If true, summaries will only be returned for channels in the map's entry set.", required = true) @QueryParam("ignoreNewChannels") boolean ignoreNewChannels) throws ClientException;
    // @formatter: on

    @POST
    @Path("/_setEnabled")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ApiOperation("Enables/disables the specified channels.")
    @MirthOperation(name = "setChannelEnabled", display = "Set channel enabled flag", permission = Permissions.CHANNELS_MANAGE)
    public void setChannelEnabled(// @formatter:off
            @Param("channelIds") @ApiParam(value = "The IDs of the channels to enable/disable. If absent, all channels will be enabled/disabled.") @FormParam("channelId") Set<String> channelIds,
            @Param("enabled") @ApiParam(value = "Indicates whether the channels should be enabled or disabled.", required = true) @FormParam("enabled") boolean enabled) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/enabled/{enabled}")
    @ApiOperation("Enables/disables the specified channel.")
    @MirthOperation(name = "setChannelEnabled", display = "Set channel enabled flag", permission = Permissions.CHANNELS_MANAGE)
    public void setChannelEnabled(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("enabled") @ApiParam(value = "The enabled flag (true/false) to set.", required = true) @PathParam("enabled") boolean enabled) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_setInitialState")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ApiOperation("Sets the initial state for the specified channels.")
    @MirthOperation(name = "setChannelInitialState", display = "Set channel initial state", permission = Permissions.CHANNELS_MANAGE)
    public void setChannelInitialState(// @formatter:off
            @Param("channelIds") @ApiParam(value = "The IDs of the channels to modify initial states on. If absent, the initial state will be set on all channels.") @FormParam("channelId") Set<String> channelIds,
            @Param("initialState") @ApiParam(value = "The initial state of the channel.", allowableValues = "STARTED, PAUSED, STOPPED", required = true) @FormParam("initialState") DeployedState initialState) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/initialState/{initialState}")
    @ApiOperation("Sets the initial state for a single channel.")
    @MirthOperation(name = "setChannelInitialState", display = "Set channel initial state", permission = Permissions.CHANNELS_MANAGE)
    public void setChannelInitialState(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("initialState") @ApiParam(value = "The initial state of the channel.", allowableValues = "STARTED, PAUSED, STOPPED", required = true) @PathParam("initialState") DeployedState initialState) throws ClientException;
    // @formatter:on

    @PUT
    @Path("/{channelId}")
    @ApiOperation("Updates the specified channel.")
    @MirthOperation(name = "updateChannel", display = "Update channel", permission = Permissions.CHANNELS_MANAGE)
    public boolean updateChannel(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel to update.", required = true) @PathParam("channelId") String channelId,
            @Param("channel") @ApiParam(value = "The Channel object to update with.", required = true) Channel channel,
            @Param("override") @ApiParam(value = "If true, the channel will be updated even if a different revision exists on the server.", defaultValue = "false") @QueryParam("override") boolean override) throws ClientException;
    // @formatter:on

    @DELETE
    @Path("/{channelId}")
    @ApiOperation("Removes the channel with the specified ID.")
    @MirthOperation(name = "removeChannel", display = "Remove channel", permission = Permissions.CHANNELS_MANAGE)
    public void removeChannel(@Param("channelId") @ApiParam(value = "The ID of the channel to remove.", required = true) @PathParam("channelId") String channelId) throws ClientException;

    @DELETE
    @Path("/")
    @ApiOperation("Removes the channels with the specified IDs.")
    @MirthOperation(name = "removeChannels", display = "Remove channels", permission = Permissions.CHANNELS_MANAGE)
    public void removeChannels(@Param("channelIds") @ApiParam(value = "The IDs of the channels to remove.", required = true) @QueryParam("channelId") Set<String> channelIds) throws ClientException;

    @POST
    @Path("/_removeChannels")
    @ApiOperation("Removes the channels with the specified IDs. This is a POST request alternative to DELETE /channels that may be used when there are too many channel IDs to include in the query parameters.")
    @MirthOperation(name = "removeChannels", display = "Remove channels", permission = Permissions.CHANNELS_MANAGE)
    public void removeChannelsPost(@Param("channelIds") @ApiParam(value = "The IDs of the channels to remove.", required = true) Set<String> channelIds) throws ClientException;
}