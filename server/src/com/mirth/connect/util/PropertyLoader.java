/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public abstract class PropertyLoader {
    private static final boolean THROW_ON_LOAD_FAILURE = true;
    private static final boolean LOAD_AS_RESOURCE_BUNDLE = false;
    private static final String SUFFIX = ".properties";

    /**
     * Looks up a resource named 'name' in the classpath. The resource must map
     * to a file with .properties extention. The name is assumed to be absolute
     * and can use either "/" or "." for package segment separation with an
     * optional leading "/" and optional ".properties" suffix. Thus, the
     * following names refer to the same resource:
     * 
     * <pre>
     *  some.pkg.Resource
     *  some.pkg.Resource.properties
     *  some/pkg/Resource
     *  some/pkg/Resource.properties
     *  /some/pkg/Resource
     *  /some/pkg/Resource.properties
     * </pre>
     * 
     * @param name
     *            classpath resource name [may not be null]
     * @param loader
     *            classloader through which to load the resource [null is
     *            equivalent to the application loader]
     * 
     * @return resource converted to java.util.Properties [may be null if the
     *         resource was not found and THROW_ON_LOAD_FAILURE is false]
     * @throws IllegalArgumentException
     *             if the resource was not found and THROW_ON_LOAD_FAILURE is
     *             true
     */
    public static Properties loadProperties(String name, ClassLoader loader) {
        if (name == null)
            throw new IllegalArgumentException("null input: name");

        if (name.startsWith("/"))
            name = name.substring(1);

        if (name.endsWith(SUFFIX))
            name = name.substring(0, name.length() - SUFFIX.length());

        Properties result = null;

        InputStream in = null;
        try {
            if (loader == null)
                loader = ClassLoader.getSystemClassLoader();

            if (LOAD_AS_RESOURCE_BUNDLE) {
                name = name.replace('/', '.');

                // Throws MissingResourceException on lookup failures:
                final ResourceBundle rb = ResourceBundle.getBundle(name, Locale.getDefault(), loader);

                result = new Properties();
                for (Enumeration keys = rb.getKeys(); keys.hasMoreElements();) {
                    final String key = (String) keys.nextElement();
                    final String value = rb.getString(key);

                    result.put(key, value);
                }
            } else {
                name = name.replace('.', '/');

                if (!name.endsWith(SUFFIX))
                    name = name.concat(SUFFIX);

                // Returns null on lookup failures:
                in = loader.getResourceAsStream(name);
                if (in != null) {
                    result = new Properties();
                    result.load(in); // Can throw IOException
                }
            }
        } catch (Exception e) {
            result = null;
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (Throwable ignore) {
                }
        }

        if (THROW_ON_LOAD_FAILURE && (result == null)) {
            throw new IllegalArgumentException("could not load [" + name + "]" + " as " + (LOAD_AS_RESOURCE_BUNDLE ? "a resource bundle" : "a classloader resource"));
        }

        return result;
    }

    /**
     * A convenience overload of {@link #loadProperties(String, ClassLoader)}
     * that uses the current thread's context classloader.
     */
    public static Properties loadProperties(final String name) {
        return loadProperties(name, Thread.currentThread().getContextClassLoader());
    }

    public static String getProperty(final Properties properties, final String propertyKey) {
        String property = properties.getProperty(propertyKey);
        
        if (property == null) {
            Logger.getLogger(PropertyLoader.class).error("Property \"" + propertyKey + "\" was not found.");
        }
        
        return property;
    }
    
    public static String getProperty(final Properties properties, final String propertyKey, final String defaultValue) {
        return properties.getProperty(propertyKey, defaultValue);
    }
}
