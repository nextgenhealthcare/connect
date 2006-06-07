package com.webreach.mirth.server.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

public abstract class MirthServlet extends HttpServlet {
	public boolean isUserLoggedIn(HttpSession session) {
		return (session.getAttribute(UserServlet.SESSION_AUTHORIZED) != null) && (session.getAttribute(UserServlet.SESSION_AUTHORIZED).equals(true));
	}
}
