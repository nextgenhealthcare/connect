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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("/channels")
@Api("Channel Deployment Operations")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface EngineServletInterface extends BaseServletInterface {

    @POST
    @Path("/_redeployAll")
    @ApiOperation("Redeploys all channels.")
    @MirthOperation(name = "redeployAllChannels", display = "Redeploy all channels", permission = Permissions.CHANNELS_DEPLOY_UNDEPLOY, type = ExecuteType.ABORT_PENDING)
    public void redeployAllChannels(@Param("returnErrors") @ApiParam(value = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;

    @POST
    @Path("/{channelId}/_deploy")
    @ApiOperation("Deploys (or redeploys) a single channel.")
    @MirthOperation(name = "deployChannels", display = "Deploy channels", permission = Permissions.CHANNELS_DEPLOY_UNDEPLOY, type = ExecuteType.ABORT_PENDING)
    public void deployChannel(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel to deploy.", required = true) @PathParam("channelId") String channelId,
            @Param("returnErrors") @ApiParam(value = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_deploy")
    @ApiOperation("Deploys (or redeploys) selected channels.")
    @MirthOperation(name = "deployChannels", display = "Deploy channels", permission = Permissions.CHANNELS_DEPLOY_UNDEPLOY, type = ExecuteType.ABORT_PENDING)
    public void deployChannels(// @formatter:off
            @Param("channelIds") @ApiParam(value = "The ID of the channel(s) to deploy. If absent, all channels will be deployed.") Set<String> channelIds,
            @Param("returnErrors") @ApiParam(value = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/_undeploy")
    @ApiOperation("Undeploys a single channel.")
    @MirthOperation(name = "undeployChannels", display = "Undeploy channels", permission = Permissions.CHANNELS_DEPLOY_UNDEPLOY, type = ExecuteType.ABORT_PENDING)
    public void undeployChannel(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel to undeploy.", required = true) @PathParam("channelId") String channelId,
            @Param("returnErrors") @ApiParam(value = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_undeploy")
    @ApiOperation("Undeploys selected channels.")
    @MirthOperation(name = "undeployChannels", display = "Undeploy channels", permission = Permissions.CHANNELS_DEPLOY_UNDEPLOY, type = ExecuteType.ABORT_PENDING)
    public void undeployChannels(// @formatter:off
            @Param("channelIds") @ApiParam(value = "The ID of the channel(s) to undeploy. If absent, all channels will be undeployed.") Set<String> channelIds,
            @Param("returnErrors") @ApiParam(value = "If true, an error response code and the exception will be returned.") @QueryParam("returnErrors") boolean returnErrors) throws ClientException;
    // @formatter:on
}