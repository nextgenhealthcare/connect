/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.servlets;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.client.core.api.servlets.ExtensionServletInterface;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.ServerEvent.Outcome;
import com.mirth.connect.server.api.DontCheckAuthorized;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ExtensionController;
import com.mirth.connect.server.controllers.ExtensionController.InstallationResult;

public class ExtensionServlet extends MirthServlet implements ExtensionServletInterface {

    private static final ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();

    public ExtensionServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc);
    }

    @Override
    @DontCheckAuthorized
    public void installExtension(InputStream inputStream) {
        /*
         * Check whether user is authorized without auditing yet. If unauthorized, fail fast so that
         * files aren't allowed to be extracted on the server.
         */
        if (!isUserAuthorized(false)) {
            isUserAuthorized(true);
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        // Attempt to extract the extension data
        InstallationResult result = extensionController.extractExtension(inputStream);

        // Now we have the metadata, so we can place it in the parameter map and audit the request proper
        parameterMap.put("metadata", result.getMetaData());
        auditAuthorizationRequest(result.getCause() == null ? Outcome.SUCCESS : Outcome.FAILURE);

        // Throw an exception if anything bad happened
        if (result.getCause() != null) {
            throw new MirthApiException(result.getCause());
        }
    }

    @Override
    public void uninstallExtension(String extensionPath) {
        try {
            extensionController.prepareExtensionForUninstallation(extensionPath);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public MetaData getExtensionMetaData(String extensionName) {
        MetaData metaData = extensionController.getPluginMetaData().get(extensionName);
        if (metaData == null) {
            metaData = extensionController.getConnectorMetaData().get(extensionName);
            if (metaData == null) {
                throw new MirthApiException(Status.NOT_FOUND);
            }
        }
        return metaData;
    }

    @Override
    public Map<String, ConnectorMetaData> getConnectorMetaData() {
        return extensionController.getConnectorMetaData();
    }

    @Override
    public Map<String, PluginMetaData> getPluginMetaData() {
        return extensionController.getPluginMetaData();
    }

    @Override
    public boolean isExtensionEnabled(String extensionName) {
        return extensionController.isExtensionEnabled(extensionName);
    }

    @Override
    public void setExtensionEnabled(String extensionName, boolean enabled) {
        try {
            extensionController.setExtensionEnabled(extensionName, enabled);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @DontCheckAuthorized
    public Properties getPluginProperties(String extensionName, Set<String> propertyKeys) {
        parameterMap.put("extensionName", extensionName);
        checkUserAuthorizedForExtension(extensionName);
        try {
            Properties extensionProperties = extensionController.getPluginProperties(extensionName);
            if (propertyKeys == null || propertyKeys.size() == 0) {
                return extensionProperties;
            }

            Properties filteredProperties = new Properties();
            for (String key: propertyKeys) {
                if (extensionProperties.containsKey(key)) {
                    filteredProperties.setProperty(key, extensionProperties.getProperty(key));
                }
            }
            return filteredProperties;
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @DontCheckAuthorized
    public void setPluginProperties(String extensionName, Properties properties, boolean mergeProperties) {
        parameterMap.put("extensionName", extensionName);
        parameterMap.put("properties", properties);
        checkUserAuthorizedForExtension(extensionName);
        try {
            extensionController.setPluginProperties(extensionName, properties, mergeProperties);
            extensionController.updatePluginProperties(extensionName, properties);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }
}