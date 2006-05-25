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
import com.webreach.mirth.model.bind.TransportMapMarshaller;
import com.webreach.mirth.model.bind.UserListMarshaller;
import com.webreach.mirth.model.bind.UserUnmarshaller;
import com.webreach.mirth.server.managers.ConfigurationManager;
import com.webreach.mirth.server.managers.ManagerException;

public class ConfigurationServlet extends MirthServlet {
	private ConfigurationManager configurationManager = new ConfigurationManager();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isLoggedIn(request.getSession())) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			PrintWriter out = response.getWriter();
			String operation = request.getParameter("op");

			if (operation.equals("version")) {
				response.setContentType("text/plain");
				out.println("1.0");
			} else if (operation.equals("getChannels")) {
				response.setContentType("application/xml");
				out.println(getChannels());
			} else if (operation.equals("updateChannel")) {
				String body = request.getParameter("body");
				updateChannel(body);
			} else if (operation.equals("removeChannel")) {
				String body = request.getParameter("body");
				removeChannel(Integer.valueOf(body).intValue());
			} else if (operation.equals("getUsers")) {
				response.setContentType("application/xml");
				out.println(getUsers());
			} else if (operation.equals("getTransports")) {
				response.setContentType("application/xml");
				out.println(getTransports());
			} else if (operation.equals("updateUser")) {
				String body = request.getParameter("body");
				updateUser(body);
			} else if (operation.equals("removeUser")) {
				String body = request.getParameter("body");
				removeUser(Integer.valueOf(body).intValue());
			} else if (operation.equals("getProperties")) {
				response.setContentType("application/xml");
				out.println(getProperties());
			} else if (operation.equals("updateProperties")) {
				String body = request.getParameter("body");
				updateProperties(body);
			} else if (operation.equals("getNextId")) {
				response.setContentType("text/plain");
				out.print(getNextId());
			} else if (operation.equals("deployChannels")) {
				deployChannels();
			}
		}
	}

	private String getChannels() throws ServletException {
		ChannelListMarshaller marshaller = new ChannelListMarshaller();
		Serializer serializer = new Serializer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			serializer.serialize(marshaller.marshal(configurationManager.getChannels(null)), null, os);
			return os.toString();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private void updateChannel(String channel) throws ServletException {
		ChannelUnmarshaller unmarshaller = new ChannelUnmarshaller();

		try {
			configurationManager.updateChannel(unmarshaller.unmarshal(channel));
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private void removeChannel(int channelId) throws ServletException {
		try {
			configurationManager.removeChannel(channelId);
		} catch (Exception e) {
			throw new ServletException();
		}
	}

	private String getTransports() throws ServletException {
		TransportMapMarshaller marshaller = new TransportMapMarshaller();
		Serializer serializer = new Serializer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			serializer.serialize(marshaller.marshal(configurationManager.getTransports()), null, os);
			return os.toString();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private String getUsers() throws ServletException {
		UserListMarshaller marshaller = new UserListMarshaller();
		Serializer serializer = new Serializer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			serializer.serialize(marshaller.marshal(configurationManager.getUsers(null)), null, os);
			return os.toString();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private void updateUser(String user) throws ServletException {
		UserUnmarshaller unmarshaller = new UserUnmarshaller();

		try {
			configurationManager.updateUser(unmarshaller.unmarshal(user));
		} catch (Exception e) {
			throw new ServletException();
		}
	}
	
	private void removeUser(int userId) throws ServletException {
		try {
			configurationManager.removeUser(userId);
		} catch (Exception e) {
			throw new ServletException();
		}
	}

	private String getProperties() throws ServletException {
		PropertiesMarshaller marshaller = new PropertiesMarshaller();
		Serializer serializer = new Serializer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			serializer.serialize(marshaller.marshal(configurationManager.getProperties()), null, os);
			return os.toString();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private void updateProperties(String properties) throws ServletException {
		PropertiesUnmarshaller unmarshaller = new PropertiesUnmarshaller();

		try {
			configurationManager.updateProperties(unmarshaller.unmarshal(properties));
		} catch (Exception e) {
			throw new ServletException();
		}
	}

	private int getNextId() throws ServletException {
		try {
			return configurationManager.getNextId();
		} catch (ManagerException e) {
			throw new ServletException(e);
		}
	}
	
	private void deployChannels() throws ServletException {
		try {
			configurationManager.deployChannels();
		} catch (ManagerException e) {
			throw new ServletException(e);
		}
	}
}
