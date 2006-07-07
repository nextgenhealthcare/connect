package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.converters.ObjectXMLSerializer;
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
				ObjectXMLSerializer serializer = new ObjectXMLSerializer();
				PrintWriter out = response.getWriter();
				String operation = request.getParameter("op");

				if (operation.equals("getSystemEvents")) {
					String filter = request.getParameter("filter");
					response.setContentType("application/xml");
					out.println(serializer.serialize(systemLogger.getSystemEvents((SystemEventFilter) serializer.deserialize(filter))));
				} else if (operation.equals("getMessageEvents")) {
					String filter = request.getParameter("filter");
					response.setContentType("application/xml");
					out.println(serializer.serialize(messageLogger.getMessageEvents((MessageEventFilter) serializer.deserialize(filter))));
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
