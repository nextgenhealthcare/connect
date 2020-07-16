package com.mirth.connect.connectors.mongo;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mirth.connect.client.core.api.BaseServletInterface;

import io.swagger.annotations.Api;

@Path("/connectors/mongo")
@Api("Connector Services")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface MongoConnectorServletInterface  extends BaseServletInterface {
	 public static final String PLUGIN_POINT = "Document Connector Service";
	 
	 
}
