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
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.ExtensionLibrary;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ExtensionController;

public class ExtensionServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            try {
                ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                PrintWriter out = response.getWriter();
                FileItem multiPartFileItem = null;
                String operation = null;
                Map<String, Object> parameterMap = new HashMap<String, Object>();
                
                if (ServletFileUpload.isMultipartContent(request)) {
                    Map<String, String> multipartParameters = new HashMap<String, String>();
                    File installTempDir = new File(ExtensionController.getExtensionsPath(), "install_temp");

                    if (!installTempDir.exists()) {
                        installTempDir.mkdir();
                    }

                    // we need to load properties from the multipart data
                    DiskFileItemFactory factory = new DiskFileItemFactory();
                    factory.setRepository(installTempDir);
                    ServletFileUpload upload = new ServletFileUpload(factory);
                    List<FileItem> items = upload.parseRequest(request);

                    for (FileItem item : items) {
                        if (item.isFormField()) {
                            multipartParameters.put(item.getFieldName(), item.getString());
                        } else {
                            // only supports a single file
                            multiPartFileItem = item;
                        }
                    }

                    operation = multipartParameters.get("op");
                } else {
                    operation = request.getParameter("op");
                }

                if (operation.equals(Operations.PLUGIN_PROPERTIES_GET)) {
                    String pluginName = request.getParameter("name");

                    if (isUserAuthorizedForExtension(request, pluginName, operation, null)) {
                        response.setContentType("application/xml");
                        out.println(serializer.toXML(extensionController.getPluginProperties(pluginName)));
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.PLUGIN_PROPERTIES_SET)) {
                    String pluginName = request.getParameter("name");

                    if (isUserAuthorizedForExtension(request, pluginName, operation, null)) {
                        Properties properties = (Properties) serializer.fromXML(request.getParameter("properties"));
                        extensionController.setPluginProperties(pluginName, properties);
                        extensionController.updatePluginProperties(pluginName, properties);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.PLUGIN_METADATA_GET)) {
                    out.println(serializer.toXML(extensionController.getPluginMetaData(), new Class[] { MetaData.class, PluginMetaData.class, ExtensionLibrary.class }));
                } else if (operation.equals(Operations.PLUGIN_METADATA_SET)) {
                    Map<String, PluginMetaData> metaData = (Map<String, PluginMetaData>) serializer.fromXML(request.getParameter("metaData"), new Class[] { MetaData.class, PluginMetaData.class, ExtensionLibrary.class });
                    parameterMap.put("metaData", metaData);

                    if (isUserAuthorized(request, parameterMap)) {
                        extensionController.savePluginMetaData(metaData);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONNECTOR_METADATA_GET)) {
                    response.setContentType("application/xml");
                    out.println(serializer.toXML(extensionController.getConnectorMetaData(), new Class[] { MetaData.class, ConnectorMetaData.class, ExtensionLibrary.class }));
                } else if (operation.equals(Operations.CONNECTOR_METADATA_SET)) {
                    Map<String, ConnectorMetaData> metaData = (Map<String, ConnectorMetaData>) serializer.fromXML(request.getParameter("metaData"), new Class[] { MetaData.class, ConnectorMetaData.class, ExtensionLibrary.class });
                    parameterMap.put("metaData", metaData);

                    if (isUserAuthorized(request, parameterMap)) {
                        extensionController.saveConnectorMetaData(metaData);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.EXTENSION_IS_ENABLED)) {
                    String extensionName = request.getParameter("name");
                    out.println(extensionController.isExtensionEnabled(extensionName));
                } else if (operation.equals(Operations.PLUGIN_SERVICE_INVOKE)) {
                    String pluginName = request.getParameter("name");
                    String method = request.getParameter("method");
                    Object object = serializer.fromXML(request.getParameter("object"));
                    String sessionId = request.getSession().getId();

                    if (isUserAuthorizedForExtension(request, pluginName, method, null)) {
                        out.println(serializer.toXML(extensionController.invokePluginService(pluginName, method, object, sessionId)));
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONNECTOR_SERVICE_INVOKE)) {
                    String name = request.getParameter("name");
                    String method = request.getParameter("method");
                    Object object = serializer.fromXML(request.getParameter("object"));
                    String sessionId = request.getSession().getId();
                    out.println(serializer.toXML(extensionController.invokeConnectorService(name, method, object, sessionId)));
                } else if (operation.equals(Operations.EXTENSION_UNINSTALL)) {
                    String packageName = request.getParameter("packageName");
                    parameterMap.put("packageName", packageName);

                    if (isUserAuthorized(request, parameterMap)) {
                        extensionController.prepareExtensionForUninstallation(packageName);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.EXTENSION_INSTALL)) {
                    if (isUserAuthorized(request, null)) {
                        /*
                         * This is a multi-part method, so we need our
                         * parameters from the new map
                         */
                        extensionController.extractExtension(multiPartFileItem);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            } catch (Throwable t) {
                logger.error(ExceptionUtils.getStackTrace(t));
                throw new ServletException(t);
            }
        }
    }
}