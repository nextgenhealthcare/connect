/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.ExtensionLibrary;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ExtensionController;

public class ExtensionServlet extends MirthServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else if (!isUserAuthorized(request)) {
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

                    String location = ExtensionController.getExtensionsPath() + "install_temp" + File.separator;
                    File locationFile = new File(location);
                    if (!locationFile.exists()) {
                        locationFile.mkdir();
                    }

                    factory.setRepository(locationFile);

                    ServletFileUpload upload = new ServletFileUpload(factory);
                    List<FileItem> items = upload.parseRequest(request);

                    for (FileItem item : items) {
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
                if (operation.equals(Operations.PLUGIN_PROPERTIES_GET)) {
                    response.setContentType("application/xml");
                    String name = request.getParameter("name");
                    out.println(serializer.toXML(extensionController.getPluginProperties(name)));
                } else if (operation.equals(Operations.PLUGIN_PROPERTIES_SET)) {
                    String name = request.getParameter("name");
                    Properties properties = (Properties) serializer.fromXML(request.getParameter("properties"));
                    extensionController.setPluginProperties(name, properties);
                    extensionController.updatePlugin(name, properties);
                } else if (operation.equals(Operations.PLUGIN_METADATA_GET)) {
                    out.println(serializer.toXML(extensionController.getPluginMetaData(), new Class[] { MetaData.class, PluginMetaData.class, ExtensionLibrary.class }));
                } else if (operation.equals(Operations.PLUGIN_METADATA_SET)) {
                    Map<String, PluginMetaData> metaData = (Map<String, PluginMetaData>) serializer.fromXML(request.getParameter("metaData"), new Class[] { MetaData.class, PluginMetaData.class, ExtensionLibrary.class });
                    extensionController.savePluginMetaData(metaData);
                } else if (operation.equals(Operations.CONNECTOR_METADATA_GET)) {
                    response.setContentType("application/xml");
                    out.println(serializer.toXML(extensionController.getConnectorMetaData(), new Class[] { MetaData.class, ConnectorMetaData.class, ExtensionLibrary.class }));
                } else if (operation.equals(Operations.CONNECTOR_METADATA_SET)) {
                    Map<String, ConnectorMetaData> metaData = (Map<String, ConnectorMetaData>) serializer.fromXML(request.getParameter("metaData"), new Class[] { MetaData.class, ConnectorMetaData.class, ExtensionLibrary.class });
                    extensionController.saveConnectorMetaData(metaData);
                } else if (operation.equals(Operations.EXTENSION_IS_ENABLED)) {
                    String extensionName = request.getParameter("name");
                    out.println(extensionController.isExtensionEnabled(extensionName));
                } else if (operation.equals(Operations.PLUGIN_SERVICE_INVOKE)) {
                    String name = request.getParameter("name");
                    String method = request.getParameter("method");
                    Object object = serializer.fromXML(request.getParameter("object"));
                    String sessionId = request.getSession().getId();
                    out.println(serializer.toXML(extensionController.invokePluginService(name, method, object, sessionId)));
                } else if (operation.equals(Operations.CONNECTOR_SERVICE_INVOKE)) {
                    String name = request.getParameter("name");
                    String method = request.getParameter("method");
                    Object object = serializer.fromXML(request.getParameter("object"));
                    String sessionId = request.getSession().getId();
                    out.println(serializer.toXML(extensionController.invokeConnectorService(name, method, object, sessionId)));
                } else if (operation.equals(Operations.EXTENSION_UNINSTALL)) {
                    String packageName = request.getParameter("packageName");
                    extensionController.uninstallExtension(packageName);
                } else if (operation.equals(Operations.EXTENSION_INSTALL)) {
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