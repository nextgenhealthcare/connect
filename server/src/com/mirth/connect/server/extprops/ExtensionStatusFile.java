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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;

public class ExtensionStatusFile extends ExtensionStatusProvider {

    private File extensionPropertiesFile;
    private volatile Properties extensionProperties = new Properties();

    public ExtensionStatusFile(Properties mirthProperties) {
        super(mirthProperties);

        String appData = mirthProperties.getProperty("dir.appdata");
        if (appData == null || appData.isEmpty()) {
            appData = "appdata";
        }

        File appDataDir = new File(appData);

        if (appDataDir.exists()) {
            extensionPropertiesFile = new File(appDataDir, "extension.properties");

            if (!extensionPropertiesFile.exists()) {
                save();

                if (!extensionPropertiesFile.exists()) {
                    extensionPropertiesFile = null;
                    logger.error("Unable to create new extension.properties file.");
                }
            } else {
                reload();
            }
        } else {
            logger.error("Unable to find appdata directory: " + appData);
        }
    }

    @Override
    public void reload() {
        if (extensionPropertiesFile != null) {
            InputStream is = null;

            try {
                is = new FileInputStream(extensionPropertiesFile);
                Properties props = new Properties();
                props.load(is);
                extensionProperties = props;
            } catch (Exception e) {
                logger.error("Error reading extension.properties file.", e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
    }

    @Override
    public Set<String> keySet() {
        Set<String> set = new HashSet<String>();
        for (Object key : extensionProperties.keySet()) {
            set.add((String) key);
        }
        return set;
    }

    @Override
    public boolean containsKey(String pluginName) {
        return extensionProperties.containsKey(pluginName);
    }

    @Override
    public boolean isEnabled(String pluginName) {
        return Boolean.parseBoolean(extensionProperties.getProperty(pluginName, "true"));
    }

    @Override
    public void setEnabled(String pluginName, boolean enabled) {
        extensionProperties.setProperty(pluginName, Boolean.toString(enabled));
    }

    @Override
    public void remove(String pluginName) {
        extensionProperties.remove(pluginName);
    }

    @Override
    public void save() {
        if (extensionPropertiesFile != null) {
            Properties props = extensionProperties;
            FileOutputStream os = null;
            try {
                os = new FileOutputStream(extensionPropertiesFile, false);
                props.store(os, null);
            } catch (Exception e) {
                logger.error("Unable to save extension.properties file.", e);
            } finally {
                IOUtils.closeQuietly(os);
            }
        }
    }
}
