/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jms;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.LinkedHashMap;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.Operation.ExecuteType;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;
import com.mirth.connect.client.core.api.Param;

@Path("/connectors/jms")
@Tag(name = "Connector Services")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public interface JmsConnectorServletInterface extends BaseServletInterface {

    public static final String PLUGIN_POINT = "JMS Connector Service";

    @GET
    @Path("/templates")
    @Operation(summary="Retrieves JMS connector settings templates.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "jms_connector_properties_map", ref = "../apiexamples/jms_connector_properties_map_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "jms_connector_properties_map", ref = "../apiexamples/jms_connector_properties_map_json") }) })
    @MirthOperation(name = "getTemplates", display = "Get JMS Templates", type = ExecuteType.ASYNC, auditable = false)
    public LinkedHashMap<String, JmsConnectorProperties> getTemplates() throws ClientException;

    @GET
    @Path("/templates/{templateName}")
    @Operation(summary="Retrieves a single JMS connector settings template.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "jms_connector_properties", ref = "../apiexamples/jms_connector_properties_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "jms_connector_properties", ref = "../apiexamples/jms_connector_properties_json") }) })
    @MirthOperation(name = "getTemplate", display = "Get JMS Template", type = ExecuteType.ASYNC, auditable = false)
    public JmsConnectorProperties getTemplate(@Param("templateName") @Parameter(description = "The name of the template.", required = true) @PathParam("templateName") String templateName) throws ClientException;

    @PUT
    @Path("/templates/{templateName}")
    @Operation(summary="Creates or updates a JMS connector settings template.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "jms_template_name_set", ref = "../apiexamples/jms_template_name_set_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "jms_template_name_set", ref = "../apiexamples/jms_template_name_set_json") }) })
    @MirthOperation(name = "saveTemplate", display = "Save JMS Template", type = ExecuteType.ASYNC, auditable = false)
    public Set<String> saveTemplate(// @formatter:off
            @Param("templateName") @Parameter(description = "The name of the template.", required = true) @PathParam("templateName") String templateName,
            @Param("properties") 
            @RequestBody(description = "The JMS connector properties to save.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "jms_connector_properties", ref = "../apiexamples/jms_connector_properties_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "jms_connector_properties", ref = "../apiexamples/jms_connector_properties_json") }) })
            JmsConnectorProperties properties) throws ClientException;
    // @formatter:on

    @DELETE
    @Path("/templates/{templateName}")
    @Operation(summary="Creates or updates a JMS connector settings template.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "jms_template_name_set", ref = "../apiexamples/jms_template_name_set_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "jms_template_name_set", ref = "../apiexamples/jms_template_name_set_json") }) })
    @MirthOperation(name = "saveTemplate", display = "Save JMS Template", type = ExecuteType.ASYNC, auditable = false)
    public Set<String> deleteTemplate(@Param("templateName") @Parameter(description = "The name of the template.", required = true) @PathParam("templateName") String templateName) throws ClientException;
}