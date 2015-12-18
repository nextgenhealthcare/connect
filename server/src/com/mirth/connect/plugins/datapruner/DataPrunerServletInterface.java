/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datapruner;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.Calendar;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;

@Path("/extensions/datapruner")
@Api("Extension Services")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface DataPrunerServletInterface extends BaseServletInterface {

    public static final String PLUGIN_POINT = "Data Pruner";

    public static final String PERMISSION_VIEW = "View Settings";
    public static final String PERMISSION_SAVE = "Save Settings";
    public static final String PERMISSION_START_STOP = "Start / Stop";

    public static final String TASK_START = "doStart";
    public static final String TASK_STOP = "doStop";

    @GET
    @Path("/status")
    @ApiOperation("Retrieves the current data pruner status.")
    @MirthOperation(name = "getDataPrunerStatusMap", display = "Get status", permission = PERMISSION_VIEW)
    public Map<String, String> getStatusMap() throws ClientException;

    @POST
    @Path("/_start")
    @ApiOperation("Starts the data pruner on-demand.")
    @MirthOperation(name = "startDataPruner", display = "Start pruner", permission = PERMISSION_START_STOP)
    public Calendar start() throws ClientException;

    @POST
    @Path("/_stop")
    @ApiOperation("Stops the data pruner if currently running.")
    @MirthOperation(name = "stopDataPruner", display = "Stop pruner", permission = PERMISSION_START_STOP)
    public void stop() throws ClientException;
}