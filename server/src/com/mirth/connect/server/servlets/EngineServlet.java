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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;

public class EngineServlet extends MirthServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else if (!isUserAuthorized(request)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            try {
                EngineController engineController = ControllerFactory.getFactory().createEngineController();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                String operation = request.getParameter("op");

                if (operation.equals(Operations.CHANNEL_REDEPLOY)) {
                    engineController.redeployAllChannels();
                } else if (operation.equals(Operations.CHANNEL_DEPLOY)) {
                    List<Channel> channels = (List<Channel>) serializer.fromXML(request.getParameter("channels"));
                    engineController.deployChannels(channels);
                } else if (operation.equals(Operations.CHANNEL_UNDEPLOY)) {
                    List<String> channelIds = (List<String>) serializer.fromXML(request.getParameter("channelIds"));
                    engineController.undeployChannels(channelIds);
                }
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
    }
}
