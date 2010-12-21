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
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ScriptController;

public class ConfigurationServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            PrintWriter out = response.getWriter();
            String operation = request.getParameter("op");

            if (operation.equals(Operations.CONFIGURATION_STATUS_GET)) {
                response.setContentType("text/plain");
                out.println(ControllerFactory.getFactory().createConfigurationController().getStatus());
            } else if (!isUserLoggedIn(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } else {
                ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
                ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();

                if (operation.equals(Operations.CONFIGURATION_CHARSET_ENCODINGS_GET)) {
                    if (isUserAuthorized(request)) {
                        response.setContentType("application/xml");
                        out.println(serializer.toXML(configurationController.getAvaiableCharsetEncodings()));
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_SERVER_PROPERTIES_GET)) {
                    response.setContentType("application/xml");
                    
                    if (isUserAuthorized(request)) {
                        out.println(serializer.toXML(configurationController.getServerProperties()));
                    } else {
                        Properties allServerProperties = configurationController.getServerProperties();
                        Properties permittedServerProperties = new Properties();
                        
                        for (Object key : allServerProperties.keySet()) {
                            String property = (String) key;
                            
                            if ("firstlogin".equals(property) || "update.enabled".equals(property) || "stats.enabled".equals(property) || "update.url".equals(property)) {
                                permittedServerProperties.setProperty(property, allServerProperties.getProperty(property));
                            }
                        }
                        
                        out.println(serializer.toXML(permittedServerProperties));
                    }
                } else if (operation.equals(Operations.CONFIGURATION_SERVER_PROPERTIES_SET)) {
                    if (isUserAuthorized(request)) {
                        String properties = request.getParameter("data");
                        configurationController.setServerProperties((Properties) serializer.fromXML(properties));
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_GUID_GET)) {
                    if (isUserAuthorized(request)) {
                        response.setContentType("text/plain");
                        out.print(configurationController.generateGuid());
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_DATABASE_DRIVERS_GET)) {
                    if (isUserAuthorized(request)) {
                        response.setContentType("application/xml");
                        out.println(serializer.toXML(configurationController.getDatabaseDrivers()));
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_VERSION_GET)) {
                    if (isUserAuthorized(request)) {
                        response.setContentType("text/plain");
                        out.print(configurationController.getServerVersion());
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_BUILD_DATE_GET)) {
                    if (isUserAuthorized(request)) {
                        response.setContentType("text/plain");
                        out.print(configurationController.getBuildDate());
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.SERVER_CONFIGURATION_GET)) {
                    if (isUserAuthorized(request)) {
                        response.setContentType("application/xml");
                        out.println(serializer.toXML(configurationController.getServerConfiguration()));
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.SERVER_CONFIGURATION_SET)) {
                    if (isUserAuthorized(request)) {
                        String serverConfiguration = request.getParameter("data");
                        configurationController.setServerConfiguration((ServerConfiguration) serializer.fromXML(serverConfiguration));
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_SERVER_ID_GET)) {
                    if (isUserAuthorized(request)) {
                        response.setContentType("application/xml");
                        out.println(configurationController.getServerId());
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_SERVER_TIMEZONE_GET)) {
                    if (isUserAuthorized(request)) {
                        response.setContentType("application/xml");
                        out.println(configurationController.getServerTimezone(request.getLocale()));
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.GLOBAL_SCRIPT_GET)) {
                    if (isUserAuthorized(request)) {
                        response.setContentType("application/xml");
                        out.println(serializer.toXML(scriptController.getGlobalScripts()));
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.GLOBAL_SCRIPT_SET)) {
                    if (isUserAuthorized(request)) {
                        String scripts = request.getParameter("scripts");
                        scriptController.setGlobalScripts((Map<String, String>) serializer.fromXML(scripts));
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONFIGURATION_PASSWORD_REQUIREMENTS_GET)) {
                    if (isUserAuthorized(request)) {
                        response.setContentType("application/xml");
                        out.println(serializer.toXML(configurationController.getPasswordRequirements()));
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            }
        } catch (Throwable t) {
            logger.error(ExceptionUtils.getStackTrace(t));
            throw new ServletException(t);
        }
    }
}
