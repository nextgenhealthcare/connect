package com.mirth.connect.server.migration;

import java.util.Map;

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
}
