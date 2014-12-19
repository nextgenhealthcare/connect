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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.model.LibraryProperties;
import com.mirth.connect.plugins.LibraryPlugin;
import com.mirth.connect.plugins.ServicePlugin;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class DirectoryResourcePlugin implements ServicePlugin, LibraryPlugin {

    private Logger logger = Logger.getLogger(getClass());
    private ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();

    @Override
    public String getPluginPointName() {
        return DirectoryResourceProperties.PLUGIN_POINT;
    }

    @Override
    public void init(Properties properties) {}

    @Override
    public void update(Properties properties) {}

    @Override
    public Object invoke(String method, Object object, String sessionId) throws Exception {
        if (method.equals("getLibraries")) {
            DirectoryResourceProperties props = (DirectoryResourceProperties) object;
            List<URL> urls = contextFactoryController.getLibraries(props.getId());
            List<String> libraries = new ArrayList<String>();

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
            return libraries;
        }
        return null;
    }

    @Override
    public Properties getDefaultProperties() {
        return new Properties();
    }

    @Override
    public ExtensionPermission[] getExtensionPermissions() {
        return new ExtensionPermission[] {};
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public List<URL> getLibraries(LibraryProperties properties) throws Exception {
        DirectoryResourceProperties props = (DirectoryResourceProperties) properties;
        List<URL> libraries = new ArrayList<URL>();
        File directory = new File(props.getDirectory());

        if (directory.exists() && directory.isDirectory()) {
            for (File file : FileUtils.listFiles(directory, new NotFileFilter(new WildcardFileFilter(".*")), FileFilterUtils.trueFileFilter())) {
                if (file.isFile()) {
                    try {
                        libraries.add(file.toURI().toURL());
                    } catch (MalformedURLException e) {
                        logger.warn("Unable to load library: " + file.getName(), e);
                    }
                }
            }
        } else {
            logger.warn("Directory \"" + props.getDirectory() + "\" does not exist or is not a directory.");
        }

        return libraries;
    }

    @Override
    public void update(LibraryProperties properties) throws Exception {}

    @Override
    public void remove(LibraryProperties properties) throws Exception {}
}