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
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;

public class EngineServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // MIRTH-1745
        response.setCharacterEncoding("UTF-8");
        
        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            try {
                EngineController engineController = ControllerFactory.getFactory().createEngineController();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                Operation operation = Operations.getOperation(request.getParameter("op"));
                Map<String, Object> parameterMap = new HashMap<String, Object>();
                ServerEventContext context = new ServerEventContext();
                context.setUserId(getCurrentUserId(request));

                if (operation.equals(Operations.CHANNEL_REDEPLOY)) {
                    if (!isUserAuthorized(request, null)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        engineController.redeployAllChannels(context);
                    }
                } else if (operation.equals(Operations.CHANNEL_DEPLOY)) {
                    List<Channel> channels = (List<Channel>) serializer.fromXML(request.getParameter("channels"));
                    parameterMap.put("channels", channels);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        engineController.deployChannels(channels, context);
                    }
                } else if (operation.equals(Operations.CHANNEL_UNDEPLOY)) {
                    List<String> channelIds = (List<String>) serializer.fromXML(request.getParameter("channelIds"));
                    parameterMap.put("channelIds", channelIds);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        engineController.undeployChannels(channelIds, context);
                    }
                }
            } catch (Throwable t) {
                logger.error(ExceptionUtils.getStackTrace(t));
                throw new ServletException(t);
            }
        }
    }
}
