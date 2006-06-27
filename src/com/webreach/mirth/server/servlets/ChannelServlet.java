package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.converters.ObjectSerializer;
import com.webreach.mirth.server.controllers.ChannelController;

public class ChannelServlet extends MirthServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ChannelController channelController = new ChannelController();
		
		if (!isUserLoggedIn(request.getSession())) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				ObjectSerializer serializer = new ObjectSerializer();
				PrintWriter out = response.getWriter();
				String operation = request.getParameter("op");

				if (operation.equals("getChannels")) {
					response.setContentType("application/xml");
					out.println(serializer.toXML(channelController.getChannels(null)));
				} else if (operation.equals("updateChannel")) {
					response.setContentType("text/plain");
					String channel = request.getParameter("data");
					boolean override = Boolean.valueOf(request.getParameter("override")).booleanValue();
					out.print(channelController.updateChannel((Channel) serializer.fromXML(channel), override));
				} else if (operation.equals("removeChannel")) {
					String channelId = request.getParameter("data");
					channelController.removeChannel(Integer.valueOf(channelId).intValue());
				} else if (operation.equals("exportChannel")) {
					String channelId = request.getParameter("data");
					response.setContentType("application/xml");
					out.println(channelController.exportChannel(Integer.valueOf(channelId).intValue()));
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}

}
