package com.mirth.connect.server.migration;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.Version;
import com.mirth.connect.model.util.MigrationException;

public class Migrate3_12_0 extends Migrator implements ConfigurationMigrator {
	
	public static final String REQUIRE_REQUESTED_WITH_PROPERTY = "server.api.require-requested-with";
	Logger logger = Logger.getLogger(getClass());
	
	@Override
    public void migrate() throws MigrationException {}

    @Override
    public void migrateSerializedData() throws MigrationException {}

    @Override
    public Map<String, Object> getConfigurationPropertiesToAdd() {
    	Map<String, Object> propertiesToAdd = new LinkedHashMap<String, Object>();
    	boolean defaultRequireRequestedWith = true;
    	
    	if (getStartingVersion() != null && getStartingVersion().ordinal() < Version.v3_12_0.ordinal()) {
    		defaultRequireRequestedWith = false;
    	}
        
        propertiesToAdd.put(REQUIRE_REQUESTED_WITH_PROPERTY, 
        		new MutablePair<Object, String>(defaultRequireRequestedWith, "If set to true, the Connect REST API will require all incoming requests to contain an \"X-Requested-With\" header.\nThis protects against Cross-Site Request Forgery (CSRF) security vulnerabilities."));
        
        return propertiesToAdd;
    }

    @Override
    public String[] getConfigurationPropertiesToRemove() {
        return null;
    }

    @Override
    public void updateConfiguration(PropertiesConfiguration configuration) {
        String keystoreType = configuration.getString("keystore.type");
        if (!StringUtils.equals("JCEKS", keystoreType)) {
            logger.error("Setting Keystore type from '" + keystoreType + "' to JCEKS");
        }
        configuration.setProperty("keystore.type", "JCEKS");
    }
}
