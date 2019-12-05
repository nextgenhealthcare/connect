/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Connector Services")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public interface WebServiceConnectorServletInterface extends BaseServletInterface {

    public static final String PLUGIN_POINT = "Web Service Connector Service";

    @POST
    @Path("/_cacheWsdlFromUrl")
    @Operation(summary="Downloads the WSDL at the specified URL and caches the web service definition tree.")
    @MirthOperation(name = "cacheWsdlFromUrl", display = "Download and cache WSDL", type = ExecuteType.ASYNC, auditable = false)
    public Object cacheWsdlFromUrl(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @QueryParam("channelId") String channelId,
            @Param("channelName") @Parameter(description = "The name of the channel.", required = true) @QueryParam("channelName") String channelName,
            @Param("properties") @Parameter(description = "The Web Service Sender properties to use.", required = true) WebServiceDispatcherProperties properties) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_isWsdlCached")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary="Returns true if the definition tree for the WSDL is cached by the server.")
    @MirthOperation(name = "isWsdlCached", display = "Check if WSDL is cached", type = ExecuteType.ASYNC, auditable = false)
    public boolean isWsdlCached(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @FormParam("channelId") String channelId,
            @Param("channelName") @Parameter(description = "The name of the channel.", required = true) @FormParam("channelName") String channelName,
            @Param("wsdlUrl") @Parameter(description = "The WSDL URL to check.", required = true) @FormParam("wsdlUrl") String wsdlUrl,
            @Param("username") @Parameter(description = "Username used to authenticate to the web server.") @FormParam("username") String username,
            @Param(value = "password", excludeFromAudit = true) @Parameter(description = "Password used to authenticate to the web server.") @FormParam("password") String password) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_getDefinition")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary="Retrieves the definition service map corresponding to the specified WSDL.")
    @MirthOperation(name = "getDefinition", display = "Get WSDL Definition", type = ExecuteType.ASYNC, auditable = false)
    public DefinitionServiceMap getDefinition(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @FormParam("channelId") String channelId,
            @Param("channelName") @Parameter(description = "The name of the channel.", required = true) @FormParam("channelName") String channelName,
            @Param("wsdlUrl") @Parameter(description = "The WSDL URL to check.", required = true) @FormParam("wsdlUrl") String wsdlUrl,
            @Param("username") @Parameter(description = "Username used to authenticate to the web server.") @FormParam("username") String username,
            @Param(value = "password", excludeFromAudit = true) @Parameter(description = "Password used to authenticate to the web server.") @FormParam("password") String password) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_generateEnvelope")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary="Generate SOAP envelope for a given WSDL operation.")
    @MirthOperation(name = "generateEnvelope", display = "Generate WSDL operation envelope", type = ExecuteType.ASYNC, auditable = false)
    public String generateEnvelope(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @FormParam("channelId") String channelId,
            @Param("channelName") @Parameter(description = "The name of the channel.", required = true) @FormParam("channelName") String channelName,
            @Param("wsdlUrl") @Parameter(description = "The WSDL URL to check.", required = true) @FormParam("wsdlUrl") String wsdlUrl,
            @Param("username") @Parameter(description = "Username used to authenticate to the web server.") @FormParam("username") String username,
            @Param(value = "password", excludeFromAudit = true) @Parameter(description = "Password used to authenticate to the web server.") @FormParam("password") String password,
            @Param("service") @Parameter(description = "The service name for the WSDL operation.") @FormParam("service") String service,
            @Param("port") @Parameter(description = "The port / endpoint name for the service.") @FormParam("port") String port,
            @Param("operation") @Parameter(description = "The name of the operation to generate an envelope for.") @FormParam("operation") String operation, 
            @Param("buildOptional") @Parameter(description = "Whether to include optional fields in the envelope.") @FormParam("buildOptional") boolean buildOptional) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_getSoapAction")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary="Retrieves the default SOAP Action for a given WSDL operation.")
    @MirthOperation(name = "getSoapAction", display = "Get SOAP Action for operation", type = ExecuteType.ASYNC, auditable = false)
    public String getSoapAction(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @FormParam("channelId") String channelId,
            @Param("channelName") @Parameter(description = "The name of the channel.", required = true) @FormParam("channelName") String channelName,
            @Param("wsdlUrl") @Parameter(description = "The WSDL URL to check.", required = true) @FormParam("wsdlUrl") String wsdlUrl,
            @Param("username") @Parameter(description = "Username used to authenticate to the web server.") @FormParam("username") String username,
            @Param(value = "password", excludeFromAudit = true) @Parameter(description = "Password used to authenticate to the web server.") @FormParam("password") String password,
            @Param("service") @Parameter(description = "The service name for the WSDL operation.") @FormParam("service") String service,
            @Param("port") @Parameter(description = "The port / endpoint name for the service.") @FormParam("port") String port,
            @Param("operation") @Parameter(description = "The name of the operation to generate an envelope for.") @FormParam("operation") String operation) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_testConnection")
    @Operation(summary="Tests whether a connection can be successfully established to the destination endpoint.")
    @MirthOperation(name = "testConnection", display = "Test Web Service Connection", type = ExecuteType.ASYNC, auditable = false)
    public ConnectionTestResponse testConnection(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @QueryParam("channelId") String channelId,
            @Param("channelName") @Parameter(description = "The name of the channel.", required = true) @QueryParam("channelName") String channelName,
            @Param("properties") @Parameter(description = "The Web Service Sender properties to use.", required = true) WebServiceDispatcherProperties properties) throws ClientException;
    // @formatter:on
}