package com.webreach.mirth.server.launcher;

import java.net.URLClassLoader;

public class Launcher {
	public static void main(String[] args) {
		try {
			CustomLibrary library = new CustomLibrary(args);
			URLClassLoader classLoader = new URLClassLoader(library.getLibrary());
			Class mirthClass = classLoader.loadClass("com.webreach.mirth.server.Mirth");
			Thread mirthThread = (Thread) mirthClass.newInstance();
			mirthThread.setContextClassLoader(classLoader);
			mirthThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
