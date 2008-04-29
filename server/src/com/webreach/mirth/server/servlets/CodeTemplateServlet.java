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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.CodeTemplate;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.controllers.CodeTemplateController;

public class CodeTemplateServlet extends MirthServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isUserLoggedIn(request)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				CodeTemplateController codeTemplateController = CodeTemplateController.getInstance();
				ObjectXMLSerializer serializer = new ObjectXMLSerializer();
				PrintWriter out = response.getWriter();
				String operation = request.getParameter("op");

				if (operation.equals("getCodeTemplate")) {
					response.setContentType("application/xml");
					CodeTemplate codeTemplate = (CodeTemplate) serializer.fromXML(request.getParameter("codeTemplate"));
					out.println(serializer.toXML(codeTemplateController.getCodeTemplate(codeTemplate)));
				} else if (operation.equals("updateCodeTemplates")) {
					List<CodeTemplate> codeTemplates = (List<CodeTemplate>) serializer.fromXML(request.getParameter("codeTemplates"));
					codeTemplateController.updateCodeTemplates(codeTemplates);
				} else if (operation.equals("removeCodeTemplate")) {
					CodeTemplate codeTemplate = (CodeTemplate) serializer.fromXML(request.getParameter("codeTemplate"));
					codeTemplateController.removeCodeTemplate(codeTemplate);
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}
}
