/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.smtp;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

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

@Path("/connectors/smtp")
@Api("Connector Services")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface SmtpConnectorServletInterface extends BaseServletInterface {

    public static final String PLUGIN_POINT = "SMTP Connector Service";

    @POST
    @Path("/_sendTestEmail")
    @ApiOperation("Sends a test e-mail, replacing any connector properties first.")
    @MirthOperation(name = "sendTestEmail", display = "Send Test Email", type = ExecuteType.ASYNC, auditable = false)
    public ConnectionTestResponse sendTestEmail(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @QueryParam("channelId") String channelId,
            @Param("channelName") @ApiParam(value = "The name of the channel.", required = true) @QueryParam("channelName") String channelName,
            @Param("properties") @ApiParam(value = "The SMTP Sender properties to use.", required = true) SmtpDispatcherProperties properties) throws ClientException;
    // @formatter:on
}