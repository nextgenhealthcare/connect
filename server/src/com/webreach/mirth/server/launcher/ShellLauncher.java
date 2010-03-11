/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.launcher;

import java.lang.reflect.Constructor;
import java.net.URLClassLoader;

public class ShellLauncher {
    public static void main(String[] args) {
        if (args[0] != null) {
            try {
                ClasspathBuilder builder = new ClasspathBuilder(args[0]);
                URLClassLoader classLoader = new URLClassLoader(builder.getClasspath());
                Class<?> shellClass = classLoader.loadClass("com.webreach.mirth.server.tools.Shell");

                // remove the first arg for the shell
                String[] newArgs = new String[args.length - 1];
                for (int i = 1; i < args.length; i++)
                    newArgs[i - 1] = args[i];

                Constructor<?>[] constructors = shellClass.getDeclaredConstructors();
                for (int i = 0; i < constructors.length; i++) {
                    Class<?> parameters[];
                    parameters = constructors[i].getParameterTypes();
                    // load plugin if the number of parameters is 1.
                    if (parameters.length == 1) {
                        constructors[i].newInstance(new Object[] { newArgs });
                        i = constructors.length;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("usage: java Launcher launcher.xml");
        }
    }
}
