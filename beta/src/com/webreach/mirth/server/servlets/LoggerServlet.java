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
import com.webreach.mirth.model.filters.MessageEventFilter;
import com.webreach.mirth.model.filters.SystemEventFilter;
import com.webreach.mirth.server.controllers.MessageLogger;
import com.webreach.mirth.server.controllers.SystemLogger;

public class LoggerServlet extends MirthServlet {
	private MessageLogger messageLogger = new MessageLogger();
	private SystemLogger systemLogger = new SystemLogger();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isUserLoggedIn(request.getSession())) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				ObjectXMLSerializer serializer = new ObjectXMLSerializer();
				PrintWriter out = response.getWriter();
				String operation = request.getParameter("op");

				if (operation.equals("getSystemEvents")) {
					String filter = request.getParameter("filter");
					response.setContentType("application/xml");
					out.println(serializer.serialize(systemLogger.getSystemEvents((SystemEventFilter) serializer.deserialize(filter))));
				} else if (operation.equals("getMessageEvents")) {
					String filter = request.getParameter("filter");
					response.setContentType("application/xml");
					out.println(serializer.serialize(messageLogger.getMessageEvents((MessageEventFilter) serializer.deserialize(filter))));
				} else if (operation.equals("clearSystemEvents")) {
					systemLogger.clearSystemEvents();
				} else if (operation.equals("removeMessageEvent")) {
					String messageEventId = request.getParameter("data");
					messageLogger.removeMessageEvent(Integer.valueOf(messageEventId).intValue());
				} else if (operation.equals("clearMessageEvents")) {
					String channelId = request.getParameter("data");
					messageLogger.clearMessageEvents(Integer.valueOf(channelId).intValue());
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}
}
