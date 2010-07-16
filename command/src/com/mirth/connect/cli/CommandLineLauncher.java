/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.cli;

import java.lang.reflect.Constructor;
import java.net.URLClassLoader;
import java.util.Arrays;

public class CommandLineLauncher {
    public static void main(String[] args) {
        if (args[0] != null) {
            try {
                ClasspathBuilder cpBuilder = new ClasspathBuilder(args[0]);
                URLClassLoader classLoader = new URLClassLoader(cpBuilder.getClasspath());
                Class<?> cliClass = classLoader.loadClass("com.mirth.connect.cli.CommandLineInterface");
                Constructor<?>[] constructors = cliClass.getDeclaredConstructors();
                String[] cliArgs = Arrays.copyOfRange(args, 1, args.length);
                
                for (int i = 0; i < constructors.length; i++) {
                    Class<?> parameters[] = constructors[i].getParameterTypes();

                    // load plugin if the number of parameters is 1.
                    if (parameters.length == 1) {
                        constructors[i].newInstance(new Object[] { cliArgs });
                        i = constructors.length;
                    }
                }

            } catch (Exception e) {
                System.err.println(e);
            }
        } else {
            System.out.println("usage: java Launcher launcher.xml");
        }
    }
}
