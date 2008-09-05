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
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.converters.DocumentSerializer;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.ExtensionController;
import com.webreach.mirth.server.tools.ClassPathResource;
import com.webreach.mirth.util.PropertyLoader;

public class WebStartServlet extends HttpServlet {
    
	/*
     * Override last modified time to always be modified so it updates changes to JNLP.
	 */
	protected long getLastModified(HttpServletRequest arg0)	{
	    return System.currentTimeMillis();
	}
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
            ExtensionController pluginController = ControllerFactory.getFactory().createExtensionController();
            
			DocumentSerializer docSerializer = new DocumentSerializer();
			PrintWriter out = response.getWriter();
			
			response.setContentType("application/x-java-jnlp-file");
            response.setHeader("Pragma", "no-cache");

			// Cannot get the real path if it is not in the classpath.
            // If it is null, try it with just the filename.
            String jnlpPath = "mirth-client.jnlp";
            URI jnlpURI = ClassPathResource.getResourceURI(jnlpPath);
            if (jnlpURI != null) {
            	jnlpPath = ClassPathResource.getResourceURI(jnlpPath).toString();
            }

			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(jnlpPath);
			Element jnlpElement = document.getDocumentElement();

			// Change the title to include the version of Mirth
			Properties versionProperties = PropertyLoader.loadProperties("version");
			String version = PropertyLoader.getProperty(versionProperties, "mirth.version");
			Element informationElement = (Element) jnlpElement.getElementsByTagName("information").item(0);
			Element title = (Element) informationElement.getElementsByTagName("title").item(0);
			title.setTextContent(title.getTextContent() + " " + version);

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

				// Load the context path property and remove the last char if it is a '/'.
				String contextPathProp = PropertyLoader.getProperty(mirthProperties, "context.path");
				if (contextPathProp.lastIndexOf('/') == (contextPathProp.length() - 1)) {
					contextPathProp = contextPathProp.substring(0, contextPathProp.length() - 1);
				}
				
				server = "https://" + serverName + ":" + httpsPort + contextPath;
			}

			jnlpElement.setAttribute("codebase", codebase);
			Element applicationDescElement = (Element) jnlpElement.getElementsByTagName("application-desc").item(0);
			Element serverArgumentElement = document.createElement("argument");
			serverArgumentElement.setTextContent(server);
			applicationDescElement.appendChild(serverArgumentElement);
			Element versionArgumentElement = document.createElement("argument");
			versionArgumentElement.setTextContent(version);
			applicationDescElement.appendChild(versionArgumentElement);
			
			// add the connector client jars to the classpath
			Element resourcesElement = (Element) jnlpElement.getElementsByTagName("resources").item(0);
			
            List<String> connectorLibraries = pluginController.getConnectorLibraries();
            List<String> pluginLibraries = pluginController.getPluginLibraries();
            
			for (Iterator iter = connectorLibraries.iterator(); iter.hasNext();) {
				String lib = (String) iter.next();

				Element jarElement = document.createElement("jar");
				jarElement.setAttribute("download", "eager");
				jarElement.setAttribute("href", "connectors/" + lib);
				
				resourcesElement.appendChild(jarElement);
			}
            
            for (Iterator iter = pluginLibraries.iterator(); iter.hasNext();) {
                String lib = (String) iter.next();

                Element jarElement = document.createElement("jar");
                jarElement.setAttribute("download", "eager");
                jarElement.setAttribute("href", "plugins/" + lib);
                
                resourcesElement.appendChild(jarElement);
            }

			out.println(docSerializer.toXML(document));
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
