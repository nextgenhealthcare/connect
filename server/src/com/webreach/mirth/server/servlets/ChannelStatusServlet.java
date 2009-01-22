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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.controllers.ChannelStatusController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.ControllerFactory;

public class ChannelStatusServlet extends MirthServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            try {
                ChannelStatusController channelStatusController = ControllerFactory.getFactory().createChannelStatusController();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                PrintWriter out = response.getWriter();
                String operation = request.getParameter("op");

                if (operation.equals("startChannel")) {
                    channelStatusController.startChannel(request.getParameter("id"));
                } else if (operation.equals("stopChannel")) {
                    channelStatusController.stopChannel(request.getParameter("id"));
                } else if (operation.equals("pauseChannel")) {
                    channelStatusController.pauseChannel(request.getParameter("id"));
                } else if (operation.equals("resumeChannel")) {
                    channelStatusController.resumeChannel(request.getParameter("id"));
                } else if (operation.equals("getChannelStatusList")) {
                    response.setContentType("application/xml");
                    out.print(serializer.toXML(channelStatusController.getChannelStatusList()));
                }
            } catch (ControllerException e) {
                throw new ServletException(e);
            }
        }
    }
}
