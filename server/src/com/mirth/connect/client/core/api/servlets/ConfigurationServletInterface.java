/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api.servlets;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

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
import com.mirth.connect.model.ChannelTag;
import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.model.EncryptionSettings;
import com.mirth.connect.model.LicenseInfo;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.ResourceProperties;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.util.ConfigurationProperty;
import com.mirth.connect.util.ConnectionTestResponse;

@Path("/server")
@Tag(name = "Server Configuration")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface ConfigurationServletInterface extends BaseServletInterface {

    @GET
    @Path("/id")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Returns the server id.")
    @MirthOperation(name = "getServerId", display = "Get server ID", auditable = false)
    public String getServerId() throws ClientException;

    @GET
    @Path("/version")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Returns the version of the Mirth Connect server.")
    @MirthOperation(name = "getVersion", display = "Get version", auditable = false)
    public String getVersion() throws ClientException;

    @GET
    @Path("/buildDate")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Returns the build date of the Mirth Connect server.")
    @MirthOperation(name = "getBuildDate", display = "Get build date", auditable = false)
    public String getBuildDate() throws ClientException;

    @GET
    @Path("/status")
    @Operation(summary = "Returns the status of the Mirth Connect server.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "status", ref = "../apiexamples/integer_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "status", ref = "../apiexamples/integer_json") }) })
    @MirthOperation(name = "getStatus", display = "Get status")
    public int getStatus() throws ClientException;

    @GET
    @Path("/timezone")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Returns the time zone of the server.")
    @MirthOperation(name = "getServerTimezone", display = "Get server timezone", auditable = false)
    public String getServerTimezone() throws ClientException;

    @GET
    @Path("/time")
    @Operation(summary = "Returns the time of the server.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "serverTime", ref = "../apiexamples/calendar_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "serverTime", ref = "../apiexamples/calendar_json") }) })
    @MirthOperation(name = "getServerTime", display = "Get server time", auditable = false)
    public Calendar getServerTime() throws ClientException;

    @GET
    @Path("/jvm")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Returns the name of the JVM running Mirth Connect.")
    @MirthOperation(name = "getJVMName", display = "Get JVM name", auditable = false)
    public String getJVMName() throws ClientException;

    @GET
    @Path("/about")
    @Operation(summary = "Returns a map of common information about the Mirth Connect server.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "aboutMap", ref = "../apiexamples/generic_map_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "aboutMap", ref = "../apiexamples/generic_map_json") }) })
    @MirthOperation(name = "getAbout", display = "Get about information", auditable = false)
    public Map<String, Object> getAbout() throws ClientException;

    @GET
    @Path("/configuration")
    @Operation(summary = "Returns a ServerConfiguration object which contains all of the channels, alerts, configuration map, and properties stored on the Mirth Connect server.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "serverConfiguration", ref = "../apiexamples/server_configuration_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "serverConfiguration", ref = "../apiexamples/server_configuration_json") }) })
    @MirthOperation(name = "getServerConfiguration", display = "Get server configuration", permission = Permissions.SERVER_CONFIGURATION_BACKUP)
    public ServerConfiguration getServerConfiguration(// @formatter:off
            @Param("initialState") @Parameter(description = "The initial state to set all channels in the configuration to.", schema = @Schema(allowableValues = {"STARTED", "PAUSED", "STOPPED"}, type = "string")) @QueryParam("initialState") DeployedState initialState,
            @Param("pollingOnly") @Parameter(description = "If true, and the initialState parameter is set, only channels with polling source connectors will have their initial states overwritten in the returned server configuration.") @QueryParam("pollingOnly") boolean pollingOnly,
            @Param("disableAlerts") @Parameter(description = "If true, all alerts returned in the server configuration will be disabled.") @QueryParam("disableAlerts") boolean disableAlerts) throws ClientException;
    // @formatter:on

    @PUT
    @Path("/configuration")
    @Operation(summary = "Updates all of the channels, alerts and properties stored on the Mirth Connect server.")
    @MirthOperation(name = "setServerConfiguration", display = "Set server configuration", permission = Permissions.SERVER_CONFIGURATION_RESTORE, type = ExecuteType.ASYNC)
    public void setServerConfiguration(// @formatter:off
            @Param("serverConfiguration") @RequestBody(description = "The ServerConfiguration object containing all channels, users, alerts, and properties to update.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "serverConfiguration", ref = "../apiexamples/server_configuration_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "serverConfiguration", ref = "../apiexamples/server_configuration_json") }) }) ServerConfiguration serverConfiguration,
            @Param("deploy") @Parameter(description = "If true, all enabled channels will be deployed after the configuration is restored.", schema = @Schema(defaultValue = "false")) @QueryParam(value = "deploy") boolean deploy,
            @Param("overwriteConfigMap") @Parameter(description = "If true, overwrite the Configuration Map") @QueryParam(value = "overwriteConfigMap") boolean overwriteConfigMap) throws ClientException;
    // @formatter:on

    @GET
    @Path("/charsets")
    @Operation(summary = "Returns a List of all of the charset encodings supported by the server.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "charsetEncodings", ref = "../apiexamples/charset_encoding_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "charsetEncodings", ref = "../apiexamples/charset_encoding_list_json") }) })
    @MirthOperation(name = "getAvailableCharsetEncodings", display = "Get available charset encodings", auditable = false)
    public List<String> getAvailableCharsetEncodings() throws ClientException;

    @GET
    @Path("/settings")
    @Operation(summary = "Returns a ServerSettings object with all server settings.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "serverSettings", ref = "../apiexamples/server_settings_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "serverSettings", ref = "../apiexamples/server_settings_json") }) })
    @MirthOperation(name = "getServerSettings", display = "Get server settings", permission = Permissions.SERVER_SETTINGS_VIEW, auditable = false)
    public ServerSettings getServerSettings() throws ClientException;

    @PUT
    @Path("/settings")
    @Operation(summary = "Updates the server configuration settings.")
    @MirthOperation(name = "setServerSettings", display = "Set server settings", permission = Permissions.SERVER_SETTINGS_EDIT)
    public void setServerSettings(@Param("settings") @RequestBody(description = "The ServerSettings object containing all of the settings to update.", required = true, content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "serverSettings", ref = "../apiexamples/server_settings_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "serverSettings", ref = "../apiexamples/server_settings_json") }) }) ServerSettings settings) throws ClientException;

    @GET
    @Path("/encryption")
    @Operation(summary = "Returns an EncryptionSettings object with all encryption settings.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "encryptionSettings", ref = "../apiexamples/encryption_settings_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "encryptionSettings", ref = "../apiexamples/encryption_settings_json") }) })
    @MirthOperation(name = "getEncryptionSettings", display = "Get encryption settings")
    public EncryptionSettings getEncryptionSettings() throws ClientException;

    @POST
    @Path("/_testEmail")
    @Operation(summary = "Sends a test e-mail.")
    @MirthOperation(name = "sendTestEmail", display = "Send Test Email", permission = Permissions.SERVER_SEND_TEST_EMAIL)
    public ConnectionTestResponse sendTestEmail(@Param("properties") @RequestBody(description = "Contains all properties needed to send the e-mail. Properties include: port, encryption, host, timeout, authentication, username, password, toAddress, fromAddress", required = true, content = {
            @Content(mediaType = MediaType.APPLICATION_XML, schema = @Schema(implementation = Properties.class), examples = {
                    @ExampleObject(name = "propertiesObject", ref = "../apiexamples/properties_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Properties.class), examples = {
                    @ExampleObject(name = "propertiesObject", ref = "../apiexamples/properties_json") }) }) Properties properties) throws ClientException;

    @GET
    @Path("/updateSettings")
    @Operation(summary = "Returns an UpdateSettings object with all update settings.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "updateSettings", ref = "../apiexamples/update_settings_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "updateSettings", ref = "../apiexamples/update_settings_json") }) })
    @MirthOperation(name = "getUpdateSettings", display = "Get update settings", auditable = false)
    public UpdateSettings getUpdateSettings() throws ClientException;

    @PUT
    @Path("/updateSettings")
    @Operation(summary = "Updates the update settings.")
    @MirthOperation(name = "setUpdateSettings", display = "Set update settings", auditable = false)
    public void setUpdateSettings(@Param("settings") @RequestBody(description = "The UpdateSettings object containing all of the settings to update.", required = true, content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "updateSetings", ref = "../apiexamples/update_settings_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "updateSettings", ref = "../apiexamples/update_settings_json") }) }) UpdateSettings settings) throws ClientException;

    @GET
    @Path("/licenseInfo")
    @Operation(summary = "Returns a LicenseInfo object with the expiration date and other information.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "licenseInfo", ref = "../apiexamples/license_info_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "licenseInfo", ref = "../apiexamples/license_info_json") }) })
    @MirthOperation(name = "getLicenseInfo", display = "Get license info", auditable = false)
    public LicenseInfo getLicenseInfo() throws ClientException;

    @POST
    @Path("/_generateGUID")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Returns a globally unique id.")
    @MirthOperation(name = "getGuid", display = "Get GUID", auditable = false)
    public String getGuid() throws ClientException;

    @GET
    @Path("/globalScripts")
    @Operation(summary = "Returns a map containing all of the global scripts.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "globalScripts", ref = "../apiexamples/global_scripts_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "globalScripts", ref = "../apiexamples/global_scripts_json") }) })
    @MirthOperation(name = "getGlobalScripts", display = "Get global scripts", permission = Permissions.GLOBAL_SCRIPTS_VIEW)
    public Map<String, String> getGlobalScripts() throws ClientException;

    @PUT
    @Path("/globalScripts")
    @Operation(summary = "Updates all of the global scripts.")
    @MirthOperation(name = "setGlobalScripts", display = "Set global scripts", permission = Permissions.GLOBAL_SCRIPTS_EDIT)
    public void setGlobalScripts(@Param("scripts") @RequestBody(description = "The map of global scripts to update with. Script keys: " + ScriptController.DEPLOY_SCRIPT_KEY + ", " + ScriptController.UNDEPLOY_SCRIPT_KEY + ", " + ScriptController.PREPROCESSOR_SCRIPT_KEY + ", " + ScriptController.POSTPROCESSOR_SCRIPT_KEY, required = true, content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "globalScripts", ref = "../apiexamples/global_scripts_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "globalScripts", ref = "../apiexamples/global_scripts_json") }) }) Map<String, String> scripts) throws ClientException;

    @GET
    @Path("/configurationMap")
    @Operation(summary = "Returns all entries in the configuration map.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "configurationMap", ref = "../apiexamples/configuration_map_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "configurationMap", ref = "../apiexamples/configuration_map_json") }) })
    @MirthOperation(name = "getConfigurationMap", display = "Get configuration map", permission = Permissions.CONFIGURATION_MAP_VIEW)
    public Map<String, ConfigurationProperty> getConfigurationMap() throws ClientException;

    @PUT
    @Path("/configurationMap")
    @Operation(summary = "Updates all entries in the configuration map.")
    @MirthOperation(name = "setConfigurationMap", display = "Set configuration map", permission = Permissions.CONFIGURATION_MAP_EDIT)
    public void setConfigurationMap(@Param("map") @RequestBody(description = "The new configuration map to update with.", required = true, content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "configurationMap", ref = "../apiexamples/configuration_map_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "configurationMap", ref = "../apiexamples/configuration_map_json") }) }) Map<String, ConfigurationProperty> map) throws ClientException;

    @GET
    @Path("/databaseDrivers")
    @Operation(summary = "Returns the database driver list.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "driverInfoList", ref = "../apiexamples/driver_info_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "driverInfoList", ref = "../apiexamples/driver_info_list_json") }) })
    @MirthOperation(name = "getDatabaseDrivers", display = "Get database drivers", auditable = false, type = ExecuteType.ASYNC)
    public List<DriverInfo> getDatabaseDrivers() throws ClientException;

    @PUT
    @Path("/databaseDrivers")
    @Operation(summary = "Updates the list of database drivers.")
    @MirthOperation(name = "setDatabaseDrivers", display = "Update database drivers", permission = Permissions.DATABASE_DRIVERS_EDIT)
    public void setDatabaseDrivers(@Param("drivers") @RequestBody(description = "The new list of database drivers to update.", required = true, content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "driverInfoList", ref = "../apiexamples/driver_info_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "driverInfoList", ref = "../apiexamples/driver_info_list_json") }) }) List<DriverInfo> drivers) throws ClientException;

    @GET
    @Path("/passwordRequirements")
    @Operation(summary = "Returns all password requirements for the server.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "passwordRequirements", ref = "../apiexamples/password_requirements_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "passwordRequirements", ref = "../apiexamples/password_requirements_json") }) })
    @MirthOperation(name = "getPasswordRequirements", display = "Get password requirements")
    public PasswordRequirements getPasswordRequirements() throws ClientException;

    @GET
    @Path("/resources")
    @Operation(summary = "Returns all resources for the server.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "resources", ref = "../apiexamples/resource_properties_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "resources", ref = "../apiexamples/resource_properties_list_json") }) })
    @MirthOperation(name = "getResources", display = "Get resources", permission = Permissions.RESOURCES_VIEW, type = ExecuteType.ASYNC)
    public List<ResourceProperties> getResources() throws ClientException;

    @PUT
    @Path("/resources")
    @Operation(summary = "Updates all resources for the server.")
    @MirthOperation(name = "setResources", display = "Set resources", permission = Permissions.RESOURCES_EDIT, type = ExecuteType.ASYNC)
    public void setResources(@Param("resources") @RequestBody(description = "The new list of resource properties to update with.", required = true, content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "resources", ref = "../apiexamples/resource_properties_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "resources", ref = "../apiexamples/resource_properties_list_json") }) }) List<ResourceProperties> resources) throws ClientException;

    @POST
    @Path("/resources/{resourceId}/_reload")
    @Operation(summary = "Reloads a resource and all libraries associated with it.")
    @MirthOperation(name = "reloadResource", display = "Reload resource", permission = Permissions.RESOURCES_RELOAD, type = ExecuteType.ASYNC)
    public void reloadResource(@Param("resourceId") @Parameter(description = "The unique ID of the resource to reload.", required = true) @PathParam("resourceId") String resourceId) throws ClientException;

    @GET
    @Path("/channelDependencies")
    @Operation(summary = "Returns all channel dependencies for the server.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "channelDependencies", ref = "../apiexamples/channel_dependency_set_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "channelDependencies", ref = "../apiexamples/channel_dependency_set_json") }) })
    @MirthOperation(name = "getChannelDependencies", display = "Get channel dependencies", auditable = false)
    public Set<ChannelDependency> getChannelDependencies() throws ClientException;

    @PUT
    @Path("/channelDependencies")
    @Operation(summary = "Updates all channel dependencies for the server.")
    @MirthOperation(name = "setChannelDependencies", display = "Set channel dependencies", permission = Permissions.CHANNELS_MANAGE)
    public void setChannelDependencies(@Param("dependencies") @RequestBody(description = "The channel dependencies to set.", required = true, content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "channelDependencies", ref = "../apiexamples/channel_dependency_set_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "channelDependencies", ref = "../apiexamples/channel_dependency_set_json") }) }) Set<ChannelDependency> dependencies) throws ClientException;

    @GET
    @Path("/channelMetadata")
    @Operation(summary = "Returns all channel metadata for the server.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "channelMetadata", ref = "../apiexamples/channel_metadata_map_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "channelMetadata", ref = "../apiexamples/channel_metadata_map_json") }) })
    @MirthOperation(name = "getChannelMetadata", display = "Get channel metadata", auditable = false)
    public Map<String, ChannelMetadata> getChannelMetadata() throws ClientException;

    @PUT
    @Path("/channelMetadata")
    @Operation(summary = "Updates all channel metadata for the server.")
    @MirthOperation(name = "setChannelMetadata", display = "Set channel metadata", permission = Permissions.CHANNELS_MANAGE)
    public void setChannelMetadata(@Param("metadata") @RequestBody(description = "The map of channel metadata to set.", required = true, content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "channelMetadata", ref = "../apiexamples/channel_metadata_map_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "channelMetadata", ref = "../apiexamples/channel_metadata_map_json") }) }) Map<String, ChannelMetadata> metadata) throws ClientException;

    @GET
    @Path("/protocolsAndCipherSuites")
    @Operation(summary = "Returns a map containing all supported and enabled TLS protocols and cipher suites.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "protocolsAndCipherSuites", ref = "../apiexamples/protocols_and_cipher_suites_map_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "protocolsAndCipherSuites", ref = "../apiexamples/protocols_and_cipher_suites_map_json") }) })
    @MirthOperation(name = "getProtocolsAndCipherSuites", display = "Get protocols and cipher suites", type = ExecuteType.ASYNC, auditable = false)
    public Map<String, String[]> getProtocolsAndCipherSuites() throws ClientException;

    @GET
    @Path("/channelTags")
    @Operation(summary = "Returns a set containing all channel tags for the server.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "channelTags", ref = "../apiexamples/channel_tag_set_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "channelTags", ref = "../apiexamples/channel_tag_set_json") }) })
    @MirthOperation(name = "getChannelTags", display = "Get channel tags", permission = Permissions.TAGS_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public Set<ChannelTag> getChannelTags() throws ClientException;

    @PUT
    @Path("/channelTags")
    @Operation(summary = "Updates all channel tags.")
    @MirthOperation(name = "updateChannelTags", display = "Update channel tags", permission = Permissions.TAGS_MANAGE)
    public void setChannelTags(@Param("channelTags") @RequestBody(description = "The channel tags to set.", required = true, content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "channelTags", ref = "../apiexamples/channel_tag_set_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "channelTags", ref = "../apiexamples/channel_tag_set_json") }) }) Set<ChannelTag> channelTags) throws ClientException;

    @GET
    @Path("/rhinoLanguageVersion")
    @Operation(summary = "Returns the language version that the Rhino engine should use.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "rhinoLanguageVersion", ref = "../apiexamples/integer_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "rhinoLanguageVersion", ref = "../apiexamples/integer_json") }) })
    @MirthOperation(name = "getRhinoLanguageVersion", display = "Get rhino language version", type = ExecuteType.ASYNC, auditable = false)
    public int getRhinoLanguageVersion() throws ClientException;
}