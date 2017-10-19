package com.mirth.connect.plugins.dashboardstatus;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.Operation.ExecuteType;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;
import com.mirth.connect.client.core.api.Param;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("/extensions/dashboardstatus")
@Api("Extension Services")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface DashboardConnectionStateServletInterface extends BaseServletInterface {
    public static final String PLUGIN_POINT = "Dashboard Connection State Service";
    public static final String PERMISSION_VIEW = "View Connection State";

    @GET
    @Path("/connectorStates/")
    @ApiOperation("Retrieves connection logs for a specific channel.")
    @MirthOperation(name = "getConnectionStates", display = "Get channel connection states", permission = PERMISSION_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public Map<String, Map<String, List<ConnectionStateItem>>> getConnectorStatesMap(// @formatter:off
    		@Param("serverId") @ApiParam(value = "The server ID to retrieve connection states for. States for all servers are retrieved if this parameter is not specified.") @QueryParam("serverId") String serverId
	) throws ClientException;
    //@formatter:on
}
