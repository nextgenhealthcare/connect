package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.converters.ObjectSerializer;
import com.webreach.mirth.server.managers.LogEventStore;
import com.webreach.mirth.server.managers.MessageEventStore;

public class StoreServlet extends MirthServlet {
	private MessageEventStore messageEventStore = new MessageEventStore();
	private LogEventStore logEventStore = new LogEventStore();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isLoggedIn(request.getSession())) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				ObjectSerializer serializer = new ObjectSerializer();
				PrintWriter out = response.getWriter();
				String operation = request.getParameter("op");
				int channelId = Integer.parseInt(request.getParameter("id"));

				if (operation.equals("getLogEvents")) {
					response.setContentType("application/xml");
					out.println(serializer.toXML(logEventStore.getEvents(channelId)));
				} else if (operation.equals("getMessageEvents")) {
					response.setContentType("application/xml");
					out.println(serializer.toXML(messageEventStore.getMessageEvents(channelId)));
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}
}
