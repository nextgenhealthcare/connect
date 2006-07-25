package com.webreach.mirth.server;

import java.lang.reflect.Method;

import com.webreach.mirth.server.util.CustomLibrary;

public class Launcher {
	public static void main(String[] args) {
		int numArgs = 2;

		try {
			if (args.length != numArgs) {
				throw new Exception("Incorrect number of arguments provided.");
			}
			
			for (int i = 0; i < numArgs; i++) {
				if ((args[i] == null) || (args[i].equals(""))) {
					throw new Exception("Invalid argument: " + i);
				}
			}

			String jarPath = args[0];
			String classPath = args[1];

			CustomLibrary library = new CustomLibrary(jarPath, classPath);
			LocalClassLoader classLoader = new LocalClassLoader(library.getLibrary());
			Class mirthClass = classLoader.loadClass("com.webreach.mirth.server.Mirth");
			Object mirthObject = mirthClass.newInstance();
            Method startMethod = mirthClass.getMethod("start", (Class[]) null);
            startMethod.invoke(mirthObject, (Object[]) null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
