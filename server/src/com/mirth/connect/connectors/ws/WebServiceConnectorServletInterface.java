/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
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

@Path("/connectors/ws")
@Api("Connector Services")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface WebServiceConnectorServletInterface extends BaseServletInterface {

    public static final String PLUGIN_POINT = "Web Service Connector Service";

    @POST
    @Path("/_cacheWsdlFromUrl")
    @ApiOperation("Downloads the WSDL at the specified URL and caches the web service definition tree.")
    @MirthOperation(name = "cacheWsdlFromUrl", display = "Download and cache WSDL", type = ExecuteType.ASYNC, auditable = false)
    public Object cacheWsdlFromUrl(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @QueryParam("channelId") String channelId,
            @Param("channelName") @ApiParam(value = "The name of the channel.", required = true) @QueryParam("channelName") String channelName,
            @Param("properties") @ApiParam(value = "The Web Service Sender properties to use.", required = true) WebServiceDispatcherProperties properties) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_isWsdlCached")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ApiOperation("Returns true if the definition tree for the WSDL is cached by the server.")
    @MirthOperation(name = "isWsdlCached", display = "Check if WSDL is cached", type = ExecuteType.ASYNC, auditable = false)
    public boolean isWsdlCached(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @FormParam("channelId") String channelId,
            @Param("channelName") @ApiParam(value = "The name of the channel.", required = true) @FormParam("channelName") String channelName,
            @Param("wsdlUrl") @ApiParam(value = "The WSDL URL to check.", required = true) @FormParam("wsdlUrl") String wsdlUrl,
            @Param("username") @ApiParam(value = "Username used to authenticate to the web server.") @FormParam("username") String username,
            @Param(value = "password", excludeFromAudit = true) @ApiParam(value = "Password used to authenticate to the web server.") @FormParam("password") String password) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_getDefinition")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ApiOperation("Retrieves the definition service map corresponding to the specified WSDL.")
    @MirthOperation(name = "getDefinition", display = "Get WSDL Definition", type = ExecuteType.ASYNC, auditable = false)
    public DefinitionServiceMap getDefinition(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @FormParam("channelId") String channelId,
            @Param("channelName") @ApiParam(value = "The name of the channel.", required = true) @FormParam("channelName") String channelName,
            @Param("wsdlUrl") @ApiParam(value = "The WSDL URL to check.", required = true) @FormParam("wsdlUrl") String wsdlUrl,
            @Param("username") @ApiParam(value = "Username used to authenticate to the web server.") @FormParam("username") String username,
            @Param(value = "password", excludeFromAudit = true) @ApiParam(value = "Password used to authenticate to the web server.") @FormParam("password") String password) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_generateEnvelope")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ApiOperation("Generate SOAP envelope for a given WSDL operation.")
    @MirthOperation(name = "generateEnvelope", display = "Generate WSDL operation envelope", type = ExecuteType.ASYNC, auditable = false)
    public String generateEnvelope(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @FormParam("channelId") String channelId,
            @Param("channelName") @ApiParam(value = "The name of the channel.", required = true) @FormParam("channelName") String channelName,
            @Param("wsdlUrl") @ApiParam(value = "The WSDL URL to check.", required = true) @FormParam("wsdlUrl") String wsdlUrl,
            @Param("username") @ApiParam(value = "Username used to authenticate to the web server.") @FormParam("username") String username,
            @Param(value = "password", excludeFromAudit = true) @ApiParam(value = "Password used to authenticate to the web server.") @FormParam("password") String password,
            @Param("service") @ApiParam(value = "The service name for the WSDL operation.") @FormParam("service") String service,
            @Param("port") @ApiParam(value = "The port / endpoint name for the service.") @FormParam("port") String port,
            @Param("operation") @ApiParam(value = "The name of the operation to generate an envelope for.") @FormParam("operation") String operation, 
            @Param("buildOptional") @ApiParam(value = "Whether to include optional fields in the envelope.") @FormParam("buildOptional") boolean buildOptional) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_getSoapAction")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation("Retrieves the default SOAP Action for a given WSDL operation.")
    @MirthOperation(name = "getSoapAction", display = "Get SOAP Action for operation", type = ExecuteType.ASYNC, auditable = false)
    public String getSoapAction(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @FormParam("channelId") String channelId,
            @Param("channelName") @ApiParam(value = "The name of the channel.", required = true) @FormParam("channelName") String channelName,
            @Param("wsdlUrl") @ApiParam(value = "The WSDL URL to check.", required = true) @FormParam("wsdlUrl") String wsdlUrl,
            @Param("username") @ApiParam(value = "Username used to authenticate to the web server.") @FormParam("username") String username,
            @Param(value = "password", excludeFromAudit = true) @ApiParam(value = "Password used to authenticate to the web server.") @FormParam("password") String password,
            @Param("service") @ApiParam(value = "The service name for the WSDL operation.") @FormParam("service") String service,
            @Param("port") @ApiParam(value = "The port / endpoint name for the service.") @FormParam("port") String port,
            @Param("operation") @ApiParam(value = "The name of the operation to generate an envelope for.") @FormParam("operation") String operation) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_testConnection")
    @ApiOperation("Tests whether a connection can be successfully established to the destination endpoint.")
    @MirthOperation(name = "testConnection", display = "Test Web Service Connection", type = ExecuteType.ASYNC, auditable = false)
    public ConnectionTestResponse testConnection(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @QueryParam("channelId") String channelId,
            @Param("channelName") @ApiParam(value = "The name of the channel.", required = true) @QueryParam("channelName") String channelName,
            @Param("properties") @ApiParam(value = "The Web Service Sender properties to use.", required = true) WebServiceDispatcherProperties properties) throws ClientException;
    // @formatter:on
}