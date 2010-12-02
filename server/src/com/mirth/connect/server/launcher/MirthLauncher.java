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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MirthLauncher {
    private static final String PATH_INSTALL_TEMP = "install_temp";
    private static final String PATH_EXTENSIONS = "./extensions";
    
    private static Logger logger = Logger.getLogger(MirthLauncher.class);

    public static void main(String[] args) {
        try {
            try {
                uninstallExtensions();
                installExtensions();
            } catch (Exception e) {
                e.printStackTrace();
            }

            String[] manifest = new String[] { "mirth-server.jar", "lib", "custom-lib" };
            List<URL> classpathUrls = new ArrayList<URL>();
            addManifestToClasspath(manifest, classpathUrls);
            addExtensionsToClasspath(classpathUrls);
            URLClassLoader classLoader = new URLClassLoader(classpathUrls.toArray(new URL[classpathUrls.size()]));
            Class<?> mirthClass = classLoader.loadClass("com.mirth.connect.server.Mirth");
            Thread mirthThread = (Thread) mirthClass.newInstance();
            mirthThread.setContextClassLoader(classLoader);
            mirthThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // if we have an uninstall file, uninstall the listed extensions
    private static void uninstallExtensions() throws Exception {
        String extensionsLocation = new File(PATH_EXTENSIONS).getPath();
        String uninstallFileLocation = extensionsLocation + File.separator + "uninstall";
        File uninstallFile = new File(uninstallFileLocation);

        if (uninstallFile.exists()) {
            Scanner scanner = new Scanner(uninstallFile);
            
            while (scanner.hasNextLine()) {
                String extension = scanner.nextLine();
                File extensionDirectory = new File(extensionsLocation + File.separator + extension);
                
                if (extensionDirectory.isDirectory()) {
                    FileUtils.deleteDirectory(extensionDirectory);
                }
            }
            
            scanner.close();
            uninstallFile.delete();
        }
    }

    // if we have a temp folder, move the extensions over
    private static void installExtensions() throws Exception {
        String extensionsLocation = new File(PATH_EXTENSIONS).getPath();
        String extensionsTempLocation = extensionsLocation + File.separator + PATH_INSTALL_TEMP + File.separator;
        File extensionsTemp = new File(extensionsTempLocation);
        
        if (extensionsTemp.exists()) {
            File[] extensions = extensionsTemp.listFiles();

            for (int i = 0; i < extensions.length; i++) {
                if (extensions[i].isDirectory()) {
                    File target = new File(extensionsLocation + File.separator + extensions[i].getName());
                    FileUtils.deleteQuietly(target);
                    extensions[i].renameTo(target);
                }
            }

            FileUtils.deleteDirectory(extensionsTemp);
        }
    }
    
    private static void addManifestToClasspath(String[] manifest, List<URL> urls) throws Exception {
        IOFileFilter fileFileFilter = FileFilterUtils.fileFileFilter();

        for (String manifestPath : manifest) {
            File manifestFile = new File(manifestPath);

            if (manifestFile.exists()) {
                if (manifestFile.isDirectory()) {
                    Collection<File> pathFiles = FileUtils.listFiles(manifestFile, fileFileFilter, FileFilterUtils.trueFileFilter());

                    for (File pathFile : pathFiles) {
                        logger.trace("adding library to classpath: " + pathFile.getAbsolutePath());
                        urls.add(pathFile.toURI().toURL());
                    }
                } else {
                    logger.trace("adding library to classpath: " + manifestFile.getAbsolutePath());
                    urls.add(manifestFile.toURI().toURL());
                }
            } else {
                logger.warn("manifest path not found: " + manifestFile.getAbsolutePath());
            }
        }
    }

    private static void addExtensionsToClasspath(List<URL> urls) throws Exception {
        FileFilter extensionFileFilter = new NameFileFilter(new String[] { "plugin.xml", "source.xml", "destination.xml" }, IOCase.INSENSITIVE);
        FileFilter directoryFilter = FileFilterUtils.directoryFileFilter();
        File extensionPath = new File(PATH_EXTENSIONS);

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
