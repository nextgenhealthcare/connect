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
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class ChannelServlet extends MirthServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isUserLoggedIn(request)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		} else if (!isUserAuthorized(request)) {
		    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				ChannelController channelController = ControllerFactory.getFactory().createChannelController();
				ObjectXMLSerializer serializer = new ObjectXMLSerializer();
				PrintWriter out = response.getWriter();
				String operation = request.getParameter("op");

				if (operation.equals(Operations.CHANNEL_GET)) {
					response.setContentType("application/xml");
					Channel channel = (Channel) serializer.fromXML(request.getParameter("channel"));
					out.println(serializer.toXML(channelController.getChannel(channel)));
				} else if (operation.equals(Operations.CHANNEL_UPDATE)) {
					response.setContentType("text/plain");
					Channel channel = (Channel) serializer.fromXML(request.getParameter("channel"));
					boolean override = Boolean.valueOf(request.getParameter("override")).booleanValue();
					// NOTE: This needs to be print rather than println to avoid the newline
					out.print(channelController.updateChannel(channel, override));
				} else if (operation.equals(Operations.CHANNEL_REMOVE)) {
					Channel channel = (Channel) serializer.fromXML(request.getParameter("channel"));
					channelController.removeChannel(channel);
				} else if (operation.equals(Operations.CHANNEL_GET_SUMMARY)) {
					response.setContentType("application/xml");
					Map<String, Integer> cachedChannels = (Map<String, Integer>) serializer.fromXML(request.getParameter("cachedChannels"));
					out.println(serializer.toXML(channelController.getChannelSummary(cachedChannels)));
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}

}
