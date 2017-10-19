package com.mirth.connect.plugins.dashboardstatus;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.ControllerFactory;

public class DashboardConnectionStateServlet extends MirthServlet implements DashboardConnectionStateServletInterface {
	
    private static final ConnectionStatusLogController controller = (ConnectionStatusLogController) ControllerFactory.getFactory().createExtensionController().getServicePlugins().get(PLUGIN_POINT);
    
    public DashboardConnectionStateServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, PLUGIN_POINT);
    }

	public DashboardConnectionStateServlet(HttpServletRequest request, ContainerRequestContext containerRequestContext,
			SecurityContext sc) {
		super(request, containerRequestContext, sc);
	}

	@Override
	public Map<String, Map<String, List<ConnectionStateItem>>> getConnectorStatesMap(String serverId) throws ClientException {
		return controller.getConnectionStatesForServer(serverId);
	}

}
