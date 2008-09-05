/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.ControllerFactory;

public class ChannelServlet extends MirthServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isUserLoggedIn(request)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				ChannelController channelController = ControllerFactory.getFactory().createChannelController();
				ObjectXMLSerializer serializer = new ObjectXMLSerializer();
				PrintWriter out = response.getWriter();
				String operation = request.getParameter("op");

				if (operation.equals("getChannel")) {
					response.setContentType("application/xml");
					Channel channel = (Channel) serializer.fromXML(request.getParameter("channel"));
					out.println(serializer.toXML(channelController.getChannel(channel)));
				} else if (operation.equals("updateChannel")) {
					response.setContentType("text/plain");
					Channel channel = (Channel) serializer.fromXML(request.getParameter("channel"));
					boolean override = Boolean.valueOf(request.getParameter("override")).booleanValue();
					// NOTE: This needs to be print rather than println to avoid the newline
					out.print(channelController.updateChannel(channel, override));
				} else if (operation.equals("removeChannel")) {
					Channel channel = (Channel) serializer.fromXML(request.getParameter("channel"));
					channelController.removeChannel(channel);
				} else if (operation.equals("getChannelSummary")) {
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
