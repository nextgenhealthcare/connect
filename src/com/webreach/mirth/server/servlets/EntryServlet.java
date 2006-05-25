package com.webreach.mirth.server.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.bind.LogEntryListMarshaller;
import com.webreach.mirth.model.bind.MessageEntryListMarshaller;
import com.webreach.mirth.model.bind.Serializer;
import com.webreach.mirth.server.managers.LogEntryManager;
import com.webreach.mirth.server.managers.MessageEntryManager;

public class EntryServlet extends MirthServlet {
	private MessageEntryManager messageEntryManager = new MessageEntryManager();
	private LogEntryManager logEntryManager = new LogEntryManager();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isLoggedIn(request.getSession())) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			PrintWriter out = response.getWriter();
			String operation = request.getParameter("op");
			int id = Integer.parseInt(request.getParameter("id"));

			if (operation.equals("getLogEntries")) {
				response.setContentType("application/xml");
				out.println(getLogEntries(id));
			} else if (operation.equals("getLogEntries")) {
				response.setContentType("application/xml");
				out.println(getMessageEntries(id));
			}
		}
	}

	private String getLogEntries(int channelId) throws ServletException {
		LogEntryListMarshaller marshaller = new LogEntryListMarshaller();
		Serializer serializer = new Serializer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			serializer.serialize(marshaller.marshal(logEntryManager.getLogEntries(channelId)), null, os);
			return os.toString();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private String getMessageEntries(int channelId) throws ServletException {
		MessageEntryListMarshaller marshaller = new MessageEntryListMarshaller();
		Serializer serializer = new Serializer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			serializer.serialize(marshaller.marshal(messageEntryManager.getMessageEntries(channelId)), null, os);
			return os.toString();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
