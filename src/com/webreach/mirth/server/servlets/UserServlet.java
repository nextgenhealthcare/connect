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

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UserController userController = new UserController();

		PrintWriter out = response.getWriter();
		HttpSession session = request.getSession();
		String operation = request.getParameter("op");
		ObjectXMLSerializer serializer = new ObjectXMLSerializer();
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
			} else if (operation.equals("login")) {
				String username = request.getParameter("username");
				String password = request.getParameter("password");
				response.setContentType("text/plain");
				out.print(login(session, username, password));
			} else if (operation.equals("logout")) {
				logout(session);
			} else if (operation.equals("isLoggedIn")) {
				response.setContentType("text/plain");
				out.print(isUserLoggedIn(session));
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private boolean login(HttpSession session, String username, String password) throws ServletException {
		try {
			UserController userController = new UserController();
			int authenticateUserId = userController.authenticateUser(username, password);

			if (authenticateUserId >= 0) {
				session.setAttribute(SESSION_USER, authenticateUserId);
				session.setAttribute(SESSION_AUTHORIZED, true);

				// log the event
				SystemLogger systemLogger = new SystemLogger();
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
		SystemLogger systemLogger = new SystemLogger();
		SystemEvent event = new SystemEvent("User logged out.");
		event.getAttributes().put("Session ID", session.getId());
		systemLogger.logSystemEvent(event);
	}
}
