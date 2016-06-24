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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.model.util.MigrationException;

public class Migrate3_4_0 extends Migrator implements ConfigurationMigrator {
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void migrate() throws MigrationException {
        executeScript(getDatabaseType() + "-3.3.2-3.4.0.sql");
    }

    @Override
    public void migrateSerializedData() throws MigrationException {}

    @Override
    public Map<String, Object> getConfigurationPropertiesToAdd() {
        return null;
    }

    @Override
    public String[] getConfigurationPropertiesToRemove() {
        return null;
    }

    @Override
    public void updateConfiguration(PropertiesConfiguration configuration) {
        if (StringUtils.containsIgnoreCase(configuration.getLayout().getComment("database"), "sqlserver2000")) {
            configuration.getLayout().setComment("database", "options: derby, mysql, postgres, oracle, sqlserver");

            try {
                configuration.save();
            } catch (ConfigurationException e) {
                logger.warn("An error occurred updating the database property comment.");
            }
        }
    }
}