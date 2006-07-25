package com.webreach.mirth.server;

import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;

public class LocalClassLoader extends URLClassLoader {
	private Logger logger = Logger.getLogger(this.getClass());
	
	public LocalClassLoader(URL[] urls) {
		super(urls);
	}

	public Class<?> loadClass(String name) {
		Class c = null;

		try {
			c = findClass(name);
		} catch (Exception e) {
			try {
				c = super.loadClass(name);
			} catch (ClassNotFoundException cnfe) {
				logger.debug("could not locate resource: " + cnfe.getMessage());
			}
		}
		
		return c;
	}
}
