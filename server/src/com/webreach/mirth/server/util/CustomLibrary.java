package com.webreach.mirth.server.util;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;

import org.apache.log4j.Logger;

public class CustomLibrary {
	private Logger logger = Logger.getLogger(this.getClass());
	private String jarPath = null;
	private String classPath = null;

	public CustomLibrary(String jarPath, String classPath) {
		this.jarPath = jarPath;
		this.classPath = classPath;
	}

	public String getClassPath() {
		return this.classPath;
	}

	public String getJarPath() {
		return this.jarPath;
	}

	public URL[] getLibrary() throws Exception {
		FileFilter jarFileFilter = new FileFilter() {
			public boolean accept(File file) {
				return (!file.isDirectory() && file.getName().endsWith(".jar"));
			}
		};

		File jarsDir = new File(jarPath);
		URL[] jarURLs = null;

		if (jarsDir.exists()) {
			File[] jarFiles = jarsDir.listFiles(jarFileFilter);

			// one extra URL is needed for the path to Mirth.class
			jarURLs = new URL[jarFiles.length + 1];

			for (int i = 0; i < jarFiles.length; i++) {
				logger.info("found jar: " + jarFiles[i].getAbsolutePath());
				jarURLs[i] = jarFiles[i].toURL();
			}

			// points to basedir for the Mirth.class
			jarURLs[jarURLs.length - 1] = (new File(classPath)).toURL();
		} else {
			throw new Exception("Could not locate custom library directory: " + jarsDir.getAbsolutePath());
		}

		return jarURLs;
	}

}
