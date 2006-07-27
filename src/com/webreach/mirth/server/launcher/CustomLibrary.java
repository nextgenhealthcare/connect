package com.webreach.mirth.server.launcher;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class CustomLibrary {
	private Logger logger = Logger.getLogger(this.getClass());
	private String[] paths = null;
	
	public CustomLibrary(String[] paths) {
		this.paths = paths;
	}

	public URL[] getLibrary() throws Exception {
		FileFilter jarFileFilter = new FileFilter() {
			public boolean accept(File file) {
				return (!file.isDirectory() && file.getName().endsWith(".jar"));
			}
		};

		ArrayList<URL> urls = new ArrayList<URL>();
		
		for (int i = 0; i < paths.length; i++) {
			File path = new File(paths[i]);

			if (path.exists()) {
				if (path.isDirectory()) {
					File[] pathFiles = path.listFiles(jarFileFilter);

					for (int j = 0; j < pathFiles.length; j++) {
						logger.info("found jar in path: " + pathFiles[j].getAbsolutePath());
						urls.add(pathFiles[j].toURL());
					}
				} else {
					logger.info("found jar in path: " + path.getAbsolutePath());
					urls.add(path.toURL());
				}
			} else {
				throw new Exception("Could not locate custom library directory: " + path.getAbsolutePath());
			}
		}

		return urls.toArray(new URL[urls.size()]);
	}

}
