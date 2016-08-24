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

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.Operation.ExecuteType;
import com.mirth.connect.client.core.Permissions;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;
import com.mirth.connect.client.core.api.Param;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.model.ChannelDependency;
import com.mirth.connect.model.ChannelMetadata;
import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.model.EncryptionSettings;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.ResourceProperties;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.util.ConfigurationProperty;
import com.mirth.connect.util.ConnectionTestResponse;

@Path("/server")
@Api("Server Configuration")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface ConfigurationServletInterface extends BaseServletInterface {

    @GET
    @Path("/id")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation("Returns the server id.")
    @MirthOperation(name = "getServerId", display = "Get server ID", auditable = false)
    public String getServerId() throws ClientException;

    @GET
    @Path("/version")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation("Returns the version of the Mirth Connect server.")
    @MirthOperation(name = "getVersion", display = "Get version", auditable = false)
    public String getVersion() throws ClientException;

    @GET
    @Path("/buildDate")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation("Returns the build date of the Mirth Connect server.")
    @MirthOperation(name = "getBuildDate", display = "Get build date", auditable = false)
    public String getBuildDate() throws ClientException;

    @GET
    @Path("/status")
    @ApiOperation("Returns the status of the Mirth Connect server.")
    @MirthOperation(name = "getStatus", display = "Get status")
    public int getStatus() throws ClientException;

    @GET
    @Path("/timezone")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation("Returns the time zone of the server.")
    @MirthOperation(name = "getServerTimezone", display = "Get server timezone", auditable = false)
    public String getServerTimezone() throws ClientException;

    @GET
    @Path("/time")
    @ApiOperation("Returns the time of the server.")
    @MirthOperation(name = "getServerTime", display = "Get server time", auditable = false)
    public Calendar getServerTime() throws ClientException;

    @GET
    @Path("/jvm")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation("Returns the name of the JVM running Mirth Connect.")
    @MirthOperation(name = "getJVMName", display = "Get JVM name", auditable = false)
    public String getJVMName() throws ClientException;

    @GET
    @Path("/about")
    @ApiOperation("Returns a map of common information about the Mirth Connect server.")
    @MirthOperation(name = "getAbout", display = "Get about information", auditable = false)
    public Map<String, Object> getAbout() throws ClientException;

    @GET
    @Path("/configuration")
    @ApiOperation("Returns a ServerConfiguration object which contains all of the channels, users, alerts and properties stored on the Mirth Connect server.")
    @MirthOperation(name = "getServerConfiguration", display = "Get server configuration", permission = Permissions.SERVER_CONFIGURATION_BACKUP)
    public ServerConfiguration getServerConfiguration(// @formatter:off
            @Param("initialState") @ApiParam(value = "The initial state to set all channels in the configuration to.", allowableValues = "STARTED, PAUSED, STOPPED") @QueryParam("initialState") DeployedState initialState,
            @Param("pollingOnly") @ApiParam(value = "If true, and the initialState parameter is set, only channels with polling source connectors will have their initial states overwritten in the returned server configuration.") @QueryParam("pollingOnly") boolean pollingOnly) throws ClientException;
    // @formatter:on

    @PUT
    @Path("/configuration")
    @ApiOperation("Updates all of the channels, alerts and properties stored on the Mirth Connect server.")
    @MirthOperation(name = "setServerConfiguration", display = "Set server configuration", permission = Permissions.SERVER_CONFIGURATION_RESTORE, type = ExecuteType.ASYNC)
    public void setServerConfiguration(// @formatter:off
            @Param("serverConfiguration") @ApiParam(value = "The ServerConfiguration object containing all channels, users, alerts, and properties to update.", required = true) ServerConfiguration serverConfiguration,
            @Param("deploy") @ApiParam(value = "If true, all enabled channels will be deployed after the configuration is restored.", defaultValue = "false") @QueryParam(value = "deploy") boolean deploy) throws ClientException;
    // @formatter:on

    @GET
    @Path("/charsets")
    @ApiOperation("Returns a List of all of the charset encodings supported by the server.")
    @MirthOperation(name = "getAvailableCharsetEncodings", display = "Get available charset encodings", auditable = false)
    public List<String> getAvailableCharsetEncodings() throws ClientException;

    @GET
    @Path("/settings")
    @ApiOperation("Returns a ServerSettings object with all server settings.")
    @MirthOperation(name = "getServerSettings", display = "Get server settings", permission = Permissions.SERVER_SETTINGS_VIEW, auditable = false)
    public ServerSettings getServerSettings() throws ClientException;

    @PUT
    @Path("/settings")
    @ApiOperation("Updates the server configuration settings.")
    @MirthOperation(name = "setServerSettings", display = "Set server settings", permission = Permissions.SERVER_SETTINGS_EDIT)
    public void setServerSettings(@Param("settings") @ApiParam(value = "The ServerSettings object containing all of the settings to update.", required = true) ServerSettings settings) throws ClientException;

    @GET
    @Path("/encryption")
    @ApiOperation("Returns an EncryptionSettings object with all encryption settings.")
    @MirthOperation(name = "getEncryptionSettings", display = "Get encryption settings")
    public EncryptionSettings getEncryptionSettings() throws ClientException;

    @POST
    @Path("/_testEmail")
    @ApiOperation("Sends a test e-mail.")
    @MirthOperation(name = "sendTestEmail", display = "Send Test Email", permission = Permissions.SERVER_SEND_TEST_EMAIL)
    public ConnectionTestResponse sendTestEmail(@Param("properties") @ApiParam(value = "Contains all properties needed to send the e-mail. Properties include: port, encryption, host, timeout, authentication, username, password, toAddress, fromAddress", required = true) Properties properties) throws ClientException;

    @GET
    @Path("/updateSettings")
    @ApiOperation("Returns an UpdateSettings object with all update settings.")
    @MirthOperation(name = "getUpdateSettings", display = "Get update settings", auditable = false)
    public UpdateSettings getUpdateSettings() throws ClientException;

    @PUT
    @Path("/updateSettings")
    @ApiOperation("Updates the update settings.")
    @MirthOperation(name = "setUpdateSettings", display = "Set update settings", auditable = false)
    public void setUpdateSettings(@Param("settings") @ApiParam(value = "The UpdateSettings object containing all of the settings to update.", required = true) UpdateSettings settings) throws ClientException;

    @POST
    @Path("/_generateGUID")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation("Returns a globally unique id.")
    @MirthOperation(name = "getGuid", display = "Get GUID", auditable = false)
    public String getGuid() throws ClientException;

    @GET
    @Path("/globalScripts")
    @ApiOperation("Returns a map containing all of the global scripts.")
    @MirthOperation(name = "getGlobalScripts", display = "Get global scripts", permission = Permissions.GLOBAL_SCRIPTS_VIEW)
    public Map<String, String> getGlobalScripts() throws ClientException;

    @PUT
    @Path("/globalScripts")
    @ApiOperation("Updates all of the global scripts.")
    @MirthOperation(name = "setGlobalScripts", display = "Set global scripts", permission = Permissions.GLOBAL_SCRIPTS_EDIT)
    public void setGlobalScripts(@Param("scripts") @ApiParam(value = "The map of global scripts to update with. Script keys: " + ScriptController.DEPLOY_SCRIPT_KEY + ", " + ScriptController.UNDEPLOY_SCRIPT_KEY + ", " + ScriptController.PREPROCESSOR_SCRIPT_KEY + ", " + ScriptController.POSTPROCESSOR_SCRIPT_KEY, required = true) Map<String, String> scripts) throws ClientException;

    @GET
    @Path("/configurationMap")
    @ApiOperation("Returns all entries in the configuration map.")
    @MirthOperation(name = "getConfigurationMap", display = "Get configuration map", permission = Permissions.CONFIGURATION_MAP_VIEW)
    public Map<String, ConfigurationProperty> getConfigurationMap() throws ClientException;

    @PUT
    @Path("/configurationMap")
    @ApiOperation("Updates all entries in the configuration map.")
    @MirthOperation(name = "setConfigurationMap", display = "Set configuration map", permission = Permissions.CONFIGURATION_MAP_EDIT)
    public void setConfigurationMap(@Param("map") @ApiParam(value = "The new configuration map to update with.", required = true) Map<String, ConfigurationProperty> map) throws ClientException;

    @GET
    @Path("/databaseDrivers")
    @ApiOperation("Returns the database driver list.")
    @MirthOperation(name = "getDatabaseDrivers", display = "Get database drivers", auditable = false)
    public List<DriverInfo> getDatabaseDrivers() throws ClientException;

    @GET
    @Path("/passwordRequirements")
    @ApiOperation("Returns all password requirements for the server.")
    @MirthOperation(name = "getPasswordRequirements", display = "Get password requirements")
    public PasswordRequirements getPasswordRequirements() throws ClientException;

    @GET
    @Path("/resources")
    @ApiOperation("Returns all resources for the server.")
    @MirthOperation(name = "getResources", display = "Get resources", permission = Permissions.RESOURCES_VIEW, type = ExecuteType.ASYNC)
    public List<ResourceProperties> getResources() throws ClientException;

    @PUT
    @Path("/resources")
    @ApiOperation("Updates all resources for the server.")
    @MirthOperation(name = "setResources", display = "Set resources", permission = Permissions.RESOURCES_EDIT, type = ExecuteType.ASYNC)
    public void setResources(@Param("resources") @ApiParam(value = "The new list of resource properties to update with.", required = true) List<ResourceProperties> resources) throws ClientException;

    @POST
    @Path("/resources/{resourceId}/_reload")
    @ApiOperation("Reloads a resource and all libraries associated with it.")
    @MirthOperation(name = "reloadResource", display = "Reload resource", permission = Permissions.RESOURCES_RELOAD, type = ExecuteType.ASYNC)
    public void reloadResource(@Param("resourceId") @ApiParam(value = "The unique ID of the resource to reload.", required = true) @PathParam("resourceId") String resourceId) throws ClientException;

    @GET
    @Path("/channelDependencies")
    @ApiOperation("Returns all channel dependencies for the server.")
    @MirthOperation(name = "getChannelDependencies", display = "Get channel dependencies", auditable = false)
    public Set<ChannelDependency> getChannelDependencies() throws ClientException;

    @PUT
    @Path("/channelDependencies")
    @ApiOperation("Updates all channel dependencies for the server.")
    @MirthOperation(name = "setChannelDependencies", display = "Set channel dependencies", permission = Permissions.CHANNELS_MANAGE)
    public void setChannelDependencies(Set<ChannelDependency> dependencies) throws ClientException;

    @GET
    @Path("/channelMetadata")
    @ApiOperation("Returns all channel metadata for the server.")
    @MirthOperation(name = "getChannelMetadata", display = "Get channel metadata", auditable = false)
    public Map<String, ChannelMetadata> getChannelMetadata() throws ClientException;

    @PUT
    @Path("/channelMetadata")
    @ApiOperation("Updates all channel metadata for the server.")
    @MirthOperation(name = "setChannelMetadata", display = "Set channel metadata", permission = Permissions.CHANNELS_MANAGE)
    public void setChannelMetadata(Map<String, ChannelMetadata> metadata) throws ClientException;

    @GET
    @Path("/protocolsAndCipherSuites")
    @ApiOperation("Returns a map containing all supported and enabled TLS protocols and cipher suites.")
    @MirthOperation(name = "getProtocolsAndCipherSuites", display = "Get protocols and cipher suites", type = ExecuteType.ASYNC, auditable = false)
    public Map<String, String[]> getProtocolsAndCipherSuites() throws ClientException;
}