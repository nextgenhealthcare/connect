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

        propertiesToAdd.put("database.enable-read-write-split", new MutablePair<Object, String>(true, "If true, various read-only statements are separated into their own connection pool.\nBy default the read-only pool will use the same connection information as the master pool,\nbut you can change this with the \"database-readonly\" options. For example, to point the\nread-only pool to a different JDBC URL:\ndatabase-readonly.url = jdbc:..."));

        propertiesToAdd.put("database-readonly.max-connections", new MutablePair<Object, String>("20", "Maximum number of connections allowed for the read-only connection pool"));

        propertiesToAdd.put("rhino.languageversion", new MutablePair<Object, String>("default", "The language version for the Rhino JavaScript engine."));

        propertiesToAdd.put("database.connection.maxretry", new MutablePair<Object, String>("2", "Maximum number of retries to establish database connections in case of failure"));

        propertiesToAdd.put("database.connection.retrywaitinmilliseconds", new MutablePair<Object, String>("1000", "Maximum wait time in millseconds for retry to establish database connections in case of failure"));

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
