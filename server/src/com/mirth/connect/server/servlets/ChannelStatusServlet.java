/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.io.RuntimeIOException;

import com.mirth.connect.client.core.Operation;
import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;

public class ChannelStatusServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // MIRTH-1745
        response.setCharacterEncoding("UTF-8");

        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        EngineController engineController = ControllerFactory.getFactory().createEngineController();
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        PrintWriter out = response.getWriter();
        Operation operation = Operations.getOperation(request.getParameter("op"));
        String channelId = request.getParameter("id");
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("channelId", channelId);

        if ((!operation.equals(Operations.CHANNEL_GET_STATUS) && !isUserAuthorized(request, parameterMap)) || (operation.equals(Operations.CHANNEL_GET_STATUS) && (!isUserAuthorized(request, null)))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }

        try {
            if (operation.equals(Operations.CHANNEL_START)) {
                engineController.startChannel(channelId);
            } else if (operation.equals(Operations.CHANNEL_STOP)) {
                engineController.stopChannel(channelId);
            } else if (operation.equals(Operations.CHANNEL_HALT)) {
                engineController.haltChannel(channelId);
            } else if (operation.equals(Operations.CHANNEL_PAUSE)) {
                engineController.pauseChannel(channelId);
            } else if (operation.equals(Operations.CHANNEL_RESUME)) {
                engineController.resumeChannel(channelId);
            } else if (operation.equals(Operations.CHANNEL_GET_STATUS)) {
                response.setContentType(APPLICATION_XML);
                List<DashboardStatus> channelStatuses = null;

                if (doesUserHaveChannelRestrictions(request)) {
                    channelStatuses = redactChannelStatuses(request, engineController.getChannelStatusList());
                } else {
                    channelStatuses = engineController.getChannelStatusList();
                }

                serializer.toXML(channelStatuses, out);
            }
        } catch (RuntimeIOException rio) {
            logger.debug(rio);
        } catch (Throwable t) {
            // log the error, but don't throw the exception back since the client may no longer be waiting for a response
            logger.error(ExceptionUtils.getStackTrace(t));
        }
    }

    private List<DashboardStatus> redactChannelStatuses(HttpServletRequest request, List<DashboardStatus> channelStatuses) throws ServletException {
        List<String> authorizedChannelIds = getAuthorizedChannelIds(request);
        List<DashboardStatus> authorizedStatuses = new ArrayList<DashboardStatus>();

        for (DashboardStatus status : channelStatuses) {
            if (authorizedChannelIds.contains(status.getChannelId())) {
                authorizedStatuses.add(status);
            }
        }

        return authorizedStatuses;
    }
}
