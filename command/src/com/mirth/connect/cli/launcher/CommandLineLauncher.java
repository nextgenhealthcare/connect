/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.cli.launcher;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;

public class CommandLineLauncher {
    private static Logger logger = Logger.getLogger(CommandLineLauncher.class);

    public static void main(String[] args) {
        try {
            String[] manifest = new String[] { "mirth-cli.jar", "cli-lib" };
            List<URL> classpathUrls = new ArrayList<URL>();
            addManifestToClasspath(manifest, classpathUrls);
            addSharedLibsToClasspath(classpathUrls);
            URLClassLoader classLoader = new URLClassLoader(classpathUrls.toArray(new URL[classpathUrls.size()]));
            Class<?> cliClass = classLoader.loadClass("com.mirth.connect.cli.CommandLineInterface");
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

    private static void addManifestToClasspath(String[] manifestEntries, List<URL> urls) throws Exception {
        IOFileFilter fileFileFilter = FileFilterUtils.fileFileFilter();

        for (String manifestEntry : manifestEntries) {
            File manifestEntryFile = new File(manifestEntry);

            if (manifestEntryFile.exists()) {
                if (manifestEntryFile.isDirectory()) {
                    Collection<File> pathFiles = FileUtils.listFiles(manifestEntryFile, fileFileFilter, FileFilterUtils.trueFileFilter());

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
        IOFileFilter sharedLibFileFilter = new WildcardFileFilter("*-shared.jar");
        File extensions = new File("./extensions");

        if (extensions.exists() && extensions.isDirectory()) {
            Collection<File> sharedLibs = FileUtils.listFiles(extensions, sharedLibFileFilter, FileFilterUtils.trueFileFilter());

            for (File sharedLib : sharedLibs) {
                logger.trace("adding library to classpath: " + sharedLib.getAbsolutePath());
                urls.add(sharedLib.toURI().toURL());
            }
        } else {
            logger.warn("no extensions found");
        }
    }
}
