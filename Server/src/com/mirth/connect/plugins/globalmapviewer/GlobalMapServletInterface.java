/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.globalmapviewer;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

@Path("/extensions/globalmapviewer")
@Api("Extension Services")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface GlobalMapServletInterface extends BaseServletInterface {

    public static final String PLUGIN_POINT = "Global Maps";
    public static final String PERMISSION_VIEW = "View Global Maps";

    @GET
    @Path("/maps/all")
    @ApiOperation("Retrieves global and/or global channel map information.")
    @MirthOperation(name = "getAllMaps", display = "Get global / global channel maps", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public Map<String, Map<String, String>> getAllMaps(// @formatter:off
            @Param("channelIds") @ApiParam(value = "The ID of the channel to retrieve global channel map information for.") @QueryParam("channelId") Set<String> channelIds,
            @Param("includeGlobalMap") @ApiParam(value = "If true, the global map will be returned.") @QueryParam("includeGlobalMap") boolean includeGlobalMap) throws ClientException;
    // @formatter:on

    @POST
    @Path("/maps/_getAllMaps")
    @ApiOperation("Retrieves global and/or global channel map information. This is a POST request alternative to GET /maps/all that may be used when there are too many channel IDs to include in the query parameters.")
    @MirthOperation(name = "getAllMaps", display = "Get global / global channel maps", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public Map<String, Map<String, String>> getAllMapsPost(// @formatter:off
            @Param("channelIds") @ApiParam(value = "The ID of the channel to retrieve global channel map information for.") Set<String> channelIds,
            @Param("includeGlobalMap") @ApiParam(value = "If true, the global map will be returned.") @QueryParam("includeGlobalMap") boolean includeGlobalMap) throws ClientException;
    // @formatter:on

    @GET
    @Path("/maps/global")
    @ApiOperation("Retrieves global map information.")
    @MirthOperation(name = "getGlobalMap", display = "Get global map", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public String getGlobalMap() throws ClientException;

    @GET
    @Path("/maps/{channelId}")
    @ApiOperation("Retrieves global channel map information for a single channel.")
    @MirthOperation(name = "getGlobalChannelMap", display = "Get global channel map", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public String getGlobalChannelMap(@Param("channelId") @ApiParam(value = "The ID of the channel to retrieve global channel map information for.") @PathParam("channelId") String channelId) throws ClientException;
}