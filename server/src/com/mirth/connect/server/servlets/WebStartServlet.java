/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.servlets;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mirth.connect.model.converters.DocumentSerializer;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.util.PropertyLoader;

public class WebStartServlet extends HttpServlet {
    private Logger logger = Logger.getLogger(this.getClass());
    
	/*
     * Override last modified time to always be modified so it updates changes to JNLP.
	 */
    @Override
	protected long getLastModified(HttpServletRequest arg0)	{
	    return System.currentTimeMillis();
	}
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			DocumentSerializer docSerializer = new DocumentSerializer();
			PrintWriter out = response.getWriter();
			
			response.setContentType("application/x-java-jnlp-file");
            response.setHeader("Pragma", "no-cache");

            // try to load the jnlp file from the jar
            InputStream is = getClass().getResourceAsStream("/mirth-client.jnlp");
            
            if (is == null) {
                // if it's not in the jar, it should be in the base dir
                is = new FileInputStream("mirth-client.jnlp");
            }
            
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            IOUtils.closeQuietly(is);
            
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
			
            List<String> clientLibraries = ControllerFactory.getFactory().createExtensionController().getClientLibraries();
            
			for (String lib : clientLibraries) {
				Element jarElement = document.createElement("jar");
				jarElement.setAttribute("download", "eager");
				jarElement.setAttribute("href", "extensions/" + lib);
				
				resourcesElement.appendChild(jarElement);
			}

			out.println(docSerializer.toXML(document));
		} catch (Throwable t) {
            logger.error(ExceptionUtils.getStackTrace(t));
            throw new ServletException(t);
        }
	}
}
