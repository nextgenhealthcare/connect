/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api.servlets;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.Permissions;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;
import com.mirth.connect.client.core.api.Param;
import com.mirth.connect.model.DatabaseTask;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/databaseTasks")
@Tag(name = "Database Tasks")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface DatabaseTaskServletInterface extends BaseServletInterface {

    @GET
    @Path("/")
    @Operation(summary = "Retrieves all current database tasks.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "database_task_map", ref = "../apiexamples/database_task_map_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "database_task_map", ref = "../apiexamples/database_task_map_json") }) })
    @MirthOperation(name = "getDatabaseTasks", display = "Get database tasks", permission = Permissions.DATABASE_TASKS_VIEW)
    public Map<String, DatabaseTask> getDatabaseTasks() throws ClientException;

    @GET
    @Path("/{databaseTaskId}")
    @Operation(summary = "Retrieves a single database task.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "database_task", ref = "../apiexamples/database_task_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "database_task", ref = "../apiexamples/database_task_json") }) })
    @MirthOperation(name = "getDatabaseTask", display = "Get database task", permission = Permissions.DATABASE_TASKS_VIEW)
    public DatabaseTask getDatabaseTask(@Param("databaseTaskId") @Parameter(description = "The ID of the database task.", required = true) @PathParam("databaseTaskId") String databaseTaskId) throws ClientException;

    @POST
    @Path("/{databaseTaskId}/_run")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Executes the specified database task.")
    @ApiResponse(content = { @Content(mediaType = MediaType.TEXT_PLAIN, examples = {
            @ExampleObject(value = "Table SOME_TABLE successfully dropped.") }) })
    @MirthOperation(name = "runDatabaseTask", display = "Run database task", permission = Permissions.DATABASE_TASKS_MANAGE)
    public String runDatabaseTask(@Param("databaseTaskId") @Parameter(description = "The ID of the database task.", required = true) @PathParam("databaseTaskId") String databaseTaskId) throws ClientException;

    @POST
    @Path("/{databaseTaskId}/_cancel")
    @Operation(summary = "Cancels execution of the specified database task.")
    @MirthOperation(name = "cancelDatabaseTask", display = "Cancel database task", permission = Permissions.DATABASE_TASKS_MANAGE)
    public void cancelDatabaseTask(@Param("databaseTaskId") @Parameter(description = "The ID of the database task.", required = true) @PathParam("databaseTaskId") String databaseTaskId) throws ClientException;
}