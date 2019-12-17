/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api.servlets;

import java.util.Set;

import javax.ws.rs.Consumes;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/channels")
@Tag(name = "Channel Deployment Operations")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface EngineServletInterface extends BaseServletInterface {

    @POST
    @Path("/_redeployAll")
    @Operation(summary = "Redeploys all channels.")
    @MirthOperation(name = "redeployAllChannels", display = "Redeploy all channels", permission = Permissions.CHANNELS_DEPLOY_UNDEPLOY, type = ExecuteType.ABORT_PENDING)
    public void redeployAllChannels(@Param("returnErrors") @Parameter(description = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;

    @POST
    @Path("/{channelId}/_deploy")
    @Operation(summary = "Deploys (or redeploys) a single channel.")
    @MirthOperation(name = "deployChannels", display = "Deploy channels", permission = Permissions.CHANNELS_DEPLOY_UNDEPLOY, type = ExecuteType.ABORT_PENDING)
    public void deployChannel(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel to deploy.", required = true) @PathParam("channelId") String channelId,
            @Param("returnErrors") @Parameter(description = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_deploy")
    @Operation(summary = "Deploys (or redeploys) selected channels.")
    @MirthOperation(name = "deployChannels", display = "Deploy channels", permission = Permissions.CHANNELS_DEPLOY_UNDEPLOY, type = ExecuteType.ABORT_PENDING)
    public void deployChannels(// @formatter:off
            @Param("channelIds") 
            @RequestBody(description = "The ID of the channel(s) to deploy. If absent, all channels will be deployed.", content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "channel_set", ref = "../apiexamples/guid_set_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "channel_set", ref = "../apiexamples/guid_set_json") }) })
            Set<String> channelIds,
            @Param("returnErrors") @Parameter(description = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/_undeploy")
    @Operation(summary = "Undeploys a single channel.")
    @MirthOperation(name = "undeployChannels", display = "Undeploy channels", permission = Permissions.CHANNELS_DEPLOY_UNDEPLOY, type = ExecuteType.ABORT_PENDING)
    public void undeployChannel(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel to undeploy.", required = true) @PathParam("channelId") String channelId,
            @Param("returnErrors") @Parameter(description = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_undeploy")
    @Operation(summary = "Undeploys selected channels.")
    @MirthOperation(name = "undeployChannels", display = "Undeploy channels", permission = Permissions.CHANNELS_DEPLOY_UNDEPLOY, type = ExecuteType.ABORT_PENDING)
    public void undeployChannels(// @formatter:off
            @Param("channelIds") 
            @RequestBody(description = "The IDs of the channels to retrieve. If absent, all channels will be retrieved.", content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "channel_set", ref = "../apiexamples/guid_set_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "channel_set", ref = "../apiexamples/guid_set_json") }) })
            Set<String> channelIds,
            @Param("returnErrors") @Parameter(description = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on
}