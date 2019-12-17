/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.util.Set;
import java.util.SortedSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/connectors/jdbc")
@Tag(name = "Connector Services")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface DatabaseConnectorServletInterface extends BaseServletInterface {

    public static final String PLUGIN_POINT = "Database Connector Service";

    @POST
    @Path("/_getTables")
    @Operation(summary = "Executes a query to retrieve database table metadata.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "table_set", ref = "../apiexamples/table_set_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "table_set", ref = "../apiexamples/table_set_json") }) })
    @MirthOperation(name = "getTables", display = "Get Tables", type = ExecuteType.ASYNC, auditable = false)
    public SortedSet<Table> getTables(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @QueryParam("channelId") String channelId,
            @Param("channelName") @Parameter(description = "The name of the channel.", required = true) @QueryParam("channelName") String channelName,
            @Param("driver") @Parameter(description = "The JDBC driver class to use. (ex: org.postgresql.Driver)", required = true) @QueryParam("driver") String driver,
            @Param("url") @Parameter(description = "The JDBC connection URL to use. (ex: jdbc:postgresql://localhost:5432/mirthdb)", required = true) @QueryParam("url") String url,
            @Param("username") @Parameter(description = "The username to authenticate with.") @DefaultValue("") @QueryParam("username") String username,
            @Param(value = "password", excludeFromAudit = true) @Parameter(description = "The password to authenticate with.", schema = @Schema(format = "password")) @DefaultValue("") @QueryParam("password") String password,
            @Param("tableNamePatterns") @Parameter(description = "If specified, filters by table name. Wildcards (* or %) are allowed.") @QueryParam("tableNamePattern") Set<String> tableNamePatterns,
            @Param("selectLimit") @Parameter(description = "A simple query to use to retrieve database metadata information.", schema = @Schema(defaultValue = "SELECT * FROM ? LIMIT 1")) @DefaultValue("SELECT * FROM ? LIMIT 1") @QueryParam("selectLimit") String selectLimit,
            @Param("resourceIds") @Parameter(description = "Library resource IDs to use, if a custom driver is necessary.") @QueryParam("resourceId") Set<String> resourceIds) throws ClientException;
    // @formatter:on)
}