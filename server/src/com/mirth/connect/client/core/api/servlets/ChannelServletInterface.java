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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
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
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelHeader;
import com.mirth.connect.model.ChannelSummary;

@Path("/channels")
@Tag(name = "Channels")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface ChannelServletInterface extends BaseServletInterface {

	@POST
	@Path("/")
	@Operation(summary = "Creates a new channel.")
	@MirthOperation(name = "createChannel", display = "Create channel", permission = Permissions.CHANNELS_MANAGE)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	public boolean createChannel(
			@Param("channel") @RequestBody(description = "The Channel object to create.", required = true, content = {
					@Content(mediaType = MediaType.APPLICATION_XML, schema = @Schema(implementation = Channel.class), examples = {
							@ExampleObject(name = "channel", ref = "../apiexamples/channel_xml") }),
					@Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Channel.class), examples = {
							@ExampleObject(name = "channel", ref = "../apiexamples/channel_json") }) }) Channel channel)
			throws ClientException;

	@GET
	@Path("/")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Operation(summary = "Retrieve a list of all channels, or multiple channels by ID.")
	@ApiResponse(content = {
			@Content(mediaType = MediaType.APPLICATION_XML, examples = {
					@ExampleObject(name = "channelList", ref = "../apiexamples/channel_list_xml") }),
			@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
					@ExampleObject(name = "channelList", ref = "../apiexamples/channel_list_json") }) })
	@MirthOperation(name = "getChannels", display = "Get channels", permission = Permissions.CHANNELS_VIEW)
	public List<Channel> getChannels(
			@Param("channelIds") @Parameter(description = "The IDs of the channels to retrieve. If absent, all channels will be retrieved.") @QueryParam("channelId") Set<String> channelIds,
			@Param("pollingOnly") @Parameter(description = "If true, only channels with polling source connectors will be returned.") @QueryParam("pollingOnly") boolean pollingOnly,
			@Param("includeCodeTemplateLibraries") @Parameter(description = "If true, code template libraries will be included in the channel.") @QueryParam("includeCodeTemplateLibraries") boolean includeCodeTemplateLibraries)
			throws ClientException;

	@POST
	@Path("/_getChannels")
	@Operation(summary = "Retrieve a list of all channels, or multiple channels by ID. This is a POST request alternative to GET /channels that may be used when there are too many channel IDs to include in the query parameters.")
	@ApiResponse(content = {
			@Content(mediaType = MediaType.APPLICATION_XML, examples = {
					@ExampleObject(name = "channel", ref = "../apiexamples/channel_list_xml") }),
			@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
					@ExampleObject(name = "channel", ref = "../apiexamples/channel_list_json") }) })
	@MirthOperation(name = "getChannels", display = "Get channels", permission = Permissions.CHANNELS_VIEW)
	public List<Channel> getChannelsPost(
			@Param("channelIds") @RequestBody(description = "The IDs of the channels to retrieve. If absent, all channels will be retrieved.", content = {
					@Content(mediaType = MediaType.APPLICATION_XML, examples = {
							@ExampleObject(name = "channelIds", ref = "../apiexamples/guid_set_xml") }),
					@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
							@ExampleObject(name = "channelIds", ref = "../apiexamples/guid_set_json") }) }) Set<String> channelIds,
			@Param("pollingOnly") @Parameter(description = "If true, only channels with polling source connectors will be returned.") @QueryParam("pollingOnly") boolean pollingOnly,
			@Param("includeCodeTemplateLibraries") @Parameter(description = "If true, code template libraries will be included in the channel.") @QueryParam("includeCodeTemplateLibraries") boolean includeCodeTemplateLibraries)
			throws ClientException;

	@GET
	@Path("/{channelId}")
	@Operation(summary = "Retrieve a single channel by ID.")
	@ApiResponse(content = {
			@Content(mediaType = MediaType.APPLICATION_XML, examples = {
					@ExampleObject(name = "channel", ref = "../apiexamples/channel_xml") }),
			@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
					@ExampleObject(name = "channel", ref = "../apiexamples/channel_json") }) })
	@MirthOperation(name = "getChannel", display = "Get channel", permission = Permissions.CHANNELS_VIEW)
	public Channel getChannel(
			@Param("channelId") @Parameter(description = "The ID of the channel to retrieve.", required = true) @PathParam("channelId") String channelId,
			@Param("includeCodeTemplateLibraries") @Parameter(description = "If true, code template libraries will be included in the channel.") @QueryParam("includeCodeTemplateLibraries") boolean includeCodeTemplateLibraries)
			throws ClientException;

	@GET
	@Path("/{channelId}/connectorNames")
	@Operation(summary = "Returns all connector names for a channel.")
	@ApiResponse(content = {
			@Content(mediaType = MediaType.APPLICATION_XML, examples = {
					@ExampleObject(name = "connectorNameMap", ref = "../apiexamples/connector_name_map_xml") }),
			@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
					@ExampleObject(name = "connectorNameMap", ref = "../apiexamples/connector_name_map_json") }) })
	@MirthOperation(name = "getConnectorNames", display = "Get connector names", permission = Permissions.MESSAGES_VIEW, auditable = false)
	public Map<Integer, String> getConnectorNames(
			@Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId)
			throws ClientException;

	@GET
	@Path("/{channelId}/metaDataColumns")
	@Operation(summary = "Returns all metadata columns for a channel.")
	@ApiResponse(content = {
			@Content(mediaType = MediaType.APPLICATION_XML, examples = {
					@ExampleObject(name = "metadataColumnList", ref = "../apiexamples/metadatacolumn_list_xml") }),
			@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
					@ExampleObject(name = "metadataColumnList", ref = "../apiexamples/metadatacolumn_list_json") }) })
	@MirthOperation(name = "getMetaDataColumns", display = "Get metadata columns", permission = Permissions.MESSAGES_VIEW, auditable = false)
	public List<MetaDataColumn> getMetaDataColumns(
			@Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId)
			throws ClientException;

	@GET
	@Path("/idsAndNames")
	@Operation(summary = "Returns a map of all channel IDs and names.")
	@ApiResponse(content = {
			@Content(mediaType = MediaType.APPLICATION_XML, examples = {
					@ExampleObject(name = "channelNameMap", ref = "../apiexamples/guid_to_name_map_xml") }),
			@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
					@ExampleObject(name = "channelNameMap", ref = "../apiexamples/guid_to_name_map_json") }) })
	@MirthOperation(name = "getChannelIdsAndNames", display = "Get channel IDs and names", permission = Permissions.CHANNELS_VIEW, type = ExecuteType.ASYNC, auditable = false)
	public Map<String, String> getChannelIdsAndNames() throws ClientException;

	@POST
	@Path("/_getSummary")
	@Operation(summary = "Returns a list of channel summaries, indicating to a client which channels have changed (been updated, deleted, undeployed, etc.). If a channel was modified, the entire Channel object will be returned.")
	@ApiResponse(content = {
			@Content(mediaType = MediaType.APPLICATION_XML, examples = {
					@ExampleObject(name = "channelSummaryList", ref = "../apiexamples/channel_summary_list_xml") }),
			@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
					@ExampleObject(name = "channelSummaryList", ref = "../apiexamples/channel_summary_list_json") }) })
	@MirthOperation(name = "getChannelSummary", display = "Get channel summary", permission = Permissions.CHANNELS_VIEW, type = ExecuteType.ASYNC, auditable = false)
	public List<ChannelSummary> getChannelSummary(// @formatter:off
			@Param("cachedChannels") @RequestBody(description = "A map of ChannelHeader objects telling the server the state of the client-side channel cache.", required = true, content = {
					@Content(mediaType = MediaType.APPLICATION_XML, examples = {
							@ExampleObject(name = "cachedChannels", ref = "../apiexamples/channel_header_map_xml") }),
					@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
							@ExampleObject(name = "cachedChannels", ref = "../apiexamples/channel_header_map_json") }) }) Map<String, ChannelHeader> cachedChannels,
			@Param("ignoreNewChannels") @Parameter(description = "If true, summaries will only be returned for channels in the map's entry set.", required = true) @QueryParam("ignoreNewChannels") boolean ignoreNewChannels)
			throws ClientException;
	// @formatter: on

	@POST
	@Path("/_setEnabled")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Operation(summary = "Enables/disables the specified channels. " + SWAGGER_ARRAY_DISCLAIMER)
	@MirthOperation(name = "setChannelEnabled", display = "Set channel enabled flag", permission = Permissions.CHANNELS_MANAGE)
	public void setChannelEnabled(// @formatter:off
			@Param("channelIds") @Parameter(description = "The IDs of the channels to enable/disable. If absent, all channels will be enabled/disabled.") @FormParam("channelId") Set<String> channelIds,
			@Param("enabled") @Parameter(description = "Indicates whether the channels should be enabled or disabled.", required = true) @FormParam("enabled") boolean enabled)
			throws ClientException;
	// @formatter:on

	@POST
	@Path("/{channelId}/enabled/{enabled}")
	@Operation(summary = "Enables/disables the specified channel.")
	@MirthOperation(name = "setChannelEnabled", display = "Set channel enabled flag", permission = Permissions.CHANNELS_MANAGE)
	public void setChannelEnabled(// @formatter:off
			@Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
			@Param("enabled") @Parameter(description = "The enabled flag (true/false) to set.", required = true) @PathParam("enabled") boolean enabled)
			throws ClientException;
	// @formatter:on

	@POST
	@Path("/_setInitialState")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Operation(summary = "Sets the initial state for the specified channels. " + SWAGGER_ARRAY_DISCLAIMER)
	@MirthOperation(name = "setChannelInitialState", display = "Set channel initial state", permission = Permissions.CHANNELS_MANAGE)
	public void setChannelInitialState(// @formatter:off
			@Param("channelIds") @Parameter(description = "The IDs of the channels to modify initial states on. If absent, the initial state will be set on all channels.") @FormParam("channelId") Set<String> channelIds,
			@Param("initialState") @Parameter(description = "The initial state of the channel.", required = true, schema = @Schema(allowableValues = {
					"STARTED", "PAUSED",
					"STOPPED" }, type = "string")) @FormParam("initialState") DeployedState initialState)
			throws ClientException;
	// @formatter:on

	@POST
	@Path("/{channelId}/initialState/{initialState}")
	@Operation(summary = "Sets the initial state for a single channel.")
	@MirthOperation(name = "setChannelInitialState", display = "Set channel initial state", permission = Permissions.CHANNELS_MANAGE)
	public void setChannelInitialState(// @formatter:off
			@Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
			@Param("initialState") @Parameter(description = "The initial state of the channel.", required = true, schema = @Schema(allowableValues = {
					"STARTED", "PAUSED",
					"STOPPED" }, type = "string")) @PathParam("initialState") DeployedState initialState)
			throws ClientException;
	// @formatter:on

	@PUT
	@Path("/{channelId}")
	@Operation(summary = "Updates the specified channel.")
	@MirthOperation(name = "updateChannel", display = "Update channel", permission = Permissions.CHANNELS_MANAGE)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	public boolean updateChannel(// @formatter:off
			@Param("channelId") @Parameter(description = "The ID of the channel to update.", required = true) @PathParam("channelId") String channelId,
			@Param("channel") @RequestBody(description = "The Channel object to update with.", required = true, content = {
					@Content(mediaType = MediaType.APPLICATION_XML, schema = @Schema(implementation = Channel.class), examples = {
							@ExampleObject(name = "channel", ref = "../apiexamples/channel_xml") }),
					@Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Channel.class), examples = {
							@ExampleObject(name = "channel", ref = "../apiexamples/channel_json") }) }) Channel channel,
			@Param("override") @Parameter(description = "If true, the channel will be updated even if a different revision exists on the server.", schema = @Schema(defaultValue = "false")) @QueryParam("override") boolean override)
			throws ClientException;
	// @formatter:on

	@DELETE
	@Path("/{channelId}")
	@Operation(summary = "Removes the channel with the specified ID.")
	@MirthOperation(name = "removeChannel", display = "Remove channel", permission = Permissions.CHANNELS_MANAGE)
	public void removeChannel(
			@Param("channelId") @Parameter(description = "The ID of the channel to remove.", required = true) @PathParam("channelId") String channelId)
			throws ClientException;

	@DELETE
	@Path("/")
	@Operation(summary = "Removes the channels with the specified IDs.")
	@MirthOperation(name = "removeChannels", display = "Remove channels", permission = Permissions.CHANNELS_MANAGE)
	public void removeChannels(
			@Param("channelIds") @Parameter(description = "The IDs of the channels to remove.", required = true) @QueryParam("channelId") Set<String> channelIds)
			throws ClientException;

	@POST
	@Path("/_removeChannels")
	@Operation(summary = "Removes the channels with the specified IDs. This is a POST request alternative to DELETE /channels that may be used when there are too many channel IDs to include in the query parameters.")
	@MirthOperation(name = "removeChannels", display = "Remove channels", permission = Permissions.CHANNELS_MANAGE)
	public void removeChannelsPost(
			@Param("channelIds") @RequestBody(description = "The IDs of the channels to remove.", required = true, content = {
					@Content(mediaType = MediaType.APPLICATION_XML, examples = {
							@ExampleObject(name = "channelIds", ref = "../apiexamples/guid_set_xml") }),
					@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
							@ExampleObject(name = "channelIds", ref = "../apiexamples/guid_set_json") }) }) Set<String> channelIds)
			throws ClientException;
}