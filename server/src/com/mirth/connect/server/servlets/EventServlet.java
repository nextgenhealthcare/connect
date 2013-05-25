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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.io.RuntimeIOException;

import com.mirth.connect.client.core.Operation;
import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.filters.EventFilter;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;

public class EventServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // MIRTH-1745
        response.setCharacterEncoding("UTF-8");
        
        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            try {
                EventController eventController = ControllerFactory.getFactory().createEventController();
                ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
                PrintWriter out = response.getWriter();
                Operation operation = Operations.getOperation(request.getParameter("op"));
                Map<String, Object> parameterMap = new HashMap<String, Object>();

                if (operation.equals(Operations.GET_MAX_EVENT_ID)) {
                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType(APPLICATION_XML);
                        out.print(eventController.getMaxEventId());
                    }
                } else if (operation.equals(Operations.GET_EVENTS)) {
                    EventFilter eventFilter = (EventFilter) serializer.fromXML(request.getParameter("filter"));
                    parameterMap.put("filter", eventFilter);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        int offset = Integer.parseInt(request.getParameter("offset"));
                        int limit = Integer.parseInt(request.getParameter("limit"));
                        response.setContentType(APPLICATION_XML);
                        serializer.toXML(eventController.getEvents(eventFilter, offset, limit), out);
                    }
                } else if (operation.equals(Operations.GET_EVENT_COUNT)) {
                    EventFilter eventFilter = (EventFilter) serializer.fromXML(request.getParameter("filter"));
                    parameterMap.put("filter", eventFilter);
                    
                    if (!isUserAuthorized(request, null)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType(APPLICATION_XML);
                        out.print(eventController.getEventCount(eventFilter));
                    }
                } else if (operation.equals(Operations.EVENT_REMOVE_ALL)) {
                    if (!isUserAuthorized(request, null)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        eventController.removeAllEvents();
                        
                        // Audit after removal
                        isUserAuthorized(request, null);
                    }
                } else if (operation.equals(Operations.EVENT_EXPORT_ALL)) {
                    if (!isUserAuthorized(request, null)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType(TEXT_PLAIN);
                        out.println(eventController.exportAllEvents());
                    }
                } else if (operation.equals(Operations.EVENT_EXPORT_AND_REMOVE_ALL)) {
                    if (!isUserAuthorized(request, null)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType(TEXT_PLAIN);
                        
                        // Add file path of export and audit after removal
                        String exportPath = eventController.exportAndRemoveAllEvents();
                        parameterMap.put("file", exportPath);
                        isUserAuthorized(request, parameterMap);
                        
                        out.println(exportPath);
                    }
                }
            } catch (RuntimeIOException rio) {
                logger.debug(rio);
            } catch (Throwable t) {
                logger.error(ExceptionUtils.getStackTrace(t));
                throw new ServletException(t);
            }
        }
    }
}
