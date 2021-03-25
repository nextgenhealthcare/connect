/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.migration;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.tuple.MutablePair;

import com.mirth.connect.model.util.MigrationException;

public class Migrate3_11_0 extends Migrator implements ConfigurationMigrator {

    @Override
    public Map<String, Object> getConfigurationPropertiesToAdd() {
        Map<String, Object> propertiesToAdd = new LinkedHashMap<String, Object>();

         propertiesToAdd.put("database.connection.maxretry", new MutablePair<Object, String>("2", "On startup, Maximum number of retries to establish database connections in case of failure"));

        propertiesToAdd.put("database.connection.retrywaitinmilliseconds", new MutablePair<Object, String>("10000", "On startup, Maximum wait time in millseconds for retry to establish database connections in case of failure"));

        return propertiesToAdd;
    }

    @Override
    public String[] getConfigurationPropertiesToRemove() {
        return null;
    }

    @Override
    public void updateConfiguration(PropertiesConfiguration configuration) {
    }

    @Override
    public void migrate() throws MigrationException {
    }

    @Override
    public void migrateSerializedData() throws MigrationException {
    }
}
