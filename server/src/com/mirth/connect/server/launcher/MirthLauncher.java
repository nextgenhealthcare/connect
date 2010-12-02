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
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import com.mirth.connect.server.util.ResourceUtil;

public class MirthLauncher {
    private static final String INSTALL_TEMP = "install_temp";
    private static final String UNINSTALL_FILE = "uninstall";

    public static void main(String[] args) {
        try {
            try {
                uninstallExtensions();
                installExtensions();
            } catch (Exception e) {
                e.printStackTrace();
            }

            InputStream is = null;

            if (args.length > 0) {
                is = new FileInputStream(args[0]);
            } else {
                is = ResourceUtil.getResourceStream(MirthLauncher.class, "mirth-launcher.xml");
            }

            ClasspathBuilder builder = new ClasspathBuilder(is);
            URLClassLoader classLoader = new URLClassLoader(builder.getClasspathURLs());
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
        String extensionsLocation = new File(ClasspathBuilder.EXTENSION_PATH).getPath();
        String uninstallFileLocation = extensionsLocation + File.separator + UNINSTALL_FILE;
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
        String extensionsLocation = new File(ClasspathBuilder.EXTENSION_PATH).getPath();
        String extensionsTempLocation = extensionsLocation + File.separator + INSTALL_TEMP + File.separator;
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
}
