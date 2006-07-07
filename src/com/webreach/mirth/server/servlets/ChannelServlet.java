package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.controllers.ChannelController;

public class ChannelServlet extends MirthServlet {
	private ChannelController channelController = new ChannelController();
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isUserLoggedIn(request.getSession())) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				ObjectXMLSerializer serializer = new ObjectXMLSerializer();
				PrintWriter out = response.getWriter();
				String operation = request.getParameter("op");

				if (operation.equals("getChannels")) {
					response.setContentType("application/xml");
					out.println(serializer.serialize(channelController.getChannels(null)));
				} else if (operation.equals("updateChannel")) {
					response.setContentType("text/plain");
					String channel = request.getParameter("data");
					boolean override = Boolean.valueOf(request.getParameter("override")).booleanValue();
					out.print(channelController.updateChannel((Channel) serializer.deserialize(channel), override));
				} else if (operation.equals("removeChannel")) {
					String channelId = request.getParameter("data");
					channelController.removeChannel(Integer.valueOf(channelId).intValue());
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}

}
