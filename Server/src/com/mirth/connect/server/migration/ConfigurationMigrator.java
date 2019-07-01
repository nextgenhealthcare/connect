/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.migration;

import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;

public interface ConfigurationMigrator {
    /**
     * Returns a map of property names and default values to add to the configuration. Returns null
     * if there is nothing to add.
     */
    public Map<String, Object> getConfigurationPropertiesToAdd();

    /**
     * Returns an array of property names to remove from the configuration. Returns null if there is
     * nothing to remove.
     */
    public String[] getConfigurationPropertiesToRemove();

    /**
     * Migrates existing configuration settings
     */
    public void updateConfiguration(PropertiesConfiguration configuration);
}
