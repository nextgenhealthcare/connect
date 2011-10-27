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

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.Operation;
import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ChannelStatusController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class ChannelStatusServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // MIRTH-1745
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            try {
                ChannelStatusController channelStatusController = ControllerFactory.getFactory().createChannelStatusController();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                PrintWriter out = response.getWriter();
                Operation operation = Operations.getOperation(request.getParameter("op"));
                String channelId = request.getParameter("id");
                Map<String, Object> parameterMap = new HashMap<String, Object>();
                parameterMap.put("channelId", channelId);

                if (operation.equals(Operations.CHANNEL_START)) {
                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        channelStatusController.startChannel(channelId);
                    }
                } else if (operation.equals(Operations.CHANNEL_STOP)) {
                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        channelStatusController.stopChannel(channelId);
                    }
                } else if (operation.equals(Operations.CHANNEL_PAUSE)) {
                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        channelStatusController.pauseChannel(channelId);
                    }
                } else if (operation.equals(Operations.CHANNEL_RESUME)) {
                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        channelStatusController.resumeChannel(channelId);
                    }
                } else if (operation.equals(Operations.CHANNEL_GET_STATUS)) {
                    response.setContentType(APPLICATION_XML);
                    List<ChannelStatus> channelStatuses = null;

                    if (!isUserAuthorized(request, null)) {
                        channelStatuses = new ArrayList<ChannelStatus>();
                    } else if (doesUserHaveChannelRestrictions(request)) {
                        channelStatuses = redactChannelStatuses(request, channelStatusController.getChannelStatusList());
                    } else {
                        channelStatuses = channelStatusController.getChannelStatusList();
                    }

                    serializer.toXML(channelStatuses, out);    
                }
            } catch (Throwable t) {
                logger.error(ExceptionUtils.getStackTrace(t));
                throw new ServletException(t);
            }
        }
    }
    
    private List<ChannelStatus> redactChannelStatuses(HttpServletRequest request, List<ChannelStatus> channelStatuses) throws ServletException {
        List<String> authorizedChannelIds = getAuthorizedChannelIds(request);
        List<ChannelStatus> authorizedChannelStatuses = new ArrayList<ChannelStatus>();
        
        for (ChannelStatus channelStatus : channelStatuses) {
            if (authorizedChannelIds.contains(channelStatus.getChannelId())) {
                authorizedChannelStatuses.add(channelStatus);
            }
        }
        
        return authorizedChannelStatuses;
    }

}
