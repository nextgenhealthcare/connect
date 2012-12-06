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
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ScriptController;

public class ConfigurationServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // MIRTH-1745
        response.setCharacterEncoding("UTF-8");
        
        try {
            PrintWriter out = response.getWriter();
            Operation operation = Operations.getOperation(request.getParameter("op"));

            if (operation.equals(Operations.CONFIGURATION_STATUS_GET)) {
                response.setContentType(TEXT_PLAIN);
                out.println(ControllerFactory.getFactory().createConfigurationController().getStatus());
            } else if (!isUserLoggedIn(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } else {
                ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
                ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                Map<String, Object> parameterMap = new HashMap<String, Object>();

                if (operation.equals(Operations.CONFIGURATION_CHARSET_ENCODINGS_GET)) {
                    if (isUserAuthorized(request, null)) {
                        response.setContentType(APPLICATION_XML);
                        serializer.toXML(configurationController.getAvaiableCharsetEncodings(), out);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_SERVER_SETTINGS_GET)) {
                    response.setContentType(APPLICATION_XML);

                    if (isUserAuthorized(request, null)) {
                        serializer.toXML(configurationController.getServerSettings(), out);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_SERVER_SETTINGS_SET)) {
                    String settings = request.getParameter("data");
                    parameterMap.put("settings", settings);

                    if (isUserAuthorized(request, parameterMap)) {
                        configurationController.setServerSettings((ServerSettings) serializer.fromXML(settings));
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_UPDATE_SETTINGS_GET)) {
                    response.setContentType(APPLICATION_XML);

                    if (isUserAuthorized(request, null)) {
                        serializer.toXML(configurationController.getUpdateSettings(), out);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_UPDATE_SETTINGS_SET)) {
                    String settings = request.getParameter("data");
                    parameterMap.put("settings", settings);

                    if (isUserAuthorized(request, parameterMap)) {
                        configurationController.setUpdateSettings((UpdateSettings) serializer.fromXML(settings));
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_ENCRYPTION_SETTINGS_GET)) {
                    response.setContentType(APPLICATION_XML);

                    if (isUserAuthorized(request, null)) {
                        serializer.toXML(configurationController.getEncryptionSettings(), out);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_GUID_GET)) {
                    if (isUserAuthorized(request, null)) {
                        response.setContentType(TEXT_PLAIN);
                        out.print(configurationController.generateGuid());
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_DATABASE_DRIVERS_GET)) {
                    if (isUserAuthorized(request, null)) {
                        response.setContentType(APPLICATION_XML);
                        serializer.toXML(configurationController.getDatabaseDrivers(), out);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_VERSION_GET)) {
                    if (isUserAuthorized(request, null)) {
                        response.setContentType(TEXT_PLAIN);
                        out.print(configurationController.getServerVersion());
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_BUILD_DATE_GET)) {
                    if (isUserAuthorized(request, null)) {
                        response.setContentType(TEXT_PLAIN);
                        out.print(configurationController.getBuildDate());
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.SERVER_CONFIGURATION_GET)) {
                    if (isUserAuthorized(request, null)) {
                        response.setContentType(APPLICATION_XML);
                        serializer.toXML(configurationController.getServerConfiguration(), out);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.SERVER_CONFIGURATION_SET)) {
                    String serverConfiguration = request.getParameter("data");
                    parameterMap.put("data", serverConfiguration);

                    if (isUserAuthorized(request, parameterMap)) {
                        configurationController.setServerConfiguration((ServerConfiguration) serializer.fromXML(serverConfiguration));
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_SERVER_ID_GET)) {
                    if (isUserAuthorized(request, null)) {
                        response.setContentType(APPLICATION_XML);
                        out.println(configurationController.getServerId());
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_SERVER_TIMEZONE_GET)) {
                    if (isUserAuthorized(request, null)) {
                        response.setContentType(APPLICATION_XML);
                        out.println(configurationController.getServerTimezone(request.getLocale()));
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.GLOBAL_SCRIPT_GET)) {
                    if (isUserAuthorized(request, null)) {
                        response.setContentType(APPLICATION_XML);
                        serializer.toXML(scriptController.getGlobalScripts(), out);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.GLOBAL_SCRIPT_SET)) {
                    String scripts = request.getParameter("scripts");
                    parameterMap.put("scripts", scripts);

                    if (isUserAuthorized(request, parameterMap)) {
                        scriptController.setGlobalScripts((Map<String, String>) serializer.fromXML(scripts));
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_PASSWORD_REQUIREMENTS_GET)) {
                    if (isUserAuthorized(request, null)) {
                        response.setContentType(APPLICATION_XML);
                        serializer.toXML(configurationController.getPasswordRequirements(), out);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
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
