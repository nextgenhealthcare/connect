package com.webreach.mirth.server.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public abstract class MirthServlet extends HttpServlet {
	public boolean isUserLoggedIn(HttpServletRequest request) {
		HttpSession session = request.getSession();
		return (session.getAttribute(UserServlet.SESSION_AUTHORIZED) != null) && (session.getAttribute(UserServlet.SESSION_AUTHORIZED).equals(true));
	}
}
