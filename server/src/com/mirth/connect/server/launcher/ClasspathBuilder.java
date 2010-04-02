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
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ClasspathBuilder {
	private Logger logger = Logger.getLogger(this.getClass());
	private String launcherFileName = null;
	public static final String EXTENSION_PATH = "./extensions";
	private static final String PLUGIN_FILENAME = "plugin.xml";
	private static final String SOURCE_FILENAME = "source.xml";
	private static final String DESTINATION_FILENAME = "destination.xml";
	
	public ClasspathBuilder(String launcherFileName) {
		this.launcherFileName = launcherFileName;
	}

	public URL[] getClasspath() throws Exception {
		FileFilter pathFileFilter = new FileFilter() {
			public boolean accept(File file) {
				return (!file.isDirectory());
			}
		};
		
		FileFilter directoryFilter = new FileFilter() {
			public boolean accept(File file) {
				return (file.isDirectory());
			}
		};
		
		FileFilter extensionFileFilter = new FileFilter() {
			public boolean accept(File file) {
				return (file.isFile() && (
						file.getName().equalsIgnoreCase(PLUGIN_FILENAME) ||
						file.getName().equalsIgnoreCase(SOURCE_FILENAME) ||
						file.getName().equalsIgnoreCase(DESTINATION_FILENAME)));
			}
		};

		ArrayList<URL> urls = new ArrayList<URL>();
		File launcherFile = new File(launcherFileName);
		
		if (launcherFile.exists()) {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(launcherFile);
			Element launcherElement = document.getDocumentElement();
			Element classpathElement = (Element) launcherElement.getElementsByTagName("classpath").item(0);
			
			for (int i = 0; i < classpathElement.getElementsByTagName("lib").getLength(); i++) {
				String base = classpathElement.getAttribute("base");
				Element pathElement = (Element) classpathElement.getElementsByTagName("lib").item(i);
				File path = new File(base + "/" + pathElement.getAttribute("path"));
				
				if (path.exists()) {
					if (path.isDirectory()) {
						File[] pathFiles = path.listFiles(pathFileFilter);

						for (int j = 0; j < pathFiles.length; j++) {
							logger.trace("adding file to path: " + pathFiles[j].getAbsolutePath());
							urls.add(pathFiles[j].toURI().toURL());
						}
					} else {
						logger.trace("adding file to path: " + path.getAbsolutePath());
						urls.add(path.toURI().toURL());
					}
				} else {
					logger.warn("Could not locate path: " + path.getAbsolutePath());
				}
			}
		} else {
			logger.error("Could not locate launcher file:" + launcherFile.getAbsolutePath());
		}
		
		// Add the extension server and shared libraries to the classpath
		File path = new File(EXTENSION_PATH);

		if (path.exists() && path.isDirectory()) {
			File[] directories = path.listFiles(directoryFilter);
			
			for (File directory : directories) {
				File[] extensionFiles = directory.listFiles(extensionFileFilter);
				
				for (File extensionFile : extensionFiles) {
					Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(extensionFile);
					Element rootElement = document.getDocumentElement();
					NodeList libraries = rootElement.getElementsByTagName("library");
					
					for (int i = 0; i < libraries.getLength(); i++) {
						Element element = (Element)libraries.item(i);
						String type = element.getElementsByTagName("type").item(0).getTextContent();
						if (type.equalsIgnoreCase("server") || type.equalsIgnoreCase("shared")) {
							File file = new File(directory.getAbsolutePath() + File.separator + element.getAttribute("path"));
							if (file.exists()) {
								urls.add(file.toURI().toURL());
							} else {
								logger.error("Could not locate file: " + file.getAbsolutePath());
							}
						}
					}
				}				
			}
		}

		return urls.toArray(new URL[urls.size()]);
	}

}
