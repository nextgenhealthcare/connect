package com.mirth.connect.client.core.api.servlets;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;
import com.mirth.connect.model.SystemInfo;
import com.mirth.connect.model.SystemStats;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/system")
@Api("System Information and Statistics")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface SystemServletInterface extends BaseServletInterface {
    @GET
    @Path("/info")
    @ApiOperation("Returns information about the underlying system.")
    @MirthOperation(name = "getJVMInfo", display = "Get System Information", auditable = false)
    public SystemInfo getInfo() throws ClientException;

    @GET
    @Path("/stats")
    @ApiOperation("Returns statistics for the underlying system.")
    @MirthOperation(name = "getStats", display = "Get System Statistics", auditable = false)
    public SystemStats getStats() throws ClientException;
}
