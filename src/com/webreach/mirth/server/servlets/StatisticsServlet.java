package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.server.managers.StatisticsManager;

public class StatisticsServlet extends MirthServlet {
	private StatisticsManager statisticsManager = new StatisticsManager();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isLoggedIn(request.getSession())) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				PrintWriter out = response.getWriter();
				String operation = request.getParameter("op");
				int channelId = Integer.parseInt(request.getParameter("id"));

				if (operation.equals("getStatistics")) {
					response.setContentType("application/xml");
					out.println(statisticsManager.getStatistics(channelId));
				} else if (operation.equals("clearStatistics")) {
					statisticsManager.clearStatistics(channelId);
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}
}
