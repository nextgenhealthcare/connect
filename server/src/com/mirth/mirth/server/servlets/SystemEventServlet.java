/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.filters.SystemEventFilter;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.EventController;

public class SystemEventServlet extends MirthServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isUserLoggedIn(request)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				EventController systemLogger = ControllerFactory.getFactory().createEventController();
				ObjectXMLSerializer serializer = new ObjectXMLSerializer();
				PrintWriter out = response.getWriter();
				String operation = request.getParameter("op");
				String uid = null;
				boolean useNewTempTable = false;
				
				if (request.getParameter("uid") != null && !request.getParameter("uid").equals("")) {
					uid = request.getParameter("uid");
					useNewTempTable = true;
				}
				else {
					uid = request.getSession().getId();
				}

				if (operation.equals("createSystemEventsTempTable")) {
					String filter = request.getParameter("filter");
					response.setContentType("text/plain");
					out.println(systemLogger.createSystemEventsTempTable((SystemEventFilter) serializer.fromXML(filter), uid, useNewTempTable));
				} else if (operation.equals("removeFilterTables")) {
					systemLogger.removeFilterTable(uid);
				} else if (operation.equals("getSystemEventsByPage")) {
					String page = request.getParameter("page");
					String pageSize = request.getParameter("pageSize");
					String maxMessages = request.getParameter("maxSystemEvents");
					response.setContentType("application/xml");
					out.print(serializer.toXML(systemLogger.getSystemEventsByPage(Integer.parseInt(page), Integer.parseInt(pageSize), Integer.parseInt(maxMessages), uid)));
				} else if (operation.equals("getSystemEventsByPageLimit")) {
					String page = request.getParameter("page");
					String pageSize = request.getParameter("pageSize");
					String maxSystemEvents = request.getParameter("maxSystemEvents");
					String filter = request.getParameter("filter");
					response.setContentType("application/xml");
					out.print(serializer.toXML(systemLogger.getSystemEventsByPageLimit(Integer.parseInt(page), Integer.parseInt(pageSize), Integer.parseInt(maxSystemEvents), uid, (SystemEventFilter) serializer.fromXML(filter))));
				} else if (operation.equals("removeSystemEvents")) {
					String filter = request.getParameter("filter");
					systemLogger.removeSystemEvents((SystemEventFilter) serializer.fromXML(filter));
				} else if (operation.equals("clearSystemEvents")) {
					systemLogger.clearSystemEvents();
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}	
}
