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
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.Alert;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class AlertServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            try {
                AlertController alertController = ControllerFactory.getFactory().createAlertController();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                PrintWriter out = response.getWriter();
                String operation = request.getParameter("op");
                Map<String, Object> parameterMap = new HashMap<String, Object>();

                if (operation.equals(Operations.ALERT_GET)) {
                    Alert alert = (Alert) serializer.fromXML(request.getParameter("alert"));
                    parameterMap.put("alert", alert);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType("application/xml");
                        out.println(serializer.toXML(alertController.getAlert(alert)));
                    }
                } else if (operation.equals(Operations.ALERT_UPDATE)) {
                    List<Alert> alerts = (List<Alert>) serializer.fromXML(request.getParameter("alerts"));
                    parameterMap.put("alerts", alerts);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        alertController.updateAlerts(alerts);
                    }
                } else if (operation.equals(Operations.ALERT_REMOVE)) {
                    Alert alert = (Alert) serializer.fromXML(request.getParameter("alert"));
                    parameterMap.put("alert", alert);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        alertController.removeAlert(alert);
                    }
                }
            } catch (Throwable t) {
                logger.error(ExceptionUtils.getStackTrace(t));
                throw new ServletException(t);
            }
        }
    }
}
