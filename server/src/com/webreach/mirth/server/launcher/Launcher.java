package com.webreach.mirth.server.launcher;

import java.net.URLClassLoader;

public class Launcher {
	public static void main(String[] args) {
		if (args[0] != null) {
			try {
				ClasspathBuilder builder = new ClasspathBuilder(args[0]);
				URLClassLoader classLoader = new URLClassLoader(builder.getClasspath());
				Class mirthClass = classLoader.loadClass("com.webreach.mirth.server.Mirth");
				Thread mirthThread = (Thread) mirthClass.newInstance();
				mirthThread.setContextClassLoader(classLoader);
				mirthThread.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("usage: java Launcher launcher.xml");
		}
	}
}
