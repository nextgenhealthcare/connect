package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.converters.ObjectSerializer;
import com.webreach.mirth.server.managers.SystemLogger;
import com.webreach.mirth.server.managers.MessageLogger;

public class LoggerServlet extends MirthServlet {
	private MessageLogger messageLogger = new MessageLogger();
	private SystemLogger systemLogger = new SystemLogger();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isLoggedIn(request.getSession())) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				ObjectSerializer serializer = new ObjectSerializer();
				PrintWriter out = response.getWriter();
				String operation = request.getParameter("op");
				int channelId = Integer.parseInt(request.getParameter("id"));

				if (operation.equals("getSystemEventList")) {
					response.setContentType("application/xml");
					out.println(serializer.toXML(systemLogger.getSystemEvents(channelId)));
				} else if (operation.equals("getMessageEventList")) {
					response.setContentType("application/xml");
					out.println(serializer.toXML(messageLogger.getMessageEvents(channelId)));
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}
}
