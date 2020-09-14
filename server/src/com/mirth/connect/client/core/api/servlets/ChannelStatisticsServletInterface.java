/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api.servlets;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import com.mirth.connect.model.ChannelStatistics;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/channels")
@Tag(name = "Channel Statistics")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface ChannelStatisticsServletInterface extends BaseServletInterface {

    @GET
    @Path("/statistics")
    @Operation(summary = "Returns the Statistics for all channels.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "channel_statistics_list", ref = "../apiexamples/channel_statistics_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "channel_statistics_list", ref = "../apiexamples/channel_statistics_list_json") }) })
    @MirthOperation(name = "getAllStatistics", display = "Get all statistics", permission = Permissions.DASHBOARD_VIEW, auditable = false)
    public List<ChannelStatistics> getStatistics(//@formatter:off
            @Param("channelIds") @Parameter(description = "The IDs of the channels to retrieve. If absent, all channels will be retrieved.") @QueryParam("channelId") Set<String> channelIds,
            @Param("includeUndeployed") @Parameter(description = "If true, statistics for undeployed channels will also be included.") @QueryParam("includeUndeployed") boolean includeUndeployed,
            @Param("includeMetadataIds") @Parameter(description = "The ids of connectors to include. Cannot include and exclude connectors.") @QueryParam("includeMetadataId") Set<Integer> includeMetadataIds,
            @Param("excludeMetadataIds") @Parameter(description = "The ids of connectors to exclude. Cannot include and exclude connectors.") @QueryParam("excludeMetadataId") Set<Integer> excludeMetadataIds,
            @Param("aggregateStats") @Parameter(description = "If true, statistics will be aggregated into one result") @QueryParam("aggregateStats") boolean aggregateStats) throws ClientException;
    // @formatter:on

    @POST
    @Path("/statistics/_getStatistics")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Returns the Statistics for all channels. This is a POST request alternative to GET /statistics that may be used when there are too many channel IDs to include in the query parameters.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "channel_statistics_list", ref = "../apiexamples/channel_statistics_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "channel_statistics_list", ref = "../apiexamples/channel_statistics_list_json") }) })
    @MirthOperation(name = "getAllStatistics", display = "Get all statistics", permission = Permissions.DASHBOARD_VIEW, auditable = false)
    public List<ChannelStatistics> getStatisticsPost(//@formatter:off
            @Param("channelIds") @Parameter(description = "The IDs of the channels to retrieve. If absent, all channels will be retrieved.") @FormDataParam("channelIds") Set<String> channelIds,
            @Param("includeUndeployed") @Parameter(description = "If true, statistics for undeployed channels will also be included.") @FormDataParam("includeUndeployed") boolean includeUndeployed,
            @Param("includeMetadataIds") @Parameter(description = "The ids of connectors to include. Cannot include and exclude connectors.") @FormDataParam("includeMetadataIds") Set<Integer> includeMetadataIds,
            @Param("excludeMetadataIds") @Parameter(description = "The ids of connectors to exclude. Cannot include and exclude connectors.") @FormDataParam("excludeMetadataIds") Set<Integer> excludeMetadataIds,
            @Param("aggregateStats") @Parameter(description = "If true, statistics will be aggregated into one result") @FormDataParam("aggregateStats") boolean aggregateStats) throws ClientException;
    // @formatter:on

    @GET
    @Path("/{channelId}/statistics")
    @Operation(summary = "Returns the Statistics for the channel with the specified id.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "channel_statistics", ref = "../apiexamples/channel_statistics_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "channel_statistics", ref = "../apiexamples/channel_statistics_json") }) })
    @MirthOperation(name = "getStatistics", display = "Get statistics", permission = Permissions.DASHBOARD_VIEW, auditable = false)
    public ChannelStatistics getStatistics(@Param("channelId") @Parameter(description = "The ID of the channel to retrieve statistics for.", required = true) @PathParam("channelId") String channelId) throws ClientException;

    @POST
    @Path("/_clearStatistics")
    @Operation(summary = "Clears the statistics for the given channels and/or connectors.")
    @MirthOperation(name = "clearStatistics", display = "Clear statistics", permission = Permissions.CHANNELS_CLEAR_STATS)
    public void clearStatistics(// @formatter:off
            @Param("channelConnectorMap") 
            @RequestBody(description = "Channel IDs mapped to lists of metaDataIds (connectors). If the metaDataId list is null, then all statistics for the channel will be cleared.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "connector_map", ref = "../apiexamples/connector_map_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "connector_map", ref = "../apiexamples/connector_map_json") }) })
            Map<String, List<Integer>> channelConnectorMap,
            
            @Param("received") @Parameter(description = "If true, received stats will be cleared.") @QueryParam("received") boolean received,
            @Param("filtered") @Parameter(description = "If true, filtered stats will be cleared.") @QueryParam("filtered") boolean filtered,
            @Param("sent") @Parameter(description = "If true, sent stats will be cleared.") @QueryParam("sent") boolean sent,
            @Param("error") @Parameter(description = "If true, error stats will be cleared.") @QueryParam("error") boolean error) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_clearAllStatistics")
    @Operation(summary = "Clears all statistics (including lifetime) for all channels/connectors.")
    @MirthOperation(name = "clearAllStatistics", display = "Clear all statistics", permission = Permissions.SERVER_CLEAR_LIFETIME_STATS)
    public void clearAllStatistics() throws ClientException;
}