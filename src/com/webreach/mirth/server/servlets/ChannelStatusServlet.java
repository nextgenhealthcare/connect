package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.converters.ObjectSerializer;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.ChannelStatusController;

public class ChannelStatusServlet extends MirthServlet {
	private ChannelStatusController statusManager = new ChannelStatusController();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isUserLoggedIn(request.getSession())) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				ObjectSerializer serializer = new ObjectSerializer();
				PrintWriter out = response.getWriter();
				String operation = request.getParameter("op");

				if (operation.equals("startChannel")) {
					statusManager.startChannel(Integer.parseInt(request.getParameter("id")));
				} else if (operation.equals("stopChannel")) {
					statusManager.stopChannel(Integer.parseInt(request.getParameter("id")));
				} else if (operation.equals("pauseChannel")) {
					statusManager.pauseChannel(Integer.parseInt(request.getParameter("id")));
				} else if (operation.equals("resumeChannel")) {
					statusManager.resumeChannel(Integer.parseInt(request.getParameter("id")));
				} else if (operation.equals("getChannelStatusList")) {
					response.setContentType("application/xml");
					out.print(serializer.toXML(statusManager.getChannelStatusList()));
				}
			} catch (ControllerException e) {
				throw new ServletException(e);
			}
		}
	}
}
