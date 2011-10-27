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
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.Operation;
import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ExtensionController;

public class ExtensionServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // MIRTH-1745
        response.setCharacterEncoding("UTF-8");
        
        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            try {
                ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                PrintWriter out = response.getWriter();
                FileItem multiPartFileItem = null;
                Operation operation = null;
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

                    operation = Operations.getOperation(multipartParameters.get("op"));
                } else {
                    operation = Operations.getOperation(request.getParameter("op"));
                }

                if (operation.equals(Operations.PLUGIN_PROPERTIES_GET)) {
                    String pluginName = request.getParameter("name");

                    if (isUserAuthorizedForExtension(request, pluginName, operation.getName(), null)) {
                        response.setContentType(APPLICATION_XML);
                        serializer.toXML(extensionController.getPluginProperties(pluginName), out);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.PLUGIN_PROPERTIES_SET)) {
                    String pluginName = request.getParameter("name");

                    if (isUserAuthorizedForExtension(request, pluginName, operation.getName(), null)) {
                        Properties properties = (Properties) serializer.fromXML(request.getParameter("properties"));
                        extensionController.setPluginProperties(pluginName, properties);
                        extensionController.updatePluginProperties(pluginName, properties);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.PLUGIN_METADATA_GET)) {
                    response.setContentType(APPLICATION_XML);
                    serializer.toXML(extensionController.getPluginMetaData(), out);
                } else if (operation.equals(Operations.EXTENSION_SET_ENABLED)) {
                    String pluginName = request.getParameter("name");
                    boolean enabled = BooleanUtils.toBoolean(request.getParameter("enabled"));
                    parameterMap.put("extension", pluginName);
                    parameterMap.put("enabled", enabled);
                    
                    if (isUserAuthorized(request, parameterMap)) {
                        extensionController.setExtensionEnabled(pluginName, enabled);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONNECTOR_METADATA_GET)) {
                    response.setContentType(APPLICATION_XML);
                    serializer.toXML(extensionController.getConnectorMetaData(), out);
                } else if (operation.equals(Operations.EXTENSION_IS_ENABLED)) {
                    String extensionName = request.getParameter("name");
                    response.setContentType(TEXT_PLAIN);
                    out.print(extensionController.isExtensionEnabled(extensionName));
                } else if (operation.equals(Operations.PLUGIN_SERVICE_INVOKE)) {
                    String pluginName = request.getParameter("name");
                    String method = request.getParameter("method");
                    Object object = serializer.fromXML(request.getParameter("object"));
                    String sessionId = request.getSession().getId();

                    if (isUserAuthorizedForExtension(request, pluginName, method, null)) {
                        serializer.toXML(extensionController.invokePluginService(pluginName, method, object, sessionId), out);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.CONNECTOR_SERVICE_INVOKE)) {
                    String name = request.getParameter("name");
                    String method = request.getParameter("method");
                    Object object = serializer.fromXML(request.getParameter("object"));
                    String sessionId = request.getSession().getId();
                    response.setContentType(APPLICATION_XML);
                    serializer.toXML(extensionController.invokeConnectorService(name, method, object, sessionId), out);
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