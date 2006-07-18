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
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.SystemLogger;
import com.webreach.mirth.server.controllers.UserController;

public class UserServlet extends MirthServlet {
	public static final String SESSION_USER = "user";
	public static final String SESSION_AUTHORIZED = "authorized";
	private UserController userController = new UserController();
	private SystemLogger systemLogger = new SystemLogger();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		HttpSession session = request.getSession();
		String operation = request.getParameter("op");
		ObjectXMLSerializer serializer = new ObjectXMLSerializer();

		if (operation.equals("login")) {
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			response.setContentType("text/plain");
			out.print(login(session, username, password));
		} else if (operation.equals("isLoggedIn")) {
			response.setContentType("text/plain");
			out.print(isUserLoggedIn(session));
		} else if (!isUserLoggedIn(request.getSession())) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				if (operation.equals("getUsers")) {
					response.setContentType("application/xml");
					out.println(serializer.serialize(userController.getUsers(null)));
				} else if (operation.equals("updateUser")) {
					String user = request.getParameter("data");
					userController.updateUser((User) serializer.deserialize(user));
				} else if (operation.equals("removeUser")) {
					String userId = request.getParameter("data");
					userController.removeUser(Integer.valueOf(userId).intValue());
				} else if (operation.equals("logout")) {
					logout(session);
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}

	private boolean login(HttpSession session, String username, String password) throws ServletException {
		try {
			int authenticateUserId = userController.authenticateUser(username, password);

			if (authenticateUserId >= 0) {
				session.setAttribute(SESSION_USER, authenticateUserId);
				session.setAttribute(SESSION_AUTHORIZED, true);

				// log the event
				SystemEvent event = new SystemEvent("User logged in.");
				event.getAttributes().put("Session ID", session.getId());
				event.getAttributes().put("User ID", String.valueOf(authenticateUserId));
				event.getAttributes().put("User Name", username);
				systemLogger.logSystemEvent(event);

				return true;
			}

			return false;
		} catch (ControllerException e) {
			throw new ServletException(e);
		}

	}

	private void logout(HttpSession session) {
		session.removeAttribute(SESSION_USER);
		session.removeAttribute(SESSION_AUTHORIZED);
		session.invalidate();

		// log the event
		SystemEvent event = new SystemEvent("User logged out.");
		event.getAttributes().put("Session ID", session.getId());
		systemLogger.logSystemEvent(event);
	}
}
