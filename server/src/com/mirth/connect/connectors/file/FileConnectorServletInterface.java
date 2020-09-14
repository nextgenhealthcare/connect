/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.Operation.ExecuteType;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;
import com.mirth.connect.client.core.api.Param;
import com.mirth.connect.util.ConnectionTestResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/connectors/file")
@Tag(name = "Connector Services")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface FileConnectorServletInterface extends BaseServletInterface {

    public static final String PLUGIN_POINT = "File Connector Service";

    @POST
    @Path("/_testRead")
    @Operation(summary = "Tests whether a file can be read from the specified directory.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "connection_test_response_file", ref = "../apiexamples/connection_test_response_file_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "connection_test_response_file", ref = "../apiexamples/connection_test_response_file_json") }) })
    @MirthOperation(name = "testRead", display = "Test Read", type = ExecuteType.ASYNC, auditable = false)
    public ConnectionTestResponse testRead(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @QueryParam("channelId") String channelId,
            @Param("channelName") @Parameter(description = "The name of the channel.", required = true) @QueryParam("channelName") String channelName,
            @Param("properties") 
            @RequestBody(description = "The File Reader properties to use.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "file_receiver_properties", ref = "../apiexamples/file_receiver_properties_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "file_receiver_properties", ref = "../apiexamples/file_receiver_properties_json") }) })
            FileReceiverProperties properties) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_testWrite")
    @Operation(summary = "Tests whether a file can be written to the specified directory.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "connection_test_response_file", ref = "../apiexamples/connection_test_response_file_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "connection_test_response_file", ref = "../apiexamples/connection_test_response_file_json") }) })
    @MirthOperation(name = "testWrite", display = "Test Write", type = ExecuteType.ASYNC, auditable = false)
    public ConnectionTestResponse testWrite(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @QueryParam("channelId") String channelId,
            @Param("channelName") @Parameter(description = "The name of the channel.", required = true) @QueryParam("channelName") String channelName,
            @Param("properties") 
            @RequestBody(description = "The File Writer properties to use.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "file_dispatcher_properties", ref = "../apiexamples/file_dispatcher_properties_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "file_dispatcher_properties", ref = "../apiexamples/file_dispatcher_properties_json") }) })
            FileDispatcherProperties properties) throws ClientException;
    // @formatter:on
}