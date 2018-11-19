/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.extprops;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;

public class ExtensionStatuses implements ExtensionStatusInterface {

    private static ExtensionStatuses instance = null;

    private LoggerWrapper logger;
    private Properties mirthProperties = new Properties();
    private ExtensionStatusProvider provider;

    public static ExtensionStatuses getInstance() {
        ExtensionStatuses provider = instance;

        if (provider == null) {
            synchronized (ExtensionStatuses.class) {
                provider = instance;
                if (provider == null) {
                    instance = provider = new ExtensionStatuses();
                }
            }
        }

        return provider;
    }

    private ExtensionStatuses() {
        try {
            logger = new LoggerWrapper(Thread.currentThread().getContextClassLoader().loadClass("org.apache.log4j.Logger").getMethod("getLogger", Class.class).invoke(null, ExtensionStatuses.class));
        } catch (Throwable t) {
            logger = new LoggerWrapper(null);
        }

        try {
            InputStream is = new FileInputStream(new File("./conf/mirth.properties"));
            try {
                mirthProperties.load(is);
            } finally {
                IOUtils.closeQuietly(is);
            }
        } catch (Exception e) {
            logger.error("Unable to read mirth.properties.", e);
        }

        String providerClass = mirthProperties.getProperty("extension.properties.provider");

        if (providerClass != null && !providerClass.isEmpty() && !"file".equalsIgnoreCase(providerClass)) {
            try {
                Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(providerClass);
                provider = (ExtensionStatusProvider) clazz.getConstructor(Properties.class).newInstance(mirthProperties);
            } catch (Throwable t) {
                logger.error("Unable to instantiate provider class: " + providerClass, t);
            }
        }

        if (provider == null) {
            provider = new ExtensionStatusFile(mirthProperties);
        }
    }

    @Override
    public void reload() {
        if (provider != null) {
            provider.reload();
        }
    }

    @Override
    public Set<String> keySet() {
        if (provider != null) {
            return provider.keySet();
        }
        return new HashSet<String>();
    }

    @Override
    public boolean containsKey(String pluginName) {
        if (provider != null) {
            return provider.containsKey(pluginName);
        }
        return false;
    }

    @Override
    public boolean isEnabled(String pluginName) {
        if (provider != null) {
            return provider.isEnabled(pluginName);
        }
        return true;
    }

    @Override
    public void setEnabled(String pluginName, boolean enabled) {
        if (provider != null) {
            provider.setEnabled(pluginName, enabled);
        }
    }

    @Override
    public void remove(String pluginName) {
        if (provider != null) {
            provider.remove(pluginName);
        }
    }

    @Override
    public void save() {
        if (provider != null) {
            provider.save();
        }
    }
}
