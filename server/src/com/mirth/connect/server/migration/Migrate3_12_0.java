package com.mirth.connect.server.migration;

import java.util.Map;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.model.util.MigrationException;

public class Migrate3_12_0 extends Migrator implements ConfigurationMigrator {
    
    Logger logger = Logger.getLogger(getClass());

    @Override
    public void migrate() throws MigrationException {}

    @Override
    public void migrateSerializedData() throws MigrationException {}
    
    @Override
    public void updateConfiguration(PropertiesConfiguration configuration) {
        String keystoreType = configuration.getString("keystore.type");
        if (!StringUtils.equals("JCEKS", keystoreType)) {
            logger.error("Setting Keystore type from '" + keystoreType + "' to JCEKS");
        }
        configuration.setProperty("keystore.type", "JCEKS");
    }

    @Override
    public Map<String, Object> getConfigurationPropertiesToAdd() {
        return null;
    }

    @Override
    public String[] getConfigurationPropertiesToRemove() {
        return null;
    }

}
