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

import sun.misc.BASE64Decoder;

import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.controllers.ExtensionController;

public class ExtensionServlet extends MirthServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isUserLoggedIn(request)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				ExtensionController extensionController = ExtensionController.getInstance();
				ObjectXMLSerializer serializer = new ObjectXMLSerializer();
				PrintWriter out = response.getWriter();
				String operation = request.getParameter("op");

				if (operation.equals("getPluginProperties")) {
					response.setContentType("application/xml");
					String name = request.getParameter("name");
					out.println(serializer.toXML(extensionController.getPluginProperties(name)));
				} else if (operation.equals("setPluginProperties")) {
                    String name = request.getParameter("name");
                    Properties properties = (Properties) serializer.fromXML(request.getParameter("properties"));
                    extensionController.setPluginProperties(name, properties);
                    extensionController.updatePlugin(name, properties);
				} else if (operation.equals("getPluginMetaData")) {
                    out.println(serializer.toXML(extensionController.getPluginMetaData(),new Class[]{PluginMetaData.class}));
                } else if (operation.equals("setPluginMetaData")) {
                    Map<String, PluginMetaData> metaData = (Map<String, PluginMetaData>) serializer.fromXML(request.getParameter("metaData"),new Class[]{PluginMetaData.class});
                    extensionController.savePluginMetaData(metaData);
                } else if (operation.equals("getConnectorMetaData")) {
                    response.setContentType("application/xml");
                    out.println(serializer.toXML(extensionController.getConnectorMetaData(), new Class[]{ConnectorMetaData.class}));
                } else if (operation.equals("setConnectorMetaData")) {
                    Map<String, ConnectorMetaData> metaData = (Map<String, ConnectorMetaData>) serializer.fromXML(request.getParameter("metaData"),new Class[]{ConnectorMetaData.class});
                    extensionController.saveConnectorMetaData(metaData);
                } else if (operation.equals("invoke")) {
                    String name = request.getParameter("name");
                    String method = request.getParameter("method");
                    Object object =(Object) serializer.fromXML(request.getParameter("object"));
                    out.println(serializer.toXML(extensionController.invoke(name, method, object)));
                } else if (operation.equals("installExtension")) {
                    String location = request.getParameter("location");
                    String fileContents = request.getParameter("file");
                    BASE64Decoder decoder = new BASE64Decoder();
                    byte[] bytes = decoder.decodeBuffer(fileContents);
                    extensionController.installExtension(location, bytes);
                } 
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}
}