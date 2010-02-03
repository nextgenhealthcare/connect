/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.launcher;

import java.io.File;
import java.net.URLClassLoader;
import java.util.Scanner;

public class MirthLauncher {
	private static final String INSTALL_TEMP = "install_temp";
	private static final String UNINSTALL_FILE = "uninstall";
	private static final String DEFAULT_LAUNCHER_FILE = "mirth-launcher.xml";

	public static void main(String[] args) {
	    String launcherFile = DEFAULT_LAUNCHER_FILE;
	    if (args.length > 0) {
	        launcherFile = args[0];
	    }
	    
		try {
			try {
				uninstallExtensions();
				installExtensions();
			} catch (Exception e) {
				e.printStackTrace();
			}
			ClasspathBuilder builder = new ClasspathBuilder(launcherFile);
			URLClassLoader classLoader = new URLClassLoader(builder.getClasspath());
			Class mirthClass = classLoader.loadClass("com.webreach.mirth.server.Mirth");
			Thread mirthThread = (Thread) mirthClass.newInstance();
			mirthThread.setContextClassLoader(classLoader);
			mirthThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void uninstallExtensions() throws Exception{
		String extensionsLocation = new File(ClasspathBuilder.EXTENSION_PATH).getPath();
		String uninstallFileLocation = extensionsLocation + System.getProperty("file.separator") + UNINSTALL_FILE;

		File uninstallFile = new File(uninstallFileLocation);
		// if we have an uninstall file, uninstall the listed extensions
		if (uninstallFile.exists()){
			Scanner scanner = new Scanner(uninstallFile);
			while(scanner.hasNextLine()) {
				String extension = scanner.nextLine();
				File extensionDirectory = new File(extensionsLocation + System.getProperty("file.separator") + extension);
				if (extensionDirectory.isDirectory()) {
					deleteDirectory(extensionDirectory);
				}
			}
			scanner.close();
			uninstallFile.delete();
		}		
	}

	private static void installExtensions() throws Exception {
		String extensionsLocation = new File(ClasspathBuilder.EXTENSION_PATH).getPath();
		String extensionsTempLocation = extensionsLocation + System.getProperty("file.separator") + INSTALL_TEMP + System.getProperty("file.separator");

		File extensionsTemp = new File(extensionsTempLocation);
		// if we have a temp folder, move the extensions over
		if (extensionsTemp.exists()) {
			File[] extensions = extensionsTemp.listFiles();
			for (int i = 0; i < extensions.length; i++) {
				if (extensions[i].isDirectory()) {
					File target = new File(extensionsLocation + System.getProperty("file.separator") + extensions[i].getName());
					if (target.exists()) {
						if (target.isDirectory()) {
							deleteDirectory(target);
						} else {
							target.delete();
						}
					}
					extensions[i].renameTo(target);
				}
			}
			deleteDirectory(extensionsTemp);
		}
	}

	private static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}
	
}
