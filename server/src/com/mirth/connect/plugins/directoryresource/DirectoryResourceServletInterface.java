/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.directoryresource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.Operation.ExecuteType;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;
import com.mirth.connect.client.core.api.Param;

@Path("/extensions/directoryresource")
@Tag(name = "Extension Services")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface DirectoryResourceServletInterface extends BaseServletInterface {

    public static final String PLUGIN_POINT = DirectoryResourceProperties.PLUGIN_POINT;

    @GET
    @Path("/resources/{resourceId}/libraries")
    @Operation(summary = "Retrieves all library URLs for the given directory resource.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "libraryList", ref = "../apiexamples/library_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "libraryList", ref = "../apiexamples/library_list_json") }) })
    @MirthOperation(name = "getLibraries", display = "Get libraries", type = ExecuteType.ASYNC)
    public List<String> getLibraries(@Param("resourceId") @Parameter(description = "The ID of the directory resource.", required = true) @PathParam("resourceId") String resourceId) throws ClientException;
}