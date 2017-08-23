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
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataParam;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.Operation.ExecuteType;
import com.mirth.connect.client.core.Permissions;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;
import com.mirth.connect.client.core.api.Param;
import com.mirth.connect.model.ChannelGroup;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("/channelgroups")
@Api("Channel Groups")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface ChannelGroupServletInterface extends BaseServletInterface {

    @GET
    @Path("/")
    @ApiOperation("Retrieve a list of all channel groups, or multiple channel groups by ID.")
    @MirthOperation(name = "getChannelGroups", display = "Get channel groups", permission = Permissions.CHANNELS_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public List<ChannelGroup> getChannelGroups(@Param("channelGroupIds") @ApiParam(value = "The IDs of the channel groups to retrieve. If absent, all groups will be retrieved.") @QueryParam("channelGroupId") Set<String> channelGroupIds) throws ClientException;

    @POST
    @Path("/_getChannelGroups")
    @ApiOperation("Retrieve a list of all channel groups, or multiple channel groups by ID. This is a POST request alternative to GET /channelgroups that may be used when there are too many channel group IDs to include in the query parameters.")
    @MirthOperation(name = "getChannelGroups", display = "Get channel groups", permission = Permissions.CHANNELS_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public List<ChannelGroup> getChannelGroupsPost(@Param("channelGroupIds") @ApiParam(value = "The IDs of the channel groups to retrieve. If absent, all groups will be retrieved.") Set<String> channelGroupIds) throws ClientException;

    @POST
    @Path("/_bulkUpdate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation("Updates all channel groups in one request.")
    @MirthOperation(name = "updateChannelGroups", display = "Update channel groups", permission = Permissions.CHANNELS_MANAGE)
    public boolean updateChannelGroups(// @formatter:off
            @Param("channelGroups") @ApiParam(value = "The channel group object to update or create.") @FormDataParam("channelGroups") Set<ChannelGroup> channelGroups,
            @Param("removedChannelGroupIds") @ApiParam(value = "All channel group IDs known to be removed.") @FormDataParam("removedChannelGroupIds") Set<String> removedChannelGroupIds,
            @Param("override") @ApiParam(value = "If true, the channel groups will be updated even if different revisions exist on the server.", defaultValue = "false") @QueryParam("override") boolean override) throws ClientException;
    // @formatter:on
}