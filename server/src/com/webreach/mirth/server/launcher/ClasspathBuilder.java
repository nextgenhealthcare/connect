package com.webreach.mirth.server.launcher;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ClasspathBuilder {
	private Logger logger = Logger.getLogger(this.getClass());
	private String launcherFileName = null;
	
	public ClasspathBuilder(String launcherFileName) {
		this.launcherFileName = launcherFileName;
	}

	public URL[] getClasspath() throws Exception {
		FileFilter pathFileFilter = new FileFilter() {
			public boolean accept(File file) {
				return (!file.isDirectory());
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
							logger.info("adding file to path: " + pathFiles[j].getAbsolutePath());
							urls.add(pathFiles[j].toURL());
						}
					} else {
						logger.info("adding file to path: " + path.getAbsolutePath());
						urls.add(path.toURL());
					}
				} else {
					logger.warn("Could not locate path: " + path.getAbsolutePath());
				}
			}
		} else {
			logger.error("Could not locate launcher file:" + launcherFile.getAbsolutePath());
		}

		return urls.toArray(new URL[urls.size()]);
	}

}
