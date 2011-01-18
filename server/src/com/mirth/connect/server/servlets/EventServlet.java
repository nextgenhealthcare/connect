/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.filters.EventFilter;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;

public class EventServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else if (!isUserAuthorized(request)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            try {
                EventController eventController = ControllerFactory.getFactory().createEventController();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                PrintWriter out = response.getWriter();
                String operation = request.getParameter("op");
                String uid = null;
                boolean useNewTempTable = false;

                if (StringUtils.isNotBlank(request.getParameter("uid"))) {
                    uid = request.getParameter("uid");
                    useNewTempTable = true;
                } else {
                    uid = request.getSession().getId();
                }

                if (operation.equals(Operations.EVENT_CREATE_TEMP_TABLE)) {
                    String filter = request.getParameter("filter");
                    response.setContentType("text/plain");
                    out.println(eventController.createEventTempTable((EventFilter) serializer.fromXML(filter), uid, useNewTempTable));
                } else if (operation.equals(Operations.EVENT_REMOVE_FILTER_TABLES)) {
                    eventController.removeEventFilterTable(uid);
                } else if (operation.equals(Operations.EVENT_GET_BY_PAGE)) {
                    String page = request.getParameter("page");
                    String pageSize = request.getParameter("pageSize");
                    String maxMessages = request.getParameter("maxEvents");
                    response.setContentType("application/xml");
                    out.print(serializer.toXML(eventController.getEventsByPage(Integer.parseInt(page), Integer.parseInt(pageSize), Integer.parseInt(maxMessages), uid)));
                } else if (operation.equals(Operations.EVENT_GET_BY_PAGE_LIMIT)) {
                    String page = request.getParameter("page");
                    String pageSize = request.getParameter("pageSize");
                    String maxSystemEvents = request.getParameter("maxEvents");
                    String filter = request.getParameter("filter");
                    response.setContentType("application/xml");
                    out.print(serializer.toXML(eventController.getEventsByPageLimit(Integer.parseInt(page), Integer.parseInt(pageSize), Integer.parseInt(maxSystemEvents), uid, (EventFilter) serializer.fromXML(filter))));
                } else if (operation.equals(Operations.EVENT_CLEAR)) {
                    eventController.clearEvents();
                }
            } catch (Throwable t) {
                logger.error(ExceptionUtils.getStackTrace(t));
                throw new ServletException(t);
            }
        }
    }
}
