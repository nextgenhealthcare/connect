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

            if (StringUtils.isNotBlank(digestAlgorithm)) {
                // Keep the current algorithm, and set these other properties since the defaults changed.
                configuration.setProperty("digest.iterations", "1000");
                configuration.setProperty("digest.usepbe", "0");
            } else {
                // Use the new algorithm, but set the old default as the fallback
                configuration.setProperty("digest.fallback.algorithm", "SHA256");
                configuration.getLayout().setBlancLinesBefore("digest.fallback.algorithm", 1);
                configuration.getLayout().setComment("digest.fallback.algorithm", "Allows old digest values to be verified");
                configuration.setProperty("digest.fallback.iterations", "1000");
                configuration.setProperty("digest.fallback.usepbe", "0");
            }
        }
    }

    @Override
    public void migrate() throws MigrationException {}

    @Override
    public void migrateSerializedData() throws MigrationException {}
}
