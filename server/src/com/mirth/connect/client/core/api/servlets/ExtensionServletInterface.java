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

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

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
@Api("Extensions")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface ExtensionServletInterface extends BaseServletInterface {

    // These are statically declared because extensions can choose to include them in specific permission groups.
    public static final String OPERATION_PLUGIN_PROPERTIES_GET = "getPluginProperties";
    public static final String OPERATION_PLUGIN_PROPERTIES_SET = "setPluginProperties";

    @POST
    @Path("/_install")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation("Installs an extension.")
    @MirthOperation(name = "installExtension", display = "Install extension", permission = Permissions.EXTENSIONS_MANAGE)
    public void installExtension(@Param("inputStream") @ApiParam(value = "The extension file to upload.") @FormDataParam("file") InputStream inputStream) throws ClientException;

    @POST
    @Path("/_uninstall")
    @ApiOperation("Uninstalls an extension.")
    @MirthOperation(name = "uninstallExtension", display = "Uninstall extension", permission = Permissions.EXTENSIONS_MANAGE)
    public void uninstallExtension(@Param("extensionPath") @ApiParam(value = "The path attribute of the extension to uninstall.", required = true) String extensionPath) throws ClientException;

    @GET
    @Path("/{extensionName}")
    @ApiOperation("Returns extension metadata by name.")
    @MirthOperation(name = "getMetaDataByName", display = "Get extension metadata by name", auditable = false)
    public MetaData getExtensionMetaData(@Param("extensionName") @ApiParam(value = "The name of the extension to retrieve.", required = true) @PathParam("extensionName") String extensionName) throws ClientException;

    @GET
    @Path("/connectors")
    @ApiOperation("Returns all active connector metadata.")
    @MirthOperation(name = "getConnectorMetaData", display = "Get connector metadata", auditable = false)
    public Map<String, ConnectorMetaData> getConnectorMetaData() throws ClientException;

    @GET
    @Path("/plugins")
    @ApiOperation("Returns all active plugin metadata.")
    @MirthOperation(name = "getPluginMetaData", display = "Get plugin metadata", auditable = false)
    public Map<String, PluginMetaData> getPluginMetaData() throws ClientException;

    @GET
    @Path("/{extensionName}/enabled")
    @ApiOperation("Returns the enabled status of an extension.")
    @MirthOperation(name = "isExtensionEnabled", display = "Check if extension is enabled", auditable = false)
    public boolean isExtensionEnabled(@Param("extensionName") @ApiParam(value = "The name of the extension to retrieve.", required = true) @PathParam("extensionName") String extensionName) throws ClientException;

    @POST
    @Path("/{extensionName}/_setEnabled")
    @ApiOperation("Enables or disables an extension.")
    @MirthOperation(name = "setExtensionEnabled", display = "Enable or disable an extension", permission = Permissions.EXTENSIONS_MANAGE)
    public void setExtensionEnabled(// @formatter:off
            @Param("extensionName") @ApiParam(value = "The name of the extension to retrieve.", required = true) @PathParam("extensionName") String extensionName,
            @Param("enabled") @ApiParam(value = "The new enabled status to set.", required = true) @QueryParam("enabled") boolean enabled) throws ClientException;
    // @formatter:on

    @GET
    @Path("/{extensionName}/properties")
    @ApiOperation("Returns properties for a specified extension.")
    @MirthOperation(name = OPERATION_PLUGIN_PROPERTIES_GET, display = "Get plugin properties", auditable = false)
    public Properties getPluginProperties(@Param("extensionName") @ApiParam(value = "The name of the extension to retrieve.", required = true) @PathParam("extensionName") String extensionName) throws ClientException;

    @PUT
    @Path("/{extensionName}/properties")
    @ApiOperation("Sets properties for a specified extension.")
    @MirthOperation(name = OPERATION_PLUGIN_PROPERTIES_SET, display = "Set plugin properties")
    public void setPluginProperties(// @formatter:off
            @Param("extensionName") @ApiParam(value = "The name of the extension to retrieve.", required = true) @PathParam("extensionName") String extensionName,
            @Param("properties") @ApiParam(value = "The new properties to set.", required = true) Properties properties) throws ClientException;
    // @formatter:on
}