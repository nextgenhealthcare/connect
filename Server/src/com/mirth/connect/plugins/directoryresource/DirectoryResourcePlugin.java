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
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;

import com.mirth.connect.model.LibraryProperties;
import com.mirth.connect.plugins.LibraryPlugin;

public class DirectoryResourcePlugin implements LibraryPlugin {

    private static final int MAX_FILES = 1000;

    private Logger logger = Logger.getLogger(getClass());

    @Override
    public String getPluginPointName() {
        return DirectoryResourceProperties.PLUGIN_POINT;
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public List<URL> getLibraries(LibraryProperties properties, boolean startup) throws Exception {
        DirectoryResourceProperties props = (DirectoryResourceProperties) properties;
        List<URL> libraries = new ArrayList<URL>();
        File directory = new File(props.getDirectory());

        if (directory.exists() && directory.isDirectory()) {
            for (File file : FileUtils.listFiles(directory, new NotFileFilter(new WildcardFileFilter(".*")), props.isDirectoryRecursion() ? FileFilterUtils.trueFileFilter() : null)) {
                if (file.isFile()) {
                    try {
                        libraries.add(file.toURI().toURL());
                        if (libraries.size() >= MAX_FILES) {
                            logger.error("Directory resource " + properties.getName() + " has reached the maximum amount of files allowed (" + MAX_FILES + "). Additional files will not be loaded.");
                            break;
                        }
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
    public void update(LibraryProperties properties, boolean startup) throws Exception {}

    @Override
    public void remove(LibraryProperties properties) throws Exception {}
}