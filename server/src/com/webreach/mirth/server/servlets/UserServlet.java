/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.EventController;
import com.webreach.mirth.server.controllers.UserController;

public class UserServlet extends MirthServlet {
	public static final String SESSION_USER = "user";
	public static final String SESSION_AUTHORIZED = "authorized";

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UserController userController = ControllerFactory.getFactory().createUserController();
		EventController systemLogger = ControllerFactory.getFactory().createEventController();
		PrintWriter out = response.getWriter();
		String operation = request.getParameter("op");
		ObjectXMLSerializer serializer = new ObjectXMLSerializer();

		if (operation.equals("login")) {
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			String version = request.getParameter("version");
			response.setContentType("text/plain");
			out.print(login(request, response, userController, systemLogger, username, password, version));
		} else if (operation.equals("isLoggedIn")) {
			response.setContentType("text/plain");
			out.print(isUserLoggedIn(request));
		} else if (!isUserLoggedIn(request)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				if (operation.equals("getUser")) {
					response.setContentType("application/xml");
					User user = (User) serializer.fromXML(request.getParameter("user"));
					out.println(serializer.toXML(userController.getUser(user)));
				} else if (operation.equals("updateUser")) {
					User user = (User) serializer.fromXML(request.getParameter("user"));
					String password = request.getParameter("password");
					userController.updateUser(user, password);
				} else if (operation.equals("removeUser")) {
					User user = (User) serializer.fromXML(request.getParameter("user"));
					userController.removeUser(user);
				} else if (operation.equals("authorizeUser")) {
					response.setContentType("text/plain");
					User user = (User) serializer.fromXML(request.getParameter("user"));
					String password = request.getParameter("password");
					out.print(userController.authorizeUser(user, password));
				} else if (operation.equals("logout")) {
					logout(request, userController, systemLogger);
				} else if (operation.equals("isUserLoggedIn")) {
					response.setContentType("text/plain");
					User user = (User) serializer.fromXML(request.getParameter("user"));
					out.print(userController.isUserLoggedIn(user));
				} else if (operation.equals("getUserPreferences")) {
                    response.setContentType("text/plain");
                    User user = (User) serializer.fromXML(request.getParameter("user"));
                    out.println(serializer.toXML(userController.getUserPreferences(user)));
                } else if (operation.equals("setUserPreference")) {
                    User user = (User) serializer.fromXML(request.getParameter("user"));
                    String name = (String) request.getParameter("name");
                    String value = (String) request.getParameter("value");
                    userController.setUserPreference(user, name, value);
                } 
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}

	private boolean login(HttpServletRequest request, HttpServletResponse response, UserController userController, EventController systemLogger, String username, String password, String version) throws ServletException {
		try {
			ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
			
			// if the version of the client in is not the same as the server and the version is not 0.0.0 (bypass)
			if (!version.equals(configurationController.getServerVersion()) && !version.equals("0.0.0")) {
				response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
				return false;
			}
			
			HttpSession session = request.getSession();
			
			User user = new User();
			user.setUsername(username);
			
			if (userController.authorizeUser(user, password)) {
				User validUser =  userController.getUser(user).get(0);
				
				// set the sessions attributes
				session.setAttribute(SESSION_USER, validUser.getId());
				session.setAttribute(SESSION_AUTHORIZED, true);
				
				// this prevents the session from timing out
				session.setMaxInactiveInterval(-1);

				// set the user status to logged in in the database
				userController.loginUser(validUser);

				// log the event
				SystemEvent event = new SystemEvent("User logged in.");
				event.getAttributes().put("Session ID", session.getId());
				event.getAttributes().put("User ID", validUser.getId());
				event.getAttributes().put("User Name", validUser.getUsername());
				systemLogger.logSystemEvent(event);
				
				return true;
			}

			// failed to login
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return false;
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private void logout(HttpServletRequest request, UserController userController, EventController systemLogger) throws ServletException {
		HttpSession session = request.getSession();

		// save the session id before removing them from the session
		Integer userId = (Integer) session.getAttribute(SESSION_USER);
		String sessionId = session.getId();
		
		// remove the sessions attributes
		session.removeAttribute(SESSION_USER);
		session.removeAttribute(SESSION_AUTHORIZED);
		
		// invalidate the current sessions
		session.invalidate();

		// set the user status to logged out in the database
		User user = new User();
		user.setId(userId);

		try {
			userController.logoutUser(user);	
		} catch (ControllerException ce) {
			throw new ServletException(ce);
		}
		
		// delete any temp tables created for this session
		ControllerFactory.getFactory().createMessageObjectController().removeFilterTable(sessionId);
		systemLogger.removeFilterTable(sessionId);
		
		// log the event
		SystemEvent event = new SystemEvent("User logged out.");
		event.getAttributes().put("Session ID", session.getId());
		systemLogger.logSystemEvent(event);
	}
}
