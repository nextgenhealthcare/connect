package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.server.services.ServiceException;
import com.webreach.mirth.server.services.StatusService;

public class StatusServlet extends MirthServlet {
	private StatusService statusService = new StatusService();

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
			statusService.startChannel(id);
		} catch (ServiceException e) {
			throw new ServletException(e);
		}
	}

	private void stopChannel(int id) throws ServletException {
		try {
			statusService.stopChannel(id);
		} catch (ServiceException e) {
			throw new ServletException(e);
		}
	}

	private void pauseChannel(int id) throws ServletException {
		try {
			statusService.pauseChannel(id);
		} catch (ServiceException e) {
			throw new ServletException(e);
		}
	}

	private void resumeChannel(int id) throws ServletException {
		try {
			statusService.resumeChannel(id);
		} catch (ServiceException e) {
			throw new ServletException(e);
		}
	}

	private String getChannelStatus(int id) throws ServletException {
		try {
			return statusService.getChannelStatus(id).toString();
		} catch (ServiceException e) {
			throw new ServletException(e);
		}
	}
}
