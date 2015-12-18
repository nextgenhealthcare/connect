/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.serverlog;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.LinkedList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.Operation.ExecuteType;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;

@Path("/extensions/serverlog")
@Api("Extension Services")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface ServerLogServletInterface extends BaseServletInterface {

    public static final String PLUGIN_POINT = "Server Log";
    public static final String PERMISSION_VIEW = "View Server Log";

    @GET
    @Path("/")
    @ApiOperation("Retrieves all server log entries.")
    @MirthOperation(name = "getMirthServerLogs", display = "View Server Log", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public LinkedList<String[]> getServerLogs() throws ClientException;

    @POST
    @Path("/_stop")
    @ApiOperation("Tells the server to stop tracking server log entries for the current session.")
    @MirthOperation(name = "removeSessionId", display = "Stop Server Log Session", type = ExecuteType.ASYNC, auditable = false)
    public boolean stopSession() throws ClientException;
}