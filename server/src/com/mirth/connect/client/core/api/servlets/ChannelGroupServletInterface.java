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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/channelgroups")
@Tag(name = "Channel Groups")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface ChannelGroupServletInterface extends BaseServletInterface {

    @GET
    @Path("/")
    @Operation(summary = "Retrieve a list of all channel groups, or multiple channel groups by ID.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "channel_group_list", ref = "../apiexamples/channel_group_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "channel_group_list", ref = "../apiexamples/channel_group_list_json") }) })
    @MirthOperation(name = "getChannelGroups", display = "Get channel groups", permission = Permissions.CHANNEL_GROUPS_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public List<ChannelGroup> getChannelGroups(@Param("channelGroupIds") @Parameter(description = "The IDs of the channel groups to retrieve. If absent, all groups will be retrieved.") @QueryParam("channelGroupId") Set<String> channelGroupIds) throws ClientException;

    @POST
    @Path("/_getChannelGroups")
    @Operation(summary = "Retrieve a list of all channel groups, or multiple channel groups by ID. This is a POST request alternative to GET /channelgroups that may be used when there are too many channel group IDs to include in the query parameters.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "channel_group_list", ref = "../apiexamples/channel_group_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "channel_group_list", ref = "../apiexamples/channel_group_list_json") }) })
    @MirthOperation(name = "getChannelGroups", display = "Get channel groups", permission = Permissions.CHANNEL_GROUPS_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public List<ChannelGroup> getChannelGroupsPost(@Param("channelGroupIds") @RequestBody(description = "The IDs of the channel groups to retrieve. If absent, all groups will be retrieved.", content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "group_set", ref = "../apiexamples/guid_set_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "group_set", ref = "../apiexamples/guid_set_json") }) }) Set<String> channelGroupIds) throws ClientException;

    @POST
    @Path("/_bulkUpdate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    @Operation(summary = "Updates all channel groups in one request. " + SWAGGER_TRY_IT_OUT_DISCLAIMER)
    @MirthOperation(name = "updateChannelGroups", display = "Update channel groups", permission = Permissions.CHANNELS_MANAGE)
    public boolean updateChannelGroups(// @formatter:off
            @Param("channelGroups") 
            @Parameter(description = "The channel group object to update or create.")
            @Schema(description = "The channel group object to update or create.", type = "object")
            @FormDataParam("channelGroups")
            Set<ChannelGroup> channelGroups,
            
            @Param("removedChannelGroupIds") 
            @Parameter(description = "All channel group IDs known to be removed.") 
            @Schema(description = "All channel group IDs known to be removed.", type = "object")
            @FormDataParam("removedChannelGroupIds") 
            Set<String> removedChannelGroupIds,
            
            @Param("override") 
            @Parameter(description = "If true, the channel groups will be updated even if different revisions exist on the server.", schema = @Schema(defaultValue = "false")) 
            @QueryParam("override") 
            boolean override) throws ClientException;
    // @formatter:on
}