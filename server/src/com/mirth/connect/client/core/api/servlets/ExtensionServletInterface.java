/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api.servlets;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.io.InputStream;
import java.util.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataParam;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.Permissions;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;
import com.mirth.connect.client.core.api.Param;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.model.PluginMetaData;

@Path("/extensions")
@Tag(name = "Extensions")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface ExtensionServletInterface extends BaseServletInterface {

    // These are statically declared because extensions can choose to include them in specific permission groups.
    public static final String OPERATION_PLUGIN_PROPERTIES_GET = "getPluginProperties";
    public static final String OPERATION_PLUGIN_PROPERTIES_SET = "setPluginProperties";

    @POST
    @Path("/_install")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Installs an extension.")
    @MirthOperation(name = "installExtension", display = "Install extension", permission = Permissions.EXTENSIONS_MANAGE)
    public void installExtension(@Param("inputStream") @Parameter(description = "The extension file to upload.", schema = @Schema(description = "The extension file to upload.", type = "string", format = "binary")) @FormDataParam("file") InputStream inputStream) throws ClientException;

    @POST
    @Path("/_uninstall")
    @Operation(summary = "Uninstalls an extension.")
    @MirthOperation(name = "uninstallExtension", display = "Uninstall extension", permission = Permissions.EXTENSIONS_MANAGE)
    public void uninstallExtension(@Param("extensionPath") @RequestBody(description = "The path attribute of the extension to uninstall.", required = true, content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "extensionPath", value = "/path/to/extension") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "extensionPath", value = "/path/to/extension") }) }) String extensionPath) throws ClientException;

    @GET
    @Path("/{extensionName}")
    @Operation(summary = "Returns extension metadata by name.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "metadata", ref = "../apiexamples/connector_metadata_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "metadata", ref = "../apiexamples/connector_metadata_json") }) })
    @MirthOperation(name = "getMetaDataByName", display = "Get extension metadata by name", auditable = false)
    public MetaData getExtensionMetaData(@Param("extensionName") @Parameter(description = "The name of the extension to retrieve.", required = true) @PathParam("extensionName") String extensionName) throws ClientException;

    @GET
    @Path("/connectors")
    @Operation(summary = "Returns all active connector metadata.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "connectorMetaDataMap", ref = "../apiexamples/connector_metadata_map_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "connectorMetaDataMap", ref = "../apiexamples/connector_metadata_map_json") }) })
    @MirthOperation(name = "getConnectorMetaData", display = "Get connector metadata", auditable = false)
    public Map<String, ConnectorMetaData> getConnectorMetaData() throws ClientException;

    @GET
    @Path("/plugins")
    @Operation(summary = "Returns all active plugin metadata.")
    @MirthOperation(name = "getPluginMetaData", display = "Get plugin metadata", auditable = false)
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "pluginMetaData", ref = "../apiexamples/plugin_metadata_map_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "pluginMetaData", ref = "../apiexamples/plugin_metadata_map_json") }) })
    public Map<String, PluginMetaData> getPluginMetaData() throws ClientException;

    @GET
    @Path("/{extensionName}/enabled")
    @Operation(summary = "Returns the enabled status of an extension.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "extensionEnabled", ref = "../apiexamples/boolean_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "extensionEnabled", ref = "../apiexamples/boolean_json") }) })
    @MirthOperation(name = "isExtensionEnabled", display = "Check if extension is enabled", auditable = false)
    public boolean isExtensionEnabled(@Param("extensionName") @Parameter(description = "The name of the extension to retrieve.", required = true) @PathParam("extensionName") String extensionName) throws ClientException;

    @POST
    @Path("/{extensionName}/_setEnabled")
    @Operation(summary = "Enables or disables an extension.")
    @MirthOperation(name = "setExtensionEnabled", display = "Enable or disable an extension", permission = Permissions.EXTENSIONS_MANAGE)
    public void setExtensionEnabled(// @formatter:off
            @Param("extensionName") @Parameter(description = "The name of the extension to retrieve.", required = true) @PathParam("extensionName") String extensionName,
            @Param("enabled") @Parameter(description = "The new enabled status to set.", required = true) @QueryParam("enabled") boolean enabled) throws ClientException;
    // @formatter:on

    @GET
    @Path("/{extensionName}/properties")
    @Operation(summary = "Returns filtered properties for a specified extension.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "propertiesObject", ref = "../apiexamples/properties_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "propertiesObject", ref = "../apiexamples/properties_json") }) })
    @MirthOperation(name = OPERATION_PLUGIN_PROPERTIES_GET, display = "Get filtered plugin properties", auditable = false)
    public Properties getPluginProperties(// @formatter:off
        @Param("extensionName") @Parameter(description = "The name of the extension to retrieve.", required = true) @PathParam("extensionName") String extensionName,
        @Param("propertyKeys") @Parameter(description = "The set of properties to retrieve.", required = false) @QueryParam("propertyKeys") Set<String> propertyKeys) throws ClientException;
    // @formatter:on

    @PUT
    @Path("/{extensionName}/properties")
    @Operation(summary = "Sets properties for a specified extension.")
    @MirthOperation(name = OPERATION_PLUGIN_PROPERTIES_SET, display = "Set plugin properties")
    public void setPluginProperties(@Param("extensionName") @RequestBody(description = "The name of the extension to retrieve.", required = true) @PathParam("extensionName") String extensionName, @Param("properties") @RequestBody(description = "description", content = {
            @Content(mediaType = MediaType.APPLICATION_XML, schema = @Schema(implementation = Properties.class), examples = {
                    @ExampleObject(name = "propertiesObject", ref = "../apiexamples/properties_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Properties.class), examples = {
                    @ExampleObject(name = "propertiesObject", ref = "../apiexamples/properties_json") }) }) Properties properties, @Param("mergeProperties") @Parameter(description = "Merge or replace properties. Defaults to replace.", required = false, schema = @Schema(defaultValue = "false")) @QueryParam("mergeProperties") boolean mergeProperties) throws ClientException;
}