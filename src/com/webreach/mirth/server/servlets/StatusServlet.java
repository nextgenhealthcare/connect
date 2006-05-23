package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.server.managers.ManagerException;
import com.webreach.mirth.server.managers.StatusManager;

public class StatusServlet extends MirthServlet {
	private StatusManager statusManager = new StatusManager();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isLoggedIn(request.getSession())) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			PrintWriter out = response.getWriter();
			String operation = request.getParameter("op");
			int id = Integer.parseInt(request.getParameter("id"));

			if (operation.equals("startChannel")) {
				startChannel(id);
			} else if (operation.equals("stopChannel")) {
				stopChannel(id);
			} else if (operation.equals("pauseChannel")) {
				pauseChannel(id);
			} else if (operation.equals("resumeChannel")) {
				resumeChannel(id);
			} else if (operation.equals("getChannelStatus")) {
				response.setContentType("text/plain");
				out.println(getChannelStatus(id));
			}
		}
	}

	private void startChannel(int id) throws ServletException {
		try {
			statusManager.startChannel(id);
		} catch (ManagerException e) {
			throw new ServletException(e);
		}
	}

	private void stopChannel(int id) throws ServletException {
		try {
			statusManager.stopChannel(id);
		} catch (ManagerException e) {
			throw new ServletException(e);
		}
	}

	private void pauseChannel(int id) throws ServletException {
		try {
			statusManager.pauseChannel(id);
		} catch (ManagerException e) {
			throw new ServletException(e);
		}
	}

	private void resumeChannel(int id) throws ServletException {
		try {
			statusManager.resumeChannel(id);
		} catch (ManagerException e) {
			throw new ServletException(e);
		}
	}

	private String getChannelStatus(int id) throws ServletException {
		try {
			return statusManager.getChannelStatus(id).toString();
		} catch (ManagerException e) {
			throw new ServletException(e);
		}
	}
}
