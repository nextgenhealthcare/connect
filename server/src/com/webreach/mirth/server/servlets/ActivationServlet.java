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
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.converters.DocumentSerializer;
import com.webreach.mirth.server.tools.ClassPathResource;

public class ActivationServlet extends HttpServlet {

    /*
     * Override last modified time to always be modified so it updates changes to JNLP.
     */
    protected long getLastModified(HttpServletRequest arg0) {
        return System.currentTimeMillis();
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			DocumentSerializer docSerializer = new DocumentSerializer();
			PrintWriter out = response.getWriter();

			response.setContentType("application/x-java-jnlp-file");
            response.setHeader("Pragma", "no-cache");

			// Cannot get the real path if it is not in the classpath.
            // If it is null, try it with just the filename.
            String jnlpPath = "activation.jnlp";
            URI jnlpURI = ClassPathResource.getResourceURI(jnlpPath);
            if (jnlpURI != null) {
            	jnlpPath = ClassPathResource.getResourceURI(jnlpPath).toString();
            }
			
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(jnlpPath);
			Element jnlpElement = document.getDocumentElement();

			String scheme = request.getScheme();
			String serverName = request.getServerName();
			int serverPort = request.getServerPort();
			String contextPath = request.getContextPath();
			String codebase = scheme + "://" + serverName + ":" + serverPort + contextPath;
			
			jnlpElement.setAttribute("codebase", codebase);
			out.println(docSerializer.toXML(document));
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
