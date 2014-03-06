/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.launcher;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;

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
    private static final String EXTENSIONS_DIR = "./extensions";
    private static final String MIRTH_PROPERTIES_FILE = "./conf/mirth.properties";
    private static final String EXTENSION_PROPERTIES = "extension.properties";
    private static final String PROPERTY_APP_DATA_DIR = "dir.appdata";

    private static String appDataDir = null;

    private static Logger logger = Logger.getLogger(MirthLauncher.class);

    public static void main(String[] args) {
        try {
            try {
                uninstallPendingExtensions();
                installPendingExtensions();
            } catch (Exception e) {
                logger.error("Error uninstalling or installing pending extensions.", e);
            }

            try {
                createAppdataDir();
            } catch (Exception e) {
                logger.error("Error creating the appdata directory.", e);
            }

            ManifestFile mirthServerJar = new ManifestFile("server-lib/mirth-server.jar");
            ManifestFile mirthClientCoreJar = new ManifestFile("server-lib/mirth-client-core.jar");
            ManifestDirectory serverLibDir = new ManifestDirectory("server-lib");
            serverLibDir.setExcludes(new String[] { "mirth-client-core.jar" });
            ManifestDirectory customLibDir = new ManifestDirectory("custom-lib");
            ManifestEntry[] manifest = new ManifestEntry[] { mirthServerJar, mirthClientCoreJar,
                    serverLibDir, customLibDir };

            // Get the current server version
            JarFile mirthClientCoreJarFile = new JarFile(mirthClientCoreJar.getName());
            Properties versionProperties = new Properties();
            versionProperties.load(mirthClientCoreJarFile.getInputStream(mirthClientCoreJarFile.getJarEntry("version.properties")));
            String currentVersion = versionProperties.getProperty("mirth.version");

            List<URL> classpathUrls = new ArrayList<URL>();
            addManifestToClasspath(manifest, classpathUrls);
            addExtensionsToClasspath(classpathUrls, currentVersion);
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
    private static void uninstallPendingExtensions() throws Exception {
        File extensionsDir = new File(EXTENSIONS_DIR);
        File uninstallFile = new File(extensionsDir, "uninstall");

        if (uninstallFile.exists()) {
            List<String> extensionPaths = FileUtils.readLines(uninstallFile);

            for (String extensionPath : extensionPaths) {
                File extensionFile = new File(extensionsDir, extensionPath);

                if (extensionFile.exists() && extensionFile.isDirectory()) {
                    logger.trace("uninstalling extension: " + extensionFile.getName());
                    FileUtils.deleteDirectory(extensionFile);
                }
            }

            // delete the uninstall file when we're done
            FileUtils.deleteQuietly(uninstallFile);
        }
    }

    /*
     * This picks up any folders in the installation temp dir and moves them over to the extensions
     * dir, in effect "installing" them.
     */
    private static void installPendingExtensions() throws Exception {
        File extensionsDir = new File(EXTENSIONS_DIR);
        File extensionsTempDir = new File(extensionsDir, "install_temp");

        if (extensionsTempDir.exists()) {
            File[] extensions = extensionsTempDir.listFiles();

            for (int i = 0; i < extensions.length; i++) {
                if (extensions[i].isDirectory()) {
                    logger.trace("installing extension: " + extensions[i].getName());
                    File target = new File(extensionsDir, extensions[i].getName());

                    // delete it if it's already there
                    if (target.exists()) {
                        FileUtils.deleteQuietly(target);
                    }

                    extensions[i].renameTo(target);
                }
            }

            FileUtils.deleteDirectory(extensionsTempDir);
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

    private static void addExtensionsToClasspath(List<URL> urls, String currentVersion) throws Exception {
        FileFilter extensionFileFilter = new NameFileFilter(new String[] { "plugin.xml",
                "source.xml", "destination.xml" }, IOCase.INSENSITIVE);
        FileFilter directoryFilter = FileFilterUtils.directoryFileFilter();
        File extensionPath = new File(EXTENSIONS_DIR);

        Properties extensionProperties = new Properties();
        File extensionPropertiesFile = new File(appDataDir, EXTENSION_PROPERTIES);

        /*
         * If the file does not exist yet, an empty Properties object will be used, returning the
         * default of true for all extensions.
         */
        if (extensionPropertiesFile.exists()) {
            extensionProperties.load(new FileInputStream(extensionPropertiesFile));
        }

        if (extensionPath.exists() && extensionPath.isDirectory()) {
            File[] directories = extensionPath.listFiles(directoryFilter);

            for (File directory : directories) {
                File[] extensionFiles = directory.listFiles(extensionFileFilter);

                for (File extensionFile : extensionFiles) {
                    try {
                        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(extensionFile);
                        Element rootElement = document.getDocumentElement();

                        boolean enabled = extensionProperties.getProperty(rootElement.getElementsByTagName("name").item(0).getTextContent(), "true").equalsIgnoreCase("true");
                        boolean compatible = isExtensionCompatible(rootElement.getElementsByTagName("mirthVersion").item(0).getTextContent(), currentVersion);

                        // Only add libraries from extensions that are not disabled and are compatible with the current version
                        if (enabled && compatible) {
                            NodeList libraries = rootElement.getElementsByTagName("library");

                            for (int i = 0; i < libraries.getLength(); i++) {
                                Element libraryElement = (Element) libraries.item(i);
                                String type = libraryElement.getAttribute("type");

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

    private static boolean isExtensionCompatible(String extensionVersion, String currentVersion) {
        if (extensionVersion != null) {
            String[] extensionMirthVersions = extensionVersion.split(",");

            // If there is no build version, just use the patch version
            if (currentVersion.split("\\.").length == 4) {
                currentVersion = currentVersion.substring(0, currentVersion.lastIndexOf('.'));
            }

            for (int i = 0; i < extensionMirthVersions.length; i++) {
                if (extensionMirthVersions[i].trim().equals(currentVersion)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void createAppdataDir() throws FileNotFoundException, IOException {
        Properties mirthProperties = new Properties();
        mirthProperties.load(new FileInputStream(new File(MIRTH_PROPERTIES_FILE)));

        File appDataDirFile = null;

        if (mirthProperties.getProperty(PROPERTY_APP_DATA_DIR) != null) {
            appDataDirFile = new File(mirthProperties.getProperty(PROPERTY_APP_DATA_DIR));

            if (!appDataDirFile.exists()) {
                if (appDataDirFile.mkdir()) {
                    logger.debug("created app data dir: " + appDataDirFile.getAbsolutePath());
                } else {
                    logger.error("error creating app data dir: " + appDataDirFile.getAbsolutePath());
                }
            }
        } else {
            appDataDirFile = new File(".");
        }

        appDataDir = appDataDirFile.getAbsolutePath();
        logger.debug("set app data dir: " + appDataDir);
    }
}
