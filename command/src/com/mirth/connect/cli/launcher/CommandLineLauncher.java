/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.cli.launcher;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class CommandLineLauncher {
    private static Logger logger;

    public static void main(String[] args) {
        System.setProperty("log4j.configuration", "log4j-cli.properties");
        logger = Logger.getLogger(CommandLineLauncher.class);

        try {
            ManifestFile mirthCliJar = new ManifestFile("cli-lib/mirth-cli.jar");
            ManifestFile mirthClientCoreJar = new ManifestFile("cli-lib/mirth-client-core.jar");
            ManifestDirectory cliLibDir = new ManifestDirectory("cli-lib");
            cliLibDir.setExcludes(new String[] { "mirth-client-core.jar" });
            
            ManifestEntry[] manifest = new ManifestEntry[] { mirthCliJar, mirthClientCoreJar, cliLibDir };

            List<URL> classpathUrls = new ArrayList<URL>();
            addManifestToClasspath(manifest, classpathUrls);
            addSharedLibsToClasspath(classpathUrls);
            URLClassLoader classLoader = new URLClassLoader(classpathUrls.toArray(new URL[classpathUrls.size()]));
            Class<?> cliClass = classLoader.loadClass("com.mirth.connect.cli.CommandLineInterface");
            Thread.currentThread().setContextClassLoader(classLoader);
            Constructor<?>[] constructors = cliClass.getDeclaredConstructors();

            for (int i = 0; i < constructors.length; i++) {
                Class<?> parameters[] = constructors[i].getParameterTypes();

                if (parameters.length == 1) {
                    constructors[i].newInstance(new Object[] { args });
                    i = constructors.length;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addManifestToClasspath(ManifestEntry[] manifestEntries, List<URL> urls) throws Exception {
        for (ManifestEntry manifestEntry : manifestEntries) {
            File manifestEntryFile = new File(manifestEntry.getName());

            if (manifestEntryFile.exists()) {
                if (manifestEntryFile.isDirectory()) {
                    ManifestDirectory manifestDir = (ManifestDirectory) manifestEntry;
                    IOFileFilter fileFilter = null;
                    
                    if (manifestDir.getExcludes().length > 0) {
                        fileFilter = FileFilterUtils.and(FileFilterUtils.fileFileFilter(), FileFilterUtils.notFileFilter(new NameFileFilter(manifestDir.getExcludes())));
                    } else {
                        fileFilter = FileFilterUtils.fileFileFilter();
                    }

                    Collection<File> pathFiles = FileUtils.listFiles(manifestEntryFile, fileFilter, FileFilterUtils.trueFileFilter());

                    for (File pathFile : pathFiles) {
                        logger.trace("adding library to classpath: " + pathFile.getAbsolutePath());
                        urls.add(pathFile.toURI().toURL());
                    }
                } else {
                    logger.trace("adding library to classpath: " + manifestEntryFile.getAbsolutePath());
                    urls.add(manifestEntryFile.toURI().toURL());
                }
            } else {
                logger.warn("manifest path not found: " + manifestEntryFile.getAbsolutePath());
            }
        }
    }

    private static void addSharedLibsToClasspath(List<URL> urls) throws Exception {
        File extensions = new File("./extensions");
        List<String> extensionXml = new LinkedList<>();
        extensionXml.add("source.xml");
        extensionXml.add("destination.xml");
        extensionXml.add("plugin.xml");
        
        if (extensions.exists() && extensions.isDirectory()) {            

            IOFileFilter extensionXmlFileFilter = new NameFileFilter(extensionXml);
            // for each folder: look for plugin.xml, source.xml, destination.xml
            // find any library tags with attribute type="SHARED"
            Collection<File> xmlFiles = FileUtils.listFiles(extensions, extensionXmlFileFilter, FileFilterUtils.trueFileFilter());
            Map<String, Set<String>> pathToLibsMap = new HashMap<>();
            for (File f : xmlFiles) {
                Element root = parseXml(FileUtils.readFileToString(f)).getDocumentElement();
                String path = root.getAttribute("path");
                if (!pathToLibsMap.containsKey(path)) {
                    pathToLibsMap.put(path, new HashSet<String>());
                }
                // TODO: make sure the mirthVersion is compatible with our current version of Mirth Server
                List<String> newLibs = getSharedLibs(root);
                pathToLibsMap.get(path).addAll(newLibs);
            }
            
            for (Entry<String, Set<String>> entry : pathToLibsMap.entrySet()) {
                Set<String> libs = entry.getValue();
                File extensionFolder = new File (extensions, entry.getKey());
                for (String libStr : libs) {
                    File lib = new File(extensionFolder, libStr);
                    urls.add(lib.toURI().toURL());
                }
            }                        
        } else {
            logger.warn("no extensions found");
        }
    }
    
    private static List<String> getSharedLibs(Element root) throws IOException, ParserConfigurationException, SAXException {
        // Unfortunately, we can't use the ExtensionLibarary at this point since none of it has been loaded, so we'll have
        // to get parse the xml manually
        List<String> sharedLibs = new LinkedList<>();
        NodeList libs = root.getElementsByTagName("library");
        for (int i = 0; i < libs.getLength(); i++) {
            Element libElement = (Element) libs.item(i);
            String type = libElement.getAttribute("type");
            // get libraries that are ExtensionLibrary.Type.SHARED
            if ("SHARED".equalsIgnoreCase(type)) {
                sharedLibs.add(libElement.getAttribute("path"));
            }
        }
        
        return sharedLibs;
    }
    
    private static Document parseXml(String xml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }
}
