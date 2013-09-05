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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.io.RuntimeIOException;

import com.mirth.connect.client.core.Operation;
import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class AlertServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // MIRTH-1745
        response.setCharacterEncoding("UTF-8");

        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            try {
                AlertController alertController = ControllerFactory.getFactory().createAlertController();
                ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
                PrintWriter out = response.getWriter();
                Operation operation = Operations.getOperation(request.getParameter("op"));
                Map<String, Object> parameterMap = new HashMap<String, Object>();

                if (operation.equals(Operations.ALERT_GET)) {
                    String alertId = request.getParameter("alertId");
                    parameterMap.put("alertId", alertId);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType(APPLICATION_XML);

                        List<AlertModel> alerts;

                        if (StringUtils.isBlank(alertId)) {
                            alerts = alertController.getAlerts();
                        } else {
                            alerts = new ArrayList<AlertModel>();

                            AlertModel alert = alertController.getAlert(alertId);
                            if (alert != null) {
                                alerts.add(alert);
                            }
                        }

                        serializer.serialize(alerts, out);
                    }
                } else if (operation.equals(Operations.ALERT_UPDATE)) {
                    AlertModel alertModel = serializer.deserialize(request.getParameter("alertModel"), AlertModel.class);
                    parameterMap.put("alertModel", alertModel);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        alertController.updateAlert(alertModel);
                    }
                } else if (operation.equals(Operations.ALERT_REMOVE)) {
                    String alertId = request.getParameter("alertId");
                    parameterMap.put("alertId", alertId);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        alertController.removeAlert(alertId);
                    }
                } else if (operation.equals(Operations.ALERT_ENABLE)) {
                    String alertId = request.getParameter("alertId");
                    parameterMap.put("alertId", alertId);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        AlertModel alertModel = alertController.getAlert(alertId);
                        alertModel.setEnabled(true);
                        alertController.updateAlert(alertModel);
                    }
                } else if (operation.equals(Operations.ALERT_DISABLE)) {
                    String alertId = request.getParameter("alertId");
                    parameterMap.put("alertId", alertId);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        AlertModel alertModel = alertController.getAlert(alertId);
                        alertModel.setEnabled(false);
                        alertController.updateAlert(alertModel);
                    }
                } else if (operation.equals(Operations.ALERT_GET_STATUS)) {
                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType(APPLICATION_XML);

                        serializer.serialize(alertController.getAlertStatusList(), out);
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
