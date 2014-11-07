/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.io.RuntimeIOException;

import com.mirth.connect.client.core.Operation;
import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.UsageController;

public class UsageServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if (!isUserLoggedIn(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } else {
                PrintWriter out = response.getWriter();
                ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
                Operation operation = Operations.getOperation(request.getParameter("op"));
                UsageController usageController = ControllerFactory.getFactory().createUsageController();
                
                if (operation.equals(Operations.USAGE_DATA_GET)) {
                    response.setContentType(TEXT_PLAIN);
                    if (isUserAuthorized(request, null)) {
                        serializer.serialize(usageController.createUsageStats(), out);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            }
        } catch (RuntimeIOException rio) {
            logger.debug(rio);
        } catch (Throwable t) {
            logger.debug(ExceptionUtils.getStackTrace(t));
            throw new ServletException(t);
        }
    }
}
