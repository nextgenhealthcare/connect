package com.mirth.connect.server.migration;

import java.util.Map;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.core.Version;
import com.mirth.connect.model.util.MigrationException;

public class Migrate4_4_0 extends Migrator implements ConfigurationMigrator {

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
        if (getStartingVersion() == null || getStartingVersion().ordinal() < Version.v4_4_0.ordinal()) {
            String digestAlgorithm = configuration.getString("digest.algorithm");

            // If no explicit digest algorithm was set, then default to the old SHA256 on upgrade.
            if (StringUtils.isBlank(digestAlgorithm)) {
                configuration.setProperty("digest.algorithm", "SHA256");
            }
            
            // Always set these properties on upgrade since the defaults changed.
            configuration.setProperty("digest.iterations", "1000");
            configuration.setProperty("digest.usepbe", "0");
        }
    }

    @Override
    public void migrate() throws MigrationException {}

    @Override
    public void migrateSerializedData() throws MigrationException {}
}
