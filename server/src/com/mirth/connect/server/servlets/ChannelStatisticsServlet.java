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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.Operation;
import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ChannelStatisticsController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class ChannelStatisticsServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            try {
                ChannelStatisticsController statisticsController = ControllerFactory.getFactory().createChannelStatisticsController();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                PrintWriter out = response.getWriter();
                Operation operation = Operations.getOperation(request.getParameter("op"));
                String channelId = request.getParameter("id");
                Map<String, Object> parameterMap = new HashMap<String, Object>();
                parameterMap.put("channelId", channelId);

                if (operation.equals(Operations.CHANNEL_STATS_GET)) {
                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType("application/xml");
                        out.println(serializer.toXML(statisticsController.getStatistics(channelId)));
                    }
                } else if (operation.equals(Operations.CHANNEL_STATS_CLEAR)) {
                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        boolean deleteReceived = Boolean.valueOf(request.getParameter("deleteReceived"));
                        boolean deleteFiltered = Boolean.valueOf(request.getParameter("deleteFiltered"));
                        boolean deleteQueued = Boolean.valueOf(request.getParameter("deleteQueued"));
                        boolean deleteSent = Boolean.valueOf(request.getParameter("deleteSent"));
                        boolean deleteErrored = Boolean.valueOf(request.getParameter("deleteErrored"));
                        boolean deleteAlerted = Boolean.valueOf(request.getParameter("deleteAlerted"));
                        statisticsController.clearStatistics(channelId, deleteReceived, deleteFiltered, deleteQueued, deleteSent, deleteErrored, deleteAlerted);
                    }
                }
            } catch (Throwable t) {
                logger.error(ExceptionUtils.getStackTrace(t));
                throw new ServletException(t);
            }
        }
    }
}
