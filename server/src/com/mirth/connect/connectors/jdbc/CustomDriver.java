/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;

public class CustomDriver implements Driver {

    private Driver delegate;

    public CustomDriver(ClassLoader classLoader, String className) throws Exception {
        /*
         * First, verify the class can be loaded with this classloader. If it can't, it will throw a
         * ClassNotFoundException and the caller should default to using DriverManager.
         */
        classLoader.loadClass(className);

        /*
         * In order to allow users to create database connections with a specific Driver from a
         * specific JAR, DriverManager cannot be used. This is because when creating a connection,
         * DriverManager iterates through its list of registered drivers, and actually attempts to
         * connect on each one, until one of them doesn't throw an exception. So in most cases, we
         * can't successfully use DriverManager to handle two different versions of the same driver,
         * because the first one in the list is going to be chosen every time.
         * 
         * Instead, we need to instantiate a Driver and use that instance directly to create
         * connections. However, that poses a problem because most drivers contain a static
         * initialization block wherein they register themselves directly with DriverManager. That
         * doesn't affect us using the driver instance directly, but it does mean that there is
         * potential for a memory leak if those drivers never get unregistered. Every time a
         * database connector is redeployed (or its context factory gets reset), a new driver would
         * get created and registered.
         * 
         * The naive solution would be to call DriverManager.deregisterDriver, but unfortunately
         * that cannot be done from a different classloader than the one used to load the driver
         * class in the first place. DriverManager actually checks when making any driver operation,
         * to ensure that the caller class' classloader is the same as the driver's. Since we need
         * to load the driver with a custom classloader (that's the whole point of this class), we
         * need to use a different class within the same loader to actually deregister the driver.
         * 
         * You might think we could just create a utility class, load it with the same classloader,
         * and invoke the appropriate methods. However, that won't work either. That's because the
         * classloader we need to use to load the driver is an isolated one, which only contains the
         * JARs necessary for the driver itself (which is how we support multiple versions of the
         * same driver). That classloader does not contain anything on the server classpath, which
         * means that even if we did create a class to handle driver registration, it would not be
         * accessible by the loader itself.
         * 
         * That's where Javassist comes in. We can create a class on the fly which can retrieve and
         * unregister drivers from DriverManager, using the same classloader used for the Driver
         * itself. This way, when calling getDrivers and deregisterDriver, DriverManager will allow
         * it to go through.
         */
        String ctClassName = getClass().getPackage().getName() + ".DriverManagerShim";
        Class<?> ctClass;

        try {
            // If we've created the shim class before in this classloader, use it
            ctClass = classLoader.loadClass(ctClassName);
        } catch (ClassNotFoundException e) {
            synchronized (classLoader) {
                try {
                    // Someone else may have already loaded it
                    ctClass = classLoader.loadClass(ctClassName);
                } catch (ClassNotFoundException e2) {
                    // Only create the driver manager shim in this classloader if we haven't already
                    ClassPool classPool = new ClassPool();
                    classPool.appendClassPath(new LoaderClassPath(classLoader));
                    classPool.appendClassPath("java.util");
                    classPool.appendClassPath("java.sql");
                    CtClass ctShimClassDefinition = classPool.makeClass(ctClassName);

                    // Add a method to call DriverManager.getDrivers()
                    CtMethod getDriversMethod = CtNewMethod.make(classPool.get(Enumeration.class.getName()), "getDrivers", new CtClass[0], new CtClass[0], "return java.sql.DriverManager.getDrivers();", ctShimClassDefinition);
                    ctShimClassDefinition.addMethod(getDriversMethod);

                    // Add a method to call DriverManager.deregisterDriver(driver)
                    CtMethod deregisterDriverMethod = CtNewMethod.make(CtClass.voidType, "deregisterDriver", new CtClass[] {
                            classPool.get(Driver.class.getName()) }, new CtClass[0], "java.sql.DriverManager.deregisterDriver($1);", ctShimClassDefinition);
                    ctShimClassDefinition.addMethod(deregisterDriverMethod);

                    ctClass = ctShimClassDefinition.toClass(classLoader, null);
                }
            }
        }

        // Instantiate our new class and get its methods
        Object driverManagerShim = ctClass.newInstance();
        Method getDriversMethod = ctClass.getMethod("getDrivers");
        Method deregisterDriverMethod = ctClass.getMethod("deregisterDriver", Driver.class);

        /*
         * The act of initializing the driver class causes it to be registered with DriverManager.
         * In order to ensure we unregister the correct driver, we synchronize here so that nothing
         * else can modify the list.
         */
        synchronized (DriverManager.class) {
            // Get the current list of drivers
            List<Driver> currentDrivers = Collections.list((Enumeration<Driver>) getDriversMethod.invoke(driverManagerShim));
            // Instantiate the driver
            delegate = (Driver) Class.forName(className, true, classLoader).newInstance();
            // Get the new list of drivers, which will usually include the one we just initialized
            List<Driver> newDrivers = Collections.list((Enumeration<Driver>) getDriversMethod.invoke(driverManagerShim));
            // If the new list is greater in size than the old, remove the last one registered
            while (newDrivers.size() > currentDrivers.size()) {
                deregisterDriverMethod.invoke(driverManagerShim, newDrivers.get(newDrivers.size() - 1));
                newDrivers = Collections.list((Enumeration<Driver>) getDriversMethod.invoke(driverManagerShim));
            }
        }
    }

    /**
     * Convenience method for creating connections. This does the same thing as DriverManager.
     */
    public Connection connect(String url, String username, String password) throws SQLException {
        Properties info = new Properties();
        if (username != null) {
            info.put("user", username);
        }
        if (password != null) {
            info.put("password", password);
        }

        return connect(url, info);
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        return delegate.connect(url, info);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return delegate.acceptsURL(url);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return delegate.getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() {
        return delegate.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return delegate.getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return delegate.jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}