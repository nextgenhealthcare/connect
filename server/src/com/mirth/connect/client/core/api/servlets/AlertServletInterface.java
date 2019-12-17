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
import javax.ws.rs.DELETE;
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
import com.mirth.connect.model.ChannelHeader;
import com.mirth.connect.model.alert.AlertInfo;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.alert.AlertStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/alerts")
@Tag(name = "Alerts")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface AlertServletInterface extends BaseServletInterface {
	
	@POST
	@Path("/")
	@Operation(summary = "Creates a new alert.")
	@MirthOperation(name = "createAlert", display = "Create alert", permission = Permissions.ALERTS_MANAGE)
	public void createAlert(
			@Param("alertModel") @RequestBody(description = "The alert to create.", required = true, content = {
					@Content(mediaType = MediaType.APPLICATION_XML, examples = {
							@ExampleObject(name = "alert", ref = "../apiexamples/alert_xml") }),
					@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
							@ExampleObject(name = "alert", ref = "../apiexamples/alert_json") }) }) AlertModel alertModel)
			throws ClientException;

	@GET
	@Path("/{alertId}")
	@Operation(summary = "Retrieves an alert by ID.")
	@ApiResponse(content = {
			@Content(mediaType = MediaType.APPLICATION_XML, examples = {
					@ExampleObject(name = "alert", ref = "../apiexamples/alert_xml") }),
			@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
					@ExampleObject(name = "alert", ref = "../apiexamples/alert_json") }), })
	@MirthOperation(name = "getAlert", display = "Get alerts", permission = Permissions.ALERTS_VIEW)
	public AlertModel getAlert(
			@Param("alertId") @Parameter(description = "The ID of the alert.", required = true) @PathParam("alertId") String alertId)
			throws ClientException;

	@GET
	@Path("/")
	@Operation(summary = "Retrieves multiple alerts by ID, or all alerts if not specified.")
	@ApiResponse(content = {
			@Content(mediaType = MediaType.APPLICATION_XML, examples = {
					@ExampleObject(name = "alert_list", ref = "../apiexamples/alert_list_xml") }),
			@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
					@ExampleObject(name = "alert_list", ref = "../apiexamples/alert_list_json") }), })
	@MirthOperation(name = "getAlert", display = "Get alerts", permission = Permissions.ALERTS_VIEW)
	public List<AlertModel> getAlerts(
			@Param("alertIds") @Parameter(description = "The ID of the alert(s). If absent, all alerts will be returned.") @QueryParam("alertId") Set<String> alertIds)
			throws ClientException;

	@POST
	@Path("/_getAlerts")
	@Operation(summary = "Retrieves multiple alerts by ID, or all alerts if not specified. This is a POST request alternative to GET /alerts that may be used when there are too many alert IDs to include in the query parameters.")
	@ApiResponse(content = {
			@Content(mediaType = MediaType.APPLICATION_XML, examples = {
					@ExampleObject(name = "alert_list", ref = "../apiexamples/alert_list_xml") }),
			@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
					@ExampleObject(name = "alert_list", ref = "../apiexamples/alert_list_json") }) })
	@MirthOperation(name = "getAlert", display = "Get alerts", permission = Permissions.ALERTS_VIEW)
	public List<AlertModel> getAlertsPost(
			@Param("alertIds") @RequestBody(description = "The ID of the alert(s). If absent, all alerts will be returned.", content = {
					@Content(mediaType = MediaType.APPLICATION_XML, examples = {
							@ExampleObject(name = "alert_set", ref = "../apiexamples/guid_set_xml") }),
					@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
							@ExampleObject(name = "alert_set", ref = "../apiexamples/guid_set_json") }) }) Set<String> alertIds)
			throws ClientException;

	@GET
	@Path("/statuses")
	@Operation(summary = "Returns all alert dashboard statuses.")
	@ApiResponse(content = {
			@Content(mediaType = MediaType.APPLICATION_XML, examples = {
					@ExampleObject(name = "alert_status_list", ref = "../apiexamples/alert_status_list_xml") }),
			@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
					@ExampleObject(name = "alert_status_list", ref = "../apiexamples/alert_status_list_json") }) })
	@MirthOperation(name = "getAlertStatusList", display = "Get alert status list", permission = Permissions.ALERTS_VIEW, type = ExecuteType.ASYNC, auditable = false)
	public List<AlertStatus> getAlertStatusList() throws ClientException;

	@POST
	@Path("/{alertId}/_getInfo")
	@Operation(summary = "Returns an AlertInfo object containing the alert model, alert protocol options, and any updated channel summaries.")
	@ApiResponse(content = {
			@Content(mediaType = MediaType.APPLICATION_XML, examples = {
					@ExampleObject(name = "alert_info", ref = "../apiexamples/alert_info_xml") }),
			@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
					@ExampleObject(name = "alert_info", ref = "../apiexamples/alert_info_json") }) })
	@MirthOperation(name = "getAlertInfo", display = "Get alert info", permission = Permissions.ALERTS_VIEW, auditable = false)
	public AlertInfo getAlertInfo(// @formatter:off
			@Param("alertId") @Parameter(description = "The ID of the alert.", required = true) @PathParam("alertId") String alertId,
			@Param("cachedChannels") @RequestBody(description = "A map of ChannelHeader objects telling the server the state of the client-side channel cache.", required = true, content = {
					@Content(mediaType = MediaType.APPLICATION_XML, examples = {
							@ExampleObject(name = "channel_header_map", ref = "../apiexamples/channel_header_map_xml") }),
					@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
							@ExampleObject(name = "channel_header_map", ref = "../apiexamples/channel_header_map_json") }) }) Map<String, ChannelHeader> cachedChannels)
			throws ClientException;
	// @formatter:on

	@POST
	@Path("/_getInfo")
	@Operation(summary = "Returns an AlertInfo object containing alert protocol options and any updated channel summaries.")
	@ApiResponse(content = {
			@Content(mediaType = MediaType.APPLICATION_XML, examples = {
					@ExampleObject(name = "alert_info", ref = "../apiexamples/alert_info_xml") }),
			@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
					@ExampleObject(name = "alert_info", ref = "../apiexamples/alert_info_json") }) })
	@MirthOperation(name = "getAlertInfo", display = "Get alert info", permission = Permissions.ALERTS_VIEW)
	public AlertInfo getAlertInfo(
			@Param("cachedChannels") @RequestBody(description = "A map of ChannelHeader objects telling the server the state of the client-side channel cache.", required = true, content = {
					@Content(mediaType = MediaType.APPLICATION_XML, examples = {
							@ExampleObject(name = "channel_header_map", ref = "../apiexamples/channel_header_map_xml") }),
					@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
							@ExampleObject(name = "channel_header_map", ref = "../apiexamples/channel_header_map_json") }) }) Map<String, ChannelHeader> cachedChannels)
			throws ClientException;

	@GET
	@Path("/options")
	@Operation(summary = "Returns all alert protocol options.")
	@ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
			@ExampleObject(name = "alert_protocol_options", ref = "../apiexamples/alert_protocol_options_xml") }),
			@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
					@ExampleObject(name = "alert_protocol_options", ref = "../apiexamples/alert_protocol_options_json") }) })
	@MirthOperation(name = "getAlertProtocolOptions", display = "Get alert protocol options", permission = Permissions.ALERTS_VIEW, auditable = false)
	public Map<String, Map<String, String>> getAlertProtocolOptions() throws ClientException;

	@PUT
	@Path("/{alertId}")
	@Operation(summary = "Updates the specified alert.")
	@MirthOperation(name = "updateAlert", display = "Update alert", permission = Permissions.ALERTS_MANAGE)
	public void updateAlert(// @formatter:off
			@Param("alertId") @Parameter(description = "The ID of the alert.", required = true) @PathParam("alertId") String alertId,
			@Param("alertModel") @RequestBody(description = "The alert to create.", required = true, content = {
					@Content(mediaType = MediaType.APPLICATION_XML, examples = {
							@ExampleObject(name = "alert", ref = "../apiexamples/alert_xml") }),
					@Content(mediaType = MediaType.APPLICATION_JSON, examples = {
							@ExampleObject(name = "alert", ref = "../apiexamples/alert_json") }) }) AlertModel alertModel)
			throws ClientException;
	// @formatter:on

	@POST
	@Path("/{alertId}/_enable")
	@Operation(summary = "Enables the specified alert.")
	@MirthOperation(name = "enableAlert", display = "Enable alert", permission = Permissions.ALERTS_MANAGE)
	public void enableAlert(
			@Param("alertId") @Parameter(description = "The ID of the alert.", required = true) @PathParam("alertId") String alertId)
			throws ClientException;

	@POST
	@Path("/{alertId}/_disable")
	@Operation(summary = "Disables the specified alert.")
	@MirthOperation(name = "disableAlert", display = "Disable alert", permission = Permissions.ALERTS_MANAGE)
	public void disableAlert(
			@Param("alertId") @Parameter(description = "The ID of the alert.", required = true) @PathParam("alertId") String alertId)
			throws ClientException;

	@DELETE
	@Path("/{alertId}")
	@Operation(summary = "Removes the specified alert.")
	@MirthOperation(name = "removeAlert", display = "Remove alert", permission = Permissions.ALERTS_MANAGE)
	public void removeAlert(
			@Param("alertId") @Parameter(description = "The ID of the alert.", required = true) @PathParam("alertId") String alertId)
			throws ClientException;
}