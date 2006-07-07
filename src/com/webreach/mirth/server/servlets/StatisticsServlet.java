package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.controllers.StatisticsController;

public class StatisticsServlet extends MirthServlet {
	private StatisticsController statisticsController = new StatisticsController();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isUserLoggedIn(request.getSession())) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				ObjectXMLSerializer serializer = new ObjectXMLSerializer();
				PrintWriter out = response.getWriter();
				String operation = request.getParameter("op");
				int channelId = Integer.parseInt(request.getParameter("id"));

				if (operation.equals("getStatistics")) {
					response.setContentType("application/xml");
					out.println(serializer.serialize(statisticsController.getStatistics(channelId)));
				} else if (operation.equals("clearStatistics")) {
					statisticsController.clearStatistics(channelId);
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}
}
