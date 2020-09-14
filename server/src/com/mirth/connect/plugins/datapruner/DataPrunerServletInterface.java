/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datapruner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Extension Services")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface DataPrunerServletInterface extends BaseServletInterface {

    public static final String PLUGIN_POINT = "Data Pruner";

    public static final String PERMISSION_VIEW = "View Settings";
    public static final String PERMISSION_SAVE = "Save Settings";
    public static final String PERMISSION_START_STOP = "Start / Stop";

    public static final String TASK_START = "doStart";
    public static final String TASK_STOP = "doStop";

    @GET
    @Path("/status")
    @Operation(summary = "Retrieves the current data pruner status.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "dataPrunerStatusMap", ref = "../apiexamples/data_pruner_status_map_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "dataPrunerStatusMap", ref = "../apiexamples/data_pruner_status_map_json") }) })
    @MirthOperation(name = "getDataPrunerStatusMap", display = "Get status", permission = PERMISSION_VIEW)
    public Map<String, String> getStatusMap() throws ClientException;

    @POST
    @Path("/_start")
    @Operation(summary = "Starts the data pruner on-demand.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "calendar", ref = "../apiexamples/calendar_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "calendar", ref = "../apiexamples/calendar_json") }) })
    @MirthOperation(name = "startDataPruner", display = "Start pruner", permission = PERMISSION_START_STOP)
    public Calendar start() throws ClientException;

    @POST
    @Path("/_stop")
    @Operation(summary = "Stops the data pruner if currently running.")
    @MirthOperation(name = "stopDataPruner", display = "Stop pruner", permission = PERMISSION_START_STOP)
    public void stop() throws ClientException;
}