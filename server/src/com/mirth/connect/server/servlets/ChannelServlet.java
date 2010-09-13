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
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class ChannelServlet extends MirthServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            try {
                ChannelController channelController = ControllerFactory.getFactory().createChannelController();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                PrintWriter out = response.getWriter();
                String operation = request.getParameter("op");

                if (operation.equals(Operations.CHANNEL_GET)) {
                    response.setContentType("application/xml");
                    List<Channel> channels = null;
                    
                    if (!isUserAuthorized(request)) {
                        channels = new ArrayList<Channel>();
                    } else {
                        Channel channel = (Channel) serializer.fromXML(request.getParameter("channel"));
                        channels = channelController.getChannel(channel);
                    }
                    
                    out.println(serializer.toXML(channels));
                } else if (operation.equals(Operations.CHANNEL_UPDATE)) {
                    if (!isUserAuthorized(request)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType("text/plain");
                        Channel channel = (Channel) serializer.fromXML(request.getParameter("channel"));
                        boolean override = Boolean.valueOf(request.getParameter("override")).booleanValue();
                        // NOTE: This needs to be print rather than println to
                        // avoid the newline
                        out.print(channelController.updateChannel(channel, override));
                    }
                } else if (operation.equals(Operations.CHANNEL_REMOVE)) {
                    if (!isUserAuthorized(request)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        Channel channel = (Channel) serializer.fromXML(request.getParameter("channel"));
                        channelController.removeChannel(channel);
                    }
                } else if (operation.equals(Operations.CHANNEL_GET_SUMMARY)) {
                    response.setContentType("application/xml");
                    List<ChannelSummary> channels = null;
                    
                    if (!isUserAuthorized(request)) {
                        channels = new ArrayList<ChannelSummary>();
                    } else {
                        Map<String, Integer> cachedChannels = (Map<String, Integer>) serializer.fromXML(request.getParameter("cachedChannels"));
                        channels = channelController.getChannelSummary(cachedChannels);
                    }
                    
                    out.println(serializer.toXML(channels));
                }
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
    }

}
