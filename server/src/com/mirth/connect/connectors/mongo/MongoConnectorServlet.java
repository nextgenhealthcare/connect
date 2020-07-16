package com.mirth.connect.connectors.mongo;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import com.mirth.connect.server.api.MirthServlet;

public class MongoConnectorServlet extends MirthServlet implements MongoConnectorServletInterface{

    public MongoConnectorServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, PLUGIN_POINT);
    }
}
