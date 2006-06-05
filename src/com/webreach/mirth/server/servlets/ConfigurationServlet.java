package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.converters.ObjectSerializer;
import com.webreach.mirth.server.managers.ConfigurationManager;

public class ConfigurationServlet extends MirthServlet {
	private ConfigurationManager configurationManager = new ConfigurationManager();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isLoggedIn(request.getSession())) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				ObjectSerializer serializer = new ObjectSerializer();
				PrintWriter out = response.getWriter();
				String operation = request.getParameter("op");

				if (operation.equals("version")) {
					response.setContentType("text/plain");
					out.println("1.0");
				} else if (operation.equals("getChannels")) {
					response.setContentType("application/xml");
					out.println(serializer.toXML(configurationManager.getChannels(null)));
				} else if (operation.equals("updateChannel")) {
					String data = request.getParameter("data");
					configurationManager.updateChannel((Channel) serializer.fromXML(data));
				} else if (operation.equals("removeChannel")) {
					String data = request.getParameter("data");
					configurationManager.removeChannel(Integer.valueOf(data).intValue());
				} else if (operation.equals("getUsers")) {
					response.setContentType("application/xml");
					out.println(serializer.toXML(configurationManager.getUsers(null)));
				} else if (operation.equals("getTransports")) {
					response.setContentType("application/xml");
					out.println(serializer.toXML(configurationManager.getTransports()));
				} else if (operation.equals("updateUser")) {
					String data = request.getParameter("data");
					configurationManager.updateUser((User) serializer.fromXML(data));
				} else if (operation.equals("removeUser")) {
					String data = request.getParameter("data");
					configurationManager.removeUser(Integer.valueOf(data).intValue());
				} else if (operation.equals("getServerProperties")) {
					response.setContentType("application/xml");
					out.println(serializer.toXML(configurationManager.getServerProperties()));
				} else if (operation.equals("updateServerProperties")) {
					String data = request.getParameter("data");
					configurationManager.updateServerProperties((Properties) serializer.fromXML(data));
				} else if (operation.equals("getNextId")) {
					response.setContentType("text/plain");
					out.print(configurationManager.getNextId());
				} else if (operation.equals("deployChannels")) {
					configurationManager.deployChannels();
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}
}
