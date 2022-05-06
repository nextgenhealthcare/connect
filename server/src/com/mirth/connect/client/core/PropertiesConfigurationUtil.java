/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.reloading.PeriodicReloadingTrigger;

public class PropertiesConfigurationUtil {

    public static FileBasedConfigurationBuilder<PropertiesConfiguration> createBuilder() {
        return new Configurations().propertiesBuilder(getDefaultParameters());
    }

    public static FileBasedConfigurationBuilder<PropertiesConfiguration> createBuilder(File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        return new Configurations().propertiesBuilder(getDefaultParameters().setFile(file));
    }

    public static PropertiesConfiguration create() {
        try {
            return createBuilder().getConfiguration();
        } catch (ConfigurationException e) {
            // Should not happen with default parameters
            return new PropertiesConfiguration();
        }
    }

    public static PropertiesConfiguration create(File file) throws ConfigurationException, IOException {
        return createBuilder(file).getConfiguration();
    }

    public static PropertiesConfiguration create(InputStream is) throws ConfigurationException {
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class);
        builder.configure(getDefaultParameters());
        PropertiesConfiguration config = builder.getConfiguration();
        FileHandler handler = new FileHandler(config);
        handler.load(is);
        return config;
    }

    public static ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> createReloadingBuilder(File file) {
        return createReloadingBuilder(file, false);
    }
    
    public static ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> createReloadingBuilder(File file, boolean commaDelimited) {
    	PropertiesBuilderParameters params = getDefaultParameters().setFile(file);
    	if (commaDelimited) {
    		params.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
    	}
        return new ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class).configure(params);
    }

    public static PeriodicReloadingTrigger createReloadTrigger(ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder) {
        return new PeriodicReloadingTrigger(builder.getReloadingController(), null, 1, TimeUnit.SECONDS);
    }

    public static void saveTo(PropertiesConfiguration config, File file) throws ConfigurationException {
        FileHandler handler = new FileHandler(config);
        handler.save(file);
    }

    public static void saveTo(PropertiesConfiguration config, OutputStream os) throws ConfigurationException {
        FileHandler handler = new FileHandler(config);
        handler.save(os);
    }

    private static PropertiesBuilderParameters getDefaultParameters() {
        return new Parameters().properties();
    }
}
