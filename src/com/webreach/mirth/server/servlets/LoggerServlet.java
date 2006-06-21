package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.converters.ObjectSerializer;
import com.webreach.mirth.model.filters.MessageEventFilter;
import com.webreach.mirth.model.filters.SystemEventFilter;
import com.webreach.mirth.server.controllers.MessageLogger;
import com.webreach.mirth.server.controllers.SystemLogger;

public class LoggerServlet extends MirthServlet {
	private MessageLogger messageLogger = new MessageLogger();
	private SystemLogger systemLogger = new SystemLogger();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isUserLoggedIn(request.getSession())) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				ObjectSerializer serializer = new ObjectSerializer();
				PrintWriter out = response.getWriter();
				String operation = request.getParameter("op");

				if (operation.equals("getSystemEvents")) {
					String filter = request.getParameter("filter");
					response.setContentType("application/xml");
					out.println(serializer.toXML(systemLogger.getSystemEvents((SystemEventFilter) serializer.fromXML(filter))));
				} else if (operation.equals("getMessageEvents")) {
					String filter = request.getParameter("filter");
					response.setContentType("application/xml");
					out.println(serializer.toXML(messageLogger.getMessageEvents((MessageEventFilter) serializer.fromXML(filter))));
				} else if (operation.equals("clearSystemEvents")) {
					systemLogger.clearSystemEvents();
				} else if (operation.equals("removeMessageEvent")) {
					String messageEventId = request.getParameter("data");
					messageLogger.removeMessageEvent(Integer.valueOf(messageEventId).intValue());
				} else if (operation.equals("clearMessageEvents")) {
					String channelId = request.getParameter("data");
					messageLogger.clearMessageEvents(Integer.valueOf(channelId).intValue());
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}
}
