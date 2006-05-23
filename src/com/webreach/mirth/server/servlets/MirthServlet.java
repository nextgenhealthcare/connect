package com.webreach.mirth.server.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

public abstract class MirthServlet extends HttpServlet {
	public boolean isLoggedIn(HttpSession session) {
		return (session.getAttribute(AuthenticationServlet.SESSION_AUTHORIZED) != null) && (session.getAttribute(AuthenticationServlet.SESSION_AUTHORIZED).equals(true));
	}
}
