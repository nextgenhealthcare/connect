/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.serverlog;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.ControllerFactory;

public class ServerLogServlet extends MirthServlet implements ServerLogServletInterface {

    private static final ServerLogProvider provider = (ServerLogProvider) ControllerFactory.getFactory().createExtensionController().getServicePlugins().get(PLUGIN_POINT);

    public ServerLogServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, PLUGIN_POINT);
    }

    @Override
    public List<ServerLogItem> getServerLogs(int fetchSize, Long lastLogId) {
        return provider.getServerLogs(fetchSize, lastLogId);
    }
}