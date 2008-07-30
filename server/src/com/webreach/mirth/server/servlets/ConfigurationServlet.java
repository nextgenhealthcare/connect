/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.ServerConfiguration;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.controllers.ConfigurationController;

public class ConfigurationServlet extends MirthServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            PrintWriter out = response.getWriter();
            String operation = request.getParameter("op");

            if (operation.equals("getStatus")) {
                response.setContentType("text/plain");
                out.println(ConfigurationController.getInstance().getStatus());
            } else if (!isUserLoggedIn(request)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                ConfigurationController configurationController = ConfigurationController.getInstance();
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
                } else if (operation.equals("deployChannels")) {
                    configurationController.deployChannels();
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
                }

            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
