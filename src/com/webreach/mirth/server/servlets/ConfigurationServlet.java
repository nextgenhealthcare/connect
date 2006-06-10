package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.converters.ObjectSerializer;
import com.webreach.mirth.server.controllers.ConfigurationController;

public class ConfigurationServlet extends MirthServlet {
	private ConfigurationController configurationController = new ConfigurationController();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isUserLoggedIn(request.getSession())) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				ObjectSerializer serializer = new ObjectSerializer();
				PrintWriter out = response.getWriter();
				String operation = request.getParameter("op");

				if (operation.equals("getTransports")) {
					response.setContentType("application/xml");
					out.println(serializer.toXML(configurationController.getTransports()));
				} else if (operation.equals("getServerProperties")) {
					response.setContentType("application/xml");
					out.println(serializer.toXML(configurationController.getServerProperties()));
				} else if (operation.equals("updateServerProperties")) {
					String properties = request.getParameter("data");
					configurationController.updateServerProperties((Properties) serializer.fromXML(properties));
				} else if (operation.equals("getNextId")) {
					response.setContentType("text/plain");
					out.print(configurationController.getNextId());
				} else if (operation.equals("deployChannels")) {
					configurationController.deployChannels();
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}
}
