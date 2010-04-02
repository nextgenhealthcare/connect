/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.controllers.ChannelStatusController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.ControllerFactory;

public class ChannelStatusServlet extends MirthServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            try {
                ChannelStatusController channelStatusController = ControllerFactory.getFactory().createChannelStatusController();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                PrintWriter out = response.getWriter();
                String operation = request.getParameter("op");

                if (operation.equals("startChannel")) {
                    channelStatusController.startChannel(request.getParameter("id"));
                } else if (operation.equals("stopChannel")) {
                    channelStatusController.stopChannel(request.getParameter("id"));
                } else if (operation.equals("pauseChannel")) {
                    channelStatusController.pauseChannel(request.getParameter("id"));
                } else if (operation.equals("resumeChannel")) {
                    channelStatusController.resumeChannel(request.getParameter("id"));
                } else if (operation.equals("getChannelStatusList")) {
                    response.setContentType("application/xml");
                    out.print(serializer.toXML(channelStatusController.getChannelStatusList()));
                }
            } catch (ControllerException e) {
                throw new ServletException(e);
            }
        }
    }
}
