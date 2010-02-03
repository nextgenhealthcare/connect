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
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ServerConfiguration;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerFactory;

public class ConfigurationServlet extends MirthServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            PrintWriter out = response.getWriter();
            String operation = request.getParameter("op");

            if (operation.equals("getStatus")) {
                response.setContentType("text/plain");
                out.println(ControllerFactory.getFactory().createConfigurationController().getStatus());
            } else if (!isUserLoggedIn(request)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();

                if (operation.equals("getAvaiableCharsetEncodings")) {
                    response.setContentType("application/xml");
                    out.println(serializer.toXML(configurationController.getAvaiableCharsetEncodings()));
                } else if (operation.equals("getServerProperties")) {
                    response.setContentType("application/xml");
                    out.println(serializer.toXML(configurationController.getServerProperties()));
                } else if (operation.equals("setServerProperties")) {
                    String properties = request.getParameter("data");
                    configurationController.setServerProperties((Properties) serializer.fromXML(properties));
                } else if (operation.equals("getGuid")) {
                    response.setContentType("text/plain");
                    out.print(configurationController.getGuid());
                } else if (operation.equals("redeployAllChannels")) {
                    configurationController.redeployAllChannels();
                } else if (operation.equals("deployChannels")) {
                    List<Channel> channels = (List<Channel>) serializer.fromXML(request.getParameter("channels"));
                    configurationController.deployChannels(channels);
                } else if (operation.equals("uneployChannels")) {
                    List<String> channelIds = (List<String>) serializer.fromXML(request.getParameter("channelIds"));
                    configurationController.undeployChannels(channelIds);
                } else if (operation.equals("getDatabaseDrivers")) {
                    response.setContentType("application/xml");
                    out.println(serializer.toXML(configurationController.getDatabaseDrivers()));
                } else if (operation.equals("getVersion")) {
                    response.setContentType("text/plain");
                    out.print(configurationController.getServerVersion());
                } else if (operation.equals("getBuildDate")) {
                    response.setContentType("text/plain");
                    out.print(configurationController.getBuildDate());
                } else if (operation.equals("getServerConfiguration")) {
                    response.setContentType("application/xml");
                    out.println(serializer.toXML(configurationController.getServerConfiguration()));
                } else if (operation.equals("setServerConfiguration")) {
                    String serverConfiguration = request.getParameter("data");
                    configurationController.setServerConfiguration((ServerConfiguration) serializer.fromXML(serverConfiguration));
                } else if (operation.equals("getServerId")) {
                    response.setContentType("application/xml");
                    out.println(configurationController.getServerId());
                } else if (operation.equals("getGlobalScripts")) {
                    response.setContentType("application/xml");
                    out.println(serializer.toXML(configurationController.getGlobalScripts()));
                } else if (operation.equals("setGlobalScripts")) {
                    String scripts = request.getParameter("scripts");
                    configurationController.setGlobalScripts((Map<String, String>) serializer.fromXML(scripts));
                } else if (operation.equals("shutdown")) {
                    configurationController.shutdown();
                } else if (operation.equals("getPasswordRequirements")) {
                    response.setContentType("application/xml");
                    out.println(serializer.toXML(configurationController.getPasswordRequirements()));
                }
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
