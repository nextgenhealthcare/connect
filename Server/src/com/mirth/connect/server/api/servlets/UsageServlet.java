/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.servlets;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import com.mirth.connect.client.core.api.servlets.UsageServletInterface;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.UsageController;

public class UsageServlet extends MirthServlet implements UsageServletInterface {

    private static final UsageController usageController = ControllerFactory.getFactory().createUsageController();

    public UsageServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc);
    }

    @Override
    public String getUsageData(Map<String, Object> clientStats) {
        return usageController.createUsageStats(clientStats);
    }
}