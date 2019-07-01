/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api.servlets;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

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

@Path("/databaseTasks")
@Api("Database Tasks")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface DatabaseTaskServletInterface extends BaseServletInterface {

    @GET
    @Path("/")
    @ApiOperation("Retrieves all current database tasks.")
    @MirthOperation(name = "getDatabaseTasks", display = "Get database tasks", permission = Permissions.DATABASE_TASKS_VIEW)
    public Map<String, DatabaseTask> getDatabaseTasks() throws ClientException;

    @GET
    @Path("/{databaseTaskId}")
    @ApiOperation("Retrieves a single database task.")
    @MirthOperation(name = "getDatabaseTask", display = "Get database task", permission = Permissions.DATABASE_TASKS_VIEW)
    public DatabaseTask getDatabaseTask(@Param("databaseTaskId") @ApiParam(value = "The ID of the database task.", required = true) @PathParam("databaseTaskId") String databaseTaskId) throws ClientException;

    @POST
    @Path("/{databaseTaskId}/_run")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation("Executes the specified database task.")
    @MirthOperation(name = "runDatabaseTask", display = "Run database task", permission = Permissions.DATABASE_TASKS_MANAGE)
    public String runDatabaseTask(@Param("databaseTaskId") @ApiParam(value = "The ID of the database task.", required = true) @PathParam("databaseTaskId") String databaseTaskId) throws ClientException;

    @POST
    @Path("/{databaseTaskId}/_cancel")
    @ApiOperation("Cancels execution of the specified database task.")
    @MirthOperation(name = "cancelDatabaseTask", display = "Cancel database task", permission = Permissions.DATABASE_TASKS_MANAGE)
    public void cancelDatabaseTask(@Param("databaseTaskId") @ApiParam(value = "The ID of the database task.", required = true) @PathParam("databaseTaskId") String databaseTaskId) throws ClientException;
}