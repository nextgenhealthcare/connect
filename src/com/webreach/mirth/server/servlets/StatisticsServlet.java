package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.Statistics;
import com.webreach.mirth.server.services.ServiceException;
import com.webreach.mirth.server.services.StatisticsService;

public class StatisticsServlet extends MirthServlet {
	private StatisticsService statisticsService = new StatisticsService();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isLoggedIn(request.getSession())) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			PrintWriter out = response.getWriter();
			String operation = request.getParameter("op");
			int id = Integer.parseInt(request.getParameter("id"));

			if (operation.equals("getChannelStatistics")) {
				response.setContentType("application/xml");
				out.println(getChannelStatistics(id));
			} else if (operation.equals("clearChannelStatistics")) {
				clearChannelStatistics(id);
			}
		}
	}

	private Statistics getChannelStatistics(int id) throws ServletException {
		try {
			return statisticsService.getChannelStatistics(id);
		} catch (ServiceException e) {
			throw new ServletException(e);
		}
	}
	
	private void clearChannelStatistics(int id) throws ServletException {
		try {
			statisticsService.clearChannelStatistics(id);
		} catch (ServiceException e) {
			throw new ServletException(e);
		}
	}
}
