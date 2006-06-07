package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.webreach.mirth.model.User;
import com.webreach.mirth.model.converters.ObjectSerializer;
import com.webreach.mirth.server.managers.UserController;
import com.webreach.mirth.server.managers.ControllerException;

public class UserServlet extends MirthServlet {
	public static final String SESSION_USER = "user";
	public static final String SESSION_AUTHORIZED = "authorized";
	public static final String ERROR_UNAUTHORIZED = "You are not logged in.";

	private UserController userController = new UserController();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		HttpSession session = request.getSession();
		String operation = request.getParameter("op");
		ObjectSerializer serializer = new ObjectSerializer();
		try {
			if (operation.equals("getUsers")) {
				response.setContentType("application/xml");
				out.println(serializer.toXML(userController.getUsers(null)));
			} else if (operation.equals("updateUser")) {
				String data = request.getParameter("data");
				userController.updateUser((User) serializer.fromXML(data));
			} else if (operation.equals("removeUser")) {
				String data = request.getParameter("data");
				userController.removeUser(Integer.valueOf(data).intValue());
			} else if (operation.equals("login")) {
				String username = request.getParameter("username");
				String password = request.getParameter("password");
				response.setContentType("text/plain");
				out.print(login(session, username, password));
			} else if (operation.equals("logout")) {
				logout(session);
			} else if (operation.equals("isLoggedIn")) {
				System.out.println("SEVLET CHECKING IF LOGGED IN");
				response.setContentType("text/plain");
				out.print(isLoggedIn(session));
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private boolean login(HttpSession session, String username, String password) throws ServletException {
		try {
			int authenticateUserId = userController.authenticateUser(username, password);

			if (authenticateUserId >= 0) {
				session.setAttribute(SESSION_USER, authenticateUserId);
				session.setAttribute(SESSION_AUTHORIZED, true);
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
	}
}
