/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

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

@Path("/connectors/file")

@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface FileConnectorServletInterface extends BaseServletInterface {

    public static final String PLUGIN_POINT = "File Connector Service";

    @POST
    @Path("/_testRead")
    @Operation(summary="Tests whether a file can be read from the specified directory.")
    @MirthOperation(name = "testRead", display = "Test Read", type = ExecuteType.ASYNC, auditable = false)
    public ConnectionTestResponse testRead(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @QueryParam("channelId") String channelId,
            @Param("channelName") @Parameter(description = "The name of the channel.", required = true) @QueryParam("channelName") String channelName,
            @Param("properties") @Parameter(description = "The File Reader properties to use.", required = true) FileReceiverProperties properties) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_testWrite")
    @Operation(summary="Tests whether a file can be written to the specified directory.")
    @MirthOperation(name = "testWrite", display = "Test Write", type = ExecuteType.ASYNC, auditable = false)
    public ConnectionTestResponse testWrite(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @QueryParam("channelId") String channelId,
            @Param("channelName") @Parameter(description = "The name of the channel.", required = true) @QueryParam("channelName") String channelName,
            @Param("properties") @Parameter(description = "The File Writer properties to use.", required = true) FileDispatcherProperties properties) throws ClientException;
    // @formatter:on
}