package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.webreach.mirth.server.managers.AuthenticationManager;
import com.webreach.mirth.server.managers.ManagerException;

public class AuthenticationServlet extends MirthServlet {
	public static final String SESSION_USER = "user";
	public static final String SESSION_AUTHORIZED = "authorized";
	public static final String ERROR_UNAUTHORIZED = "You are not logged in.";

	private AuthenticationManager authenticationManager = new AuthenticationManager();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		HttpSession session = request.getSession();
		String operation = request.getParameter("op");
		
		if (operation.equals("login")) {
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
	}

	private boolean login(HttpSession session, String username, String password) throws ServletException {
		try {
			int authenticateUserId = authenticationManager.authenticateUser(username, password);

			if (authenticateUserId >= 0) {
				session.setAttribute(SESSION_USER, authenticateUserId);
				session.setAttribute(SESSION_AUTHORIZED, true);
				return true;
			}

			return false;
		} catch (ManagerException e) {
			throw new ServletException(e);
		}
	}

	private void logout(HttpSession session) {
		session.removeAttribute(SESSION_USER);
		session.removeAttribute(SESSION_AUTHORIZED);
		session.invalidate();
	}
}
