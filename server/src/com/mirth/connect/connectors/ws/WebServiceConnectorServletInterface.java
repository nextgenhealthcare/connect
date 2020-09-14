/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/connectors/ws")
@Tag(name = "Connector Services")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface WebServiceConnectorServletInterface extends BaseServletInterface {

    public static final String PLUGIN_POINT = "Web Service Connector Service";

    @POST
    @Path("/_cacheWsdlFromUrl")
    @Operation(summary = "Downloads the WSDL at the specified URL and caches the web service definition tree.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "cache_wsdl_from_url", ref = "../apiexamples/null_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "cache_wsdl_from_url", ref = "../apiexamples/null_json") }) })
    @MirthOperation(name = "cacheWsdlFromUrl", display = "Download and cache WSDL", type = ExecuteType.ASYNC, auditable = false)
    public Object cacheWsdlFromUrl(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @QueryParam("channelId") String channelId,
            @Param("channelName") @Parameter(description = "The name of the channel.", required = true) @QueryParam("channelName") String channelName,
            @Param("properties") 
            @RequestBody(description = "The Web Service Sender properties to use.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "ws_dispatcher_properties", ref = "../apiexamples/ws_dispatcher_properties_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "ws_dispatcher_properties", ref = "../apiexamples/ws_dispatcher_properties_json") }) })
            WebServiceDispatcherProperties properties) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_isWsdlCached")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Returns true if the definition tree for the WSDL is cached by the server.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "is_wsdl_cached", ref = "../apiexamples/boolean_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "is_wsdl_cached", ref = "../apiexamples/boolean_json") }) })
    @MirthOperation(name = "isWsdlCached", display = "Check if WSDL is cached", type = ExecuteType.ASYNC, auditable = false)
    public boolean isWsdlCached(// @formatter:off
            @Param("channelId") 
            @Parameter(description = "The ID of the channel.", required = true, schema = @Schema(description = "The ID of the channel.")) 
            @FormParam("channelId") String channelId,
            
            @Param("channelName") 
            @Parameter(description = "The name of the channel.", required = true, schema = @Schema(description = "The name of the channel.")) 
            @FormParam("channelName") String channelName,
            
            @Param("wsdlUrl") 
            @Parameter(description = "The WSDL URL to check.", required = true, schema = @Schema(description = "The WSDL URL to check.")) 
            @FormParam("wsdlUrl") String wsdlUrl,
            
            @Param("username") 
            @Parameter(description = "Username used to authenticate to the web server.", schema = @Schema(description = "Username used to authenticate to the web server.")) 
            @FormParam("username") String username,
            
            @Param(value = "password", excludeFromAudit = true) 
            @Parameter(description = "Password used to authenticate to the web server.", schema = @Schema(description = "Password used to authenticate to the web server.")) 
            @FormParam("password") String password) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_getDefinition")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Retrieves the definition service map corresponding to the specified WSDL.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "definition_service_map", ref = "../apiexamples/definition_service_map_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "definition_service_map", ref = "../apiexamples/definition_service_map_json") }) })
    @MirthOperation(name = "getDefinition", display = "Get WSDL Definition", type = ExecuteType.ASYNC, auditable = false)
    public DefinitionServiceMap getDefinition(// @formatter:off
            @Param("channelId") 
            @Parameter(description = "The ID of the channel.", required = true, schema = @Schema(description = "The ID of the channel.")) 
            @FormParam("channelId") String channelId,
            
            @Param("channelName") 
            @Parameter(description = "The name of the channel.", required = true, schema = @Schema(description = "The name of the channel.")) 
            @FormParam("channelName") String channelName,
            
            @Param("wsdlUrl") 
            @Parameter(description = "The WSDL URL to check.", required = true, schema = @Schema(description = "The WSDL URL to check.")) 
            @FormParam("wsdlUrl") String wsdlUrl,
            
            @Param("username") 
            @Parameter(description = "Username used to authenticate to the web server.", schema = @Schema(description = "Username used to authenticate to the web server.")) 
            @FormParam("username") String username,
            
            @Param(value = "password", excludeFromAudit = true) 
            @Parameter(description = "Password used to authenticate to the web server.", schema = @Schema(description = "Password used to authenticate to the web server.")) 
            @FormParam("password") String password) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_generateEnvelope")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Generate SOAP envelope for a given WSDL operation.")
    @ApiResponse(content = { @Content(mediaType = MediaType.TEXT_PLAIN, examples = {
            @ExampleObject(name = "generate_envelope", ref = "../apiexamples/generate_envelope_txt") }) })
    @MirthOperation(name = "generateEnvelope", display = "Generate WSDL operation envelope", type = ExecuteType.ASYNC, auditable = false)
    public String generateEnvelope(// @formatter:off
            @Param("channelId") 
            @Parameter(description = "The ID of the channel.", required = true, schema = @Schema(description = "The ID of the channel.")) 
            @FormParam("channelId") String channelId,
            
            @Param("channelName") 
            @Parameter(description = "The name of the channel.", required = true, schema = @Schema(description = "The name of the channel.")) 
            @FormParam("channelName") String channelName,
            
            @Param("wsdlUrl") 
            @Parameter(description = "The WSDL URL to check.", required = true, schema = @Schema(description = "The WSDL URL to check.")) 
            @FormParam("wsdlUrl") String wsdlUrl,
            
            @Param("username") 
            @Parameter(description = "Username used to authenticate to the web server.", schema = @Schema(description = "Username used to authenticate to the web server.")) 
            @FormParam("username") String username,
            
            @Param(value = "password", excludeFromAudit = true) 
            @Parameter(description = "Password used to authenticate to the web server.", schema = @Schema(description = "Password used to authenticate to the web server.")) 
            @FormParam("password") String password,
            
            @Param("service") 
            @Parameter(description = "The service name for the WSDL operation.", schema = @Schema(description = "The service name for the WSDL operation.")) 
            @FormParam("service") String service,
            
            @Param("port") 
            @Parameter(description = "The port / endpoint name for the service.", schema = @Schema(description = "The port / endpoint name for the service.")) 
            @FormParam("port") String port,
            
            @Param("operation") 
            @Parameter(description = "The name of the operation to generate an envelope for.", schema = @Schema(description = "The name of the operation to generate an envelope for.")) 
            @FormParam("operation") String operation, 
            
            @Param("buildOptional") 
            @Parameter(description = "Whether to include optional fields in the envelope.", schema = @Schema(description = "Whether to include optional fields in the envelope.")) 
            @FormParam("buildOptional") 
            boolean buildOptional) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_getSoapAction")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Retrieves the default SOAP Action for a given WSDL operation.")
    @ApiResponse(content = { @Content(mediaType = MediaType.TEXT_PLAIN, examples = {
            @ExampleObject(value = "SomeAction") }) })
    @MirthOperation(name = "getSoapAction", display = "Get SOAP Action for operation", type = ExecuteType.ASYNC, auditable = false)
    public String getSoapAction(// @formatter:off
            @Param("channelId") 
            @Parameter(description = "The ID of the channel.", required = true, schema = @Schema(description = "The ID of the channel.")) 
            @FormParam("channelId") String channelId,
            
            @Param("channelName") 
            @Parameter(description = "The name of the channel.", required = true, schema = @Schema(description = "The name of the channel.")) 
            @FormParam("channelName") String channelName,
            
            @Param("wsdlUrl") 
            @Parameter(description = "The WSDL URL to check.", required = true, schema = @Schema(description = "The WSDL URL to check.")) 
            @FormParam("wsdlUrl") String wsdlUrl,
            
            @Param("username") 
            @Parameter(description = "Username used to authenticate to the web server.", schema = @Schema(description = "Username used to authenticate to the web server.")) 
            @FormParam("username") String username,
            
            @Param(value = "password", excludeFromAudit = true) 
            @Parameter(description = "Password used to authenticate to the web server.", schema = @Schema(description = "Password used to authenticate to the web server.")) 
            @FormParam("password") String password,
            
            @Param("service") 
            @Parameter(description = "The service name for the WSDL operation.", schema = @Schema(description = "The service name for the WSDL operation.")) 
            @FormParam("service") String service,
            
            @Param("port") 
            @Parameter(description = "The port / endpoint name for the service.", schema = @Schema(description = "The port / endpoint name for the service.")) 
            @FormParam("port") String port,
            
            @Param("operation") 
            @Parameter(description = "The name of the operation to generate an envelope for.", schema = @Schema(description = "The name of the operation to generate an envelope for.")) 
            @FormParam("operation") String operation) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_testConnection")
    @Operation(summary = "Tests whether a connection can be successfully established to the destination endpoint.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "connection_test_response_ws", ref = "../apiexamples/connection_test_response_ws_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "connection_test_response_ws", ref = "../apiexamples/connection_test_response_ws_json") }) })
    @MirthOperation(name = "testConnection", display = "Test Web Service Connection", type = ExecuteType.ASYNC, auditable = false)
    public ConnectionTestResponse testConnection(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @QueryParam("channelId") String channelId,
            @Param("channelName") @Parameter(description = "The name of the channel.", required = true) @QueryParam("channelName") String channelName,
            @Param("properties") 
            @RequestBody(description = "The Web Service Sender properties to use.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "ws_dispatcher_properties", ref = "../apiexamples/ws_dispatcher_properties_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "ws_dispatcher_properties", ref = "../apiexamples/ws_dispatcher_properties_json") }) })
            WebServiceDispatcherProperties properties) throws ClientException;
    // @formatter:on
}