package com.webreach.mirth.server.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

public abstract class MirthServlet extends HttpServlet {
	public boolean isLoggedIn(HttpSession session) {
		System.out.println("SERVLET CHECKING IF LOGGED IN");
		
		if (session.getAttribute(AuthenticationServlet.SESSION_AUTHORIZED) == null) {
			System.out.println("ITS NULL!");
		} else {
			System.out.println(session.getAttribute(AuthenticationServlet.SESSION_AUTHORIZED));
		}
		
		return (session.getAttribute(AuthenticationServlet.SESSION_AUTHORIZED) != null) && (session.getAttribute(AuthenticationServlet.SESSION_AUTHORIZED).equals(true));
	}
}
