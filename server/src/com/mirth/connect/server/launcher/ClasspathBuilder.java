/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.launcher;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ClasspathBuilder {
    public static final String EXTENSION_PATH = "./extensions";
    private Logger logger = Logger.getLogger(this.getClass());
    private InputStream configuration = null;

    public ClasspathBuilder(InputStream configuration) {
        this.configuration = configuration;
    }

    /**
     * Returns a list of URLs of the JAR files and folders specified in the configuration XML.
     * 
     * @return
     * @throws Exception
     */
    public URL[] getClasspathURLs() throws Exception {
        List<URL> urls = new ArrayList<URL>();
        addLauncherClasspath(urls);
        addExtensionLibraries(urls);
        return urls.toArray(new URL[urls.size()]);
    }

    private void addLauncherClasspath(List<URL> urls) throws Exception {
        IOFileFilter fileFileFilter = FileFilterUtils.fileFileFilter();
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configuration);
        IOUtils.closeQuietly(configuration);
        Element launcherElement = document.getDocumentElement();
        Element classpathElement = (Element) launcherElement.getElementsByTagName("classpath").item(0);

        for (int i = 0; i < classpathElement.getElementsByTagName("lib").getLength(); i++) {
            String base = classpathElement.getAttribute("base");
            Element pathElement = (Element) classpathElement.getElementsByTagName("lib").item(i);
            File path = new File(base + File.separator + pathElement.getAttribute("path"));

            if (path.exists()) {
                if (path.isDirectory()) {
                    Collection<File> pathFiles = FileUtils.listFiles(path, fileFileFilter, FileFilterUtils.trueFileFilter());

                    for (File pathFile : pathFiles) {
                        logger.trace("adding library to classpath: " + pathFile.getAbsolutePath());
                        urls.add(pathFile.toURI().toURL());
                    }
                } else {
                    logger.trace("adding library to classpath: " + path.getAbsolutePath());
                    urls.add(path.toURI().toURL());
                }
            } else {
                logger.warn("could not locate library: " + path.getAbsolutePath());
            }
        }
    }

    private void addExtensionLibraries(List<URL> urls) throws Exception {
        FileFilter extensionFileFilter = new NameFileFilter(new String[] { "plugin.xml", "source.xml", "destination.xml" }, IOCase.INSENSITIVE);
        FileFilter directoryFilter = FileFilterUtils.directoryFileFilter();
        File extensionPath = new File(EXTENSION_PATH);

        if (extensionPath.exists() && extensionPath.isDirectory()) {
            File[] directories = extensionPath.listFiles(directoryFilter);

            for (File directory : directories) {
                File[] extensionFiles = directory.listFiles(extensionFileFilter);

                for (File extensionFile : extensionFiles) {
                    try {
                        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(extensionFile);
                        Element rootElement = document.getDocumentElement();
                        NodeList libraries = rootElement.getElementsByTagName("library");

                        for (int i = 0; i < libraries.getLength(); i++) {
                            Element libraryElement = (Element) libraries.item(i);
                            String type = libraryElement.getElementsByTagName("type").item(0).getTextContent();

                            if (type.equalsIgnoreCase("server") || type.equalsIgnoreCase("shared")) {
                                File pathFile = new File(directory, libraryElement.getAttribute("path"));

                                if (pathFile.exists()) {
                                    logger.trace("adding library to classpath: " + pathFile.getAbsolutePath());
                                    urls.add(pathFile.toURI().toURL());
                                } else {
                                    logger.error("could not locate library: " + pathFile.getAbsolutePath());
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("failed to parse extension metadata: " + extensionFile.getAbsolutePath(), e);
                    }
                }
            }
        } else {
            logger.warn("no extensions found");
        }
    }
}
