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
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.converters.DocumentSerializer;
import com.webreach.mirth.util.PropertyLoader;

public class WebStartServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			DocumentSerializer docSerializer = new DocumentSerializer();
			PrintWriter out = response.getWriter();

			response.setContentType("application/x-java-jnlp-file");
			response.setHeader("Pragma", "no-cache");

			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("webapp/mirth-client.jnlp");
			Element jnlpElement = document.getDocumentElement();

			String scheme = request.getScheme();
			String serverName = request.getServerName();
			int serverPort = request.getServerPort();
			String contextPath = request.getContextPath();
			String codebase = scheme + "://" + serverName + ":" + serverPort + contextPath;
			
			Properties mirthProperties = PropertyLoader.loadProperties("mirth");
			
			String server;
			
			if ((mirthProperties.getProperty("server.url") != null) && !mirthProperties.getProperty("server.url").equals("")) {
				server = mirthProperties.getProperty("server.url"); 
			} else {
				int httpsPort = 8443;

				if ((mirthProperties.getProperty("https.port") != null) && !mirthProperties.getProperty("https.port").equals("")) {
					httpsPort = Integer.valueOf(mirthProperties.getProperty("https.port")).intValue();
				}

				server = "https://" + serverName + ":" + httpsPort;
			}
			
			jnlpElement.setAttribute("codebase", codebase);
			Element applicationDescElement = (Element) jnlpElement.getElementsByTagName("application-desc").item(0);
			Element argumentElement = document.createElement("argument");
			argumentElement.setTextContent(server);
			applicationDescElement.appendChild(argumentElement);

			out.println(docSerializer.toXML(document));
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
