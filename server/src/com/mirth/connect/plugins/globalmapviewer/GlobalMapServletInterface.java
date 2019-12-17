/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.globalmapviewer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Extension Services")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface GlobalMapServletInterface extends BaseServletInterface {

    public static final String PLUGIN_POINT = "Global Maps";
    public static final String PERMISSION_VIEW = "View Global Maps";

    @GET
    @Path("/maps/all")
    @Operation(summary = "Retrieves global and/or global channel map information.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "globalMaps", ref = "../apiexamples/global_maps_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "globalMaps", ref = "../apiexamples/global_maps_json") }) })
    @MirthOperation(name = "getAllMaps", display = "Get global / global channel maps", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public Map<String, Map<String, String>> getAllMaps(// @formatter:off
            @Param("channelIds") @Parameter(description = "The ID of the channel to retrieve global channel map information for.") @QueryParam("channelId") Set<String> channelIds,
            @Param("includeGlobalMap") @Parameter(description = "If true, the global map will be returned.") @QueryParam("includeGlobalMap") boolean includeGlobalMap) throws ClientException;
    // @formatter:on

    @POST
    @Path("/maps/_getAllMaps")
    @Operation(summary = "Retrieves global and/or global channel map information. This is a POST request alternative to GET /maps/all that may be used when there are too many channel IDs to include in the query parameters.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "globalMaps", ref = "../apiexamples/global_maps_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "globalMaps", ref = "../apiexamples/global_maps_json") }) })
    @MirthOperation(name = "getAllMaps", display = "Get global / global channel maps", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public Map<String, Map<String, String>> getAllMapsPost(// @formatter:off
            @Param("channelIds") @RequestBody(description = "The ID of the channel to retrieve global channel map information for.", content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "channelIds", ref = "../apiexamples/guid_set_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "channelIds", ref = "../apiexamples/guid_set_json") }) }) Set<String> channelIds,
            @Param("includeGlobalMap") @Parameter(description = "If true, the global map will be returned.") @QueryParam("includeGlobalMap") boolean includeGlobalMap) throws ClientException;
    // @formatter:on

    @GET
    @Path("/maps/global")
    @Operation(summary = "Retrieves global map information.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "globalMap", ref = "../apiexamples/generic_map_xml") }),
            @Content(mediaType = MediaType.TEXT_PLAIN, examples = {
                    @ExampleObject(name = "genericMap", ref = "../apiexamples/generic_map_xml") }) })
    @MirthOperation(name = "getGlobalMap", display = "Get global map", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    @Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN })
    public String getGlobalMap() throws ClientException;

    @GET
    @Path("/maps/{channelId}")
    @Operation(summary = "Retrieves global channel map information for a single channel.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "genericMapString", ref = "../apiexamples/generic_map_xml") }),
            @Content(mediaType = MediaType.TEXT_PLAIN, examples = {
                    @ExampleObject(name = "genericMapString", ref = "../apiexamples/generic_map_xml") }) })
    @MirthOperation(name = "getGlobalChannelMap", display = "Get global channel map", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    @Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN })
    public String getGlobalChannelMap(@Param("channelId") @Parameter(description = "The ID of the channel to retrieve global channel map information for.") @PathParam("channelId") String channelId) throws ClientException;
}