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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.model.ExtensionLibrary;
import com.webreach.mirth.model.MetaData;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.ExtensionController;

public class ExtensionServlet extends MirthServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isUserLoggedIn(request)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
				ObjectXMLSerializer serializer = new ObjectXMLSerializer();
				PrintWriter out = response.getWriter();
				FileItem multiPartFile = null;
				String operation = "";
				Map<String, String> multipartParameters = new HashMap<String, String>();
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
				if (isMultipart) {
					// we need to load properties from the multipart data
					DiskFileItemFactory factory = new DiskFileItemFactory();
					ServletFileUpload upload = new ServletFileUpload(factory);
					List items = upload.parseRequest(request);
					Iterator iter = items.iterator();
					while (iter.hasNext()) {
						FileItem item = (FileItem) iter.next();
						if (item.isFormField()) {
							multipartParameters.put(item.getFieldName(), item.getString());
						} else {
							// only supports a single file
							multiPartFile = item;
						}
					}
					operation = multipartParameters.get("op");
				} else {
					operation = request.getParameter("op");
				}
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
					out.println(serializer.toXML(extensionController.getPluginMetaData(), new Class[] { MetaData.class, PluginMetaData.class, ExtensionLibrary.class }));
				} else if (operation.equals("setPluginMetaData")) {
					Map<String, PluginMetaData> metaData = (Map<String, PluginMetaData>) serializer.fromXML(request.getParameter("metaData"), new Class[] { MetaData.class, PluginMetaData.class, ExtensionLibrary.class });
					extensionController.savePluginMetaData(metaData);
				} else if (operation.equals("getConnectorMetaData")) {
					response.setContentType("application/xml");
					out.println(serializer.toXML(extensionController.getConnectorMetaData(), new Class[] { MetaData.class, ConnectorMetaData.class, ExtensionLibrary.class }));
				} else if (operation.equals("setConnectorMetaData")) {
					Map<String, ConnectorMetaData> metaData = (Map<String, ConnectorMetaData>) serializer.fromXML(request.getParameter("metaData"), new Class[] { MetaData.class, ConnectorMetaData.class, ExtensionLibrary.class });
					extensionController.saveConnectorMetaData(metaData);
				} else if (operation.equals("isExtensionEnabled")) {
					String extensionName = request.getParameter("name");
					out.println(extensionController.isExtensionEnabled(extensionName));
				} else if (operation.equals("invoke")) {
					String name = request.getParameter("name");
					String method = request.getParameter("method");
					Object object = (Object) serializer.fromXML(request.getParameter("object"));
					String sessionId = request.getSession().getId();
					out.println(serializer.toXML(extensionController.invoke(name, method, object, sessionId)));
				} else if (operation.equals("invokeConnectorService")) {
					String name = request.getParameter("name");
					String method = request.getParameter("method");
					Object object = (Object) serializer.fromXML(request.getParameter("object"));
					String sessionId = request.getSession().getId();
					out.println(serializer.toXML(extensionController.invokeConnectorService(name, method, object, sessionId)));
				} else if (operation.equals("uninstallExtension")) {
					String packageName = request.getParameter("packageName");
					extensionController.uninstallExtension(packageName);
				} else if (operation.equals("installExtension")) {
					// This is a multi-part method, so we need our parameters
					// from the new map
					extensionController.installExtension(multiPartFile);
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}
}