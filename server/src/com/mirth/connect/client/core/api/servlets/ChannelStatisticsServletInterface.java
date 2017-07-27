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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("/channels")
@Api("Channel Statistics")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface ChannelStatisticsServletInterface extends BaseServletInterface {

    @GET
    @Path("/statistics")
    @ApiOperation("Returns the Statistics for all channels.")
    @MirthOperation(name = "getAllStatistics", display = "Get all statistics", permission = Permissions.CHANNELS_VIEW, auditable = false)
    public List<ChannelStatistics> getStatistics(//@formatter:off
            @Param("channelIds") @ApiParam(value = "The IDs of the channels to retrieve. If absent, all channels will be retrieved.") @QueryParam("channelId") Set<String> channelIds,
            @Param("includeUndeployed") @ApiParam(value = "If true, statistics for undeployed channels will also be included.") @QueryParam("includeUndeployed") boolean includeUndeployed,
            @Param("includeMetadataIds") @ApiParam(value = "The ids of connectors to include. Cannot include and exclude connectors.") @QueryParam("includeMetadataId") Set<Integer> includeMetadataIds,
            @Param("excludeMetadataIds") @ApiParam(value = "The ids of connectors to exclude. Cannot include and exclude connectors.") @QueryParam("excludeMetadataId") Set<Integer> excludeMetadataIds,
            @Param("aggregateStats") @ApiParam(value = "If true, statistics will be aggregated into one result") @QueryParam("aggregateStats") boolean aggregateStats) throws ClientException;
    // @formatter:on

    @POST
    @Path("/statistics/_getStatistics")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation("Returns the Statistics for all channels. This is a POST request alternative to GET /statistics that may be used when there are too many channel IDs to include in the query parameters.")
    @MirthOperation(name = "getAllStatistics", display = "Get all statistics", permission = Permissions.CHANNELS_VIEW, auditable = false)
    public List<ChannelStatistics> getStatisticsPost(//@formatter:off
            @Param("channelIds") @ApiParam(value = "The IDs of the channels to retrieve. If absent, all channels will be retrieved.") @FormDataParam("channelIds") Set<String> channelIds,
            @Param("includeUndeployed") @ApiParam(value = "If true, statistics for undeployed channels will also be included.") @FormDataParam("includeUndeployed") boolean includeUndeployed,
            @Param("includeMetadataIds") @ApiParam(value = "The ids of connectors to include. Cannot include and exclude connectors.") @FormDataParam("includeMetadataIds") Set<Integer> includeMetadataIds,
            @Param("excludeMetadataIds") @ApiParam(value = "The ids of connectors to exclude. Cannot include and exclude connectors.") @FormDataParam("excludeMetadataIds") Set<Integer> excludeMetadataIds,
            @Param("aggregateStats") @ApiParam(value = "If true, statistics will be aggregated into one result") @FormDataParam("aggregateStats") boolean aggregateStats) throws ClientException;
    // @formatter:on

    @GET
    @Path("/{channelId}/statistics")
    @ApiOperation("Returns the Statistics for the channel with the specified id.")
    @MirthOperation(name = "getStatistics", display = "Get statistics", permission = Permissions.CHANNELS_VIEW, auditable = false)
    public ChannelStatistics getStatistics(@Param("channelId") @ApiParam(value = "The ID of the channel to retrieve statistics for.", required = true) @PathParam("channelId") String channelId) throws ClientException;

    @POST
    @Path("/_clearStatistics")
    @ApiOperation("Clears the statistics for the given channels and/or connectors.")
    @MirthOperation(name = "clearStatistics", display = "Clear statistics", permission = Permissions.CHANNELS_CLEAR_STATS)
    public void clearStatistics(// @formatter:off
            @Param("channelConnectorMap") @ApiParam(value = "Channel IDs mapped to lists of metaDataIds (connectors). If the metaDataId list is null, then all statistics for the channel will be cleared.", required = true) Map<String, List<Integer>> channelConnectorMap,
            @Param("received") @ApiParam("If true, received stats will be cleared.") @QueryParam("received") boolean received,
            @Param("filtered") @ApiParam("If true, filtered stats will be cleared.") @QueryParam("filtered") boolean filtered,
            @Param("sent") @ApiParam("If true, sent stats will be cleared.") @QueryParam("sent") boolean sent,
            @Param("error") @ApiParam("If true, error stats will be cleared.") @QueryParam("error") boolean error) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_clearAllStatistics")
    @ApiOperation("Clears all statistics (including lifetime) for all channels/connectors.")
    @MirthOperation(name = "clearAllStatistics", display = "Clear all statistics", permission = Permissions.SERVER_CLEAR_LIFETIME_STATS)
    public void clearAllStatistics() throws ClientException;
}