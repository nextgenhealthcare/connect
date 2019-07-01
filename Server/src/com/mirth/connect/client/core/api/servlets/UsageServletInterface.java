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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.Operation.ExecuteType;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;
import com.mirth.connect.client.core.api.Param;

@Path("/usageData")
@Api("Usage Data")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface UsageServletInterface extends BaseServletInterface {

    @POST
    @Path("/_generate")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation("Generates usage document using data from both the client and server.")
    @MirthOperation(name = "getUsageData", display = "Get usage data", type = ExecuteType.ASYNC, auditable = false)
    public String getUsageData(@Param("clientStats") @ApiParam(value = "The map of client usage data to use.", required = true) Map<String, Object> clientStats) throws ClientException;
}