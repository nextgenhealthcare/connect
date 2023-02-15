package com.mirth.connect.server.migration;

import org.apache.commons.configuration2.PropertiesConfiguration;

public interface SecurityConfigurationMigrator {

    /**
     * Updates any properties that need to be migrated before encryption settings are loaded.
     */
    public void updateSecurityConfiguration(PropertiesConfiguration configuration);
}
