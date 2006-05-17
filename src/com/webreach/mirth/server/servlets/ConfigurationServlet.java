package com.webreach.mirth.server.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.bind.ChannelListMarshaller;
import com.webreach.mirth.model.bind.ChannelUnmarshaller;
import com.webreach.mirth.model.bind.PropertiesMarshaller;
import com.webreach.mirth.model.bind.PropertiesUnmarshaller;
import com.webreach.mirth.model.bind.Serializer;
import com.webreach.mirth.model.bind.UnmarshalException;
import com.webreach.mirth.model.bind.UserListMarshaller;
import com.webreach.mirth.model.bind.UserUnmarshaller;
import com.webreach.mirth.server.services.ConfigurationService;
import com.webreach.mirth.server.services.ServiceException;

public class ConfigurationServlet extends MirthServlet {
	private ConfigurationService configurationService = new ConfigurationService();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isLoggedIn(request.getSession())) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			PrintWriter out = response.getWriter();
			String method = request.getParameter("method");

			if (method.equals("version")) {
				response.setContentType("text/plain");
				out.println("1.0");
			} else if (method.equals("getChannels")) {
				response.setContentType("application/xml");
				out.println(getChannels());
			} else if (method.equals("updateChannel")) {
				String body = request.getParameter("body");
				updateChannel(body);
			} else if (method.equals("getUsers")) {
				response.setContentType("application/xml");
				out.println(getUsers());
			} else if (method.equals("updateUser")) {
				String body = request.getParameter("body");
				updateUser(body);
			} else if (method.equals("getProperties")) {
				response.setContentType("application/xml");
				out.println(getProperties());
			} else if (method.equals("updateProperties")) {
				String body = request.getParameter("body");
				updateProperties(body);
			} else if (method.equals("getNextId")) {
				response.setContentType("text/plain");
				out.print(getNextId());
			}
		}
	}

	private String getChannels() throws ServletException {
		ChannelListMarshaller marshaller = new ChannelListMarshaller();
		Serializer serializer = new Serializer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			serializer.serialize(marshaller.marshal(configurationService.getChannels(null)), null, os);
			return os.toString();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private void updateChannel(String channel) throws ServletException {
		ChannelUnmarshaller unmarshaller = new ChannelUnmarshaller();

		try {
			configurationService.updateChannel(unmarshaller.unmarshal(channel));
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (UnmarshalException e) {
			throw new ServletException(e);
		}
	}

	private String getUsers() throws ServletException {
		UserListMarshaller marshaller = new UserListMarshaller();
		Serializer serializer = new Serializer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			serializer.serialize(marshaller.marshal(configurationService.getUsers(null)), null, os);
			return os.toString();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private void updateUser(String user) throws ServletException {
		UserUnmarshaller unmarshaller = new UserUnmarshaller();

		try {
			configurationService.updateUser(unmarshaller.unmarshal(user));
		} catch (Exception e) {
			throw new ServletException();
		}
	}

	private String getProperties() throws ServletException {
		PropertiesMarshaller marshaller = new PropertiesMarshaller();
		Serializer serializer = new Serializer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			serializer.serialize(marshaller.marshal(configurationService.getProperties()), null, os);
			return os.toString();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private void updateProperties(String properties) throws ServletException {
		PropertiesUnmarshaller unmarshaller = new PropertiesUnmarshaller();

		try {
			configurationService.updateProperties(unmarshaller.unmarshal(properties));
		} catch (Exception e) {
			throw new ServletException();
		}
	}

	private int getNextId() throws ServletException {
		try {
			return configurationService.getNextId();
		} catch (ServiceException e) {
			throw new ServletException(e);
		}
	}
}
