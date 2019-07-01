/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.directoryresource;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.model.ResourceProperties;
import com.mirth.connect.model.ResourcePropertiesList;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class DirectoryResourceServlet extends MirthServlet implements DirectoryResourceServletInterface {

    private static final ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
    private static final ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private static final ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();

    public DirectoryResourceServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, PLUGIN_POINT);
    }

    @Override
    public List<String> getLibraries(String resourceId) {
        try {
            DirectoryResourceProperties props = null;
            ResourcePropertiesList resources = serializer.deserialize(configurationController.getResources(), ResourcePropertiesList.class);
            for (ResourceProperties resource : resources.getList()) {
                if (resource instanceof DirectoryResourceProperties && resource.getId().equals(resourceId)) {
                    props = (DirectoryResourceProperties) resource;
                    break;
                }
            }

            List<String> libraries = new ArrayList<String>();

            if (props != null) {
                List<URL> urls = contextFactoryController.getLibraries(props.getId());

                if (StringUtils.isNotBlank(props.getDirectory())) {
                    File directory = new File(props.getDirectory());
                    for (URL url : urls) {
                        libraries.add(StringUtils.removeStartIgnoreCase(url.toString(), directory.toURI().toURL().toString()));
                    }
                } else {
                    for (URL url : urls) {
                        libraries.add(url.toString());
                    }
                }

                Collections.sort(libraries);
            }

            return libraries;
        } catch (MirthApiException e) {
            throw e;
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }
}