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
import com.webreach.mirth.model.filters.MessageObjectFilter;
import com.webreach.mirth.server.controllers.MessageObjectController;

public class MessageObjectServlet extends MirthServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isUserLoggedIn(request)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				MessageObjectController messageObjectController = new MessageObjectController();
				ObjectXMLSerializer serializer = new ObjectXMLSerializer();
				PrintWriter out = response.getWriter();
				String operation = request.getParameter("op");

				if (operation.equals("createMessageTempTable")) {
					String filter = request.getParameter("filter");
					response.setContentType("application/xml");
					messageObjectController.createTempMessagesTable((MessageObjectFilter) serializer.fromXML(filter));
				} else if (operation.equals("getMessagesByPage")) {
					String page = request.getParameter("page");
					String pageSize = request.getParameter("pageSize");
					response.setContentType("application/xml");
					out.print(messageObjectController.getMessagesByPage(Integer.parseInt(page), Integer.parseInt(pageSize)));
				} else if (operation.equals("getMessageCount")) {
					String filter = request.getParameter("filter");
					response.setContentType("text/plain");
					out.print(messageObjectController.getMessageCount((MessageObjectFilter) serializer.fromXML(filter)));
				} else if (operation.equals("removeMessages")) {
					String filter = request.getParameter("filter");
					messageObjectController.removeMessages((MessageObjectFilter) serializer.fromXML(filter));
				} else if (operation.equals("clearMessages")) {
					String channelId = request.getParameter("data");
					messageObjectController.clearMessages(channelId);
				} else if (operation.equals("reprocessMessages")) {
					String filter = request.getParameter("filter");
					messageObjectController.reprocessMessages((MessageObjectFilter) serializer.fromXML(filter));
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}
}
