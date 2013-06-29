package com.mirth.connect.server.migration;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;

public class Migrate2_2_0 extends Migrator implements ConfigurationMigrator {
    @Override
    public void migrate() throws MigrationException {
        executeScript(getDatabaseType() + "-8-9.sql");
    }

    @Override
    public void migrateSerializedData() throws MigrationException {}

    @Override
    public Map<String, Object> getConfigurationPropertiesToAdd() {
        Map<String, Object> propertiesToAdd = new LinkedHashMap<String, Object>();
        propertiesToAdd.put("password.retrylimit", 0);
        propertiesToAdd.put("password.lockoutperiod", 0);
        propertiesToAdd.put("password.expiration", 0);
        propertiesToAdd.put("password.graceperiod", 0);
        propertiesToAdd.put("password.reuseperiod", 0);
        propertiesToAdd.put("password.reuselimit", 0);
        return propertiesToAdd;
    }

    @Override
    public String[] getConfigurationPropertiesToRemove() {
        return new String[] { "keystore.storetype", "keystore.algorithm", "keystore.alias",
                "truststore.storetype", "truststore.algorithm" };
    }

    @Override
    public void updateConfiguration(PropertiesConfiguration configuration) {}
}
