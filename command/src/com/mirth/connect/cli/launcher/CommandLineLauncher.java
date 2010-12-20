/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.cli.launcher;

import java.lang.reflect.Constructor;
import java.net.URLClassLoader;

public class CommandLineLauncher {
    private static final String DEFAULT_LAUNCHER_FILE = "mirth-cli-launcher.xml";

    public static void main(String[] args) {
        try {
            ClasspathBuilder cpBuilder = new ClasspathBuilder(DEFAULT_LAUNCHER_FILE);
            URLClassLoader classLoader = new URLClassLoader(cpBuilder.getClasspath());
            Class<?> cliClass = classLoader.loadClass("com.mirth.connect.cli.CommandLineInterface");
            Constructor<?>[] constructors = cliClass.getDeclaredConstructors();

            for (int i = 0; i < constructors.length; i++) {
                Class<?> parameters[] = constructors[i].getParameterTypes();

                if (parameters.length == 1) {
                    constructors[i].newInstance(new Object[] { args });
                    i = constructors.length;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
