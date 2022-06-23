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

import org.apache.commons.configuration2.PropertiesConfiguration;

import com.mirth.connect.model.util.MigrationException;

public class Migrate4_1_0 extends Migrator implements ConfigurationMigrator {

    @Override
    public void migrate() throws MigrationException {
        executeScript(getDatabaseType() + "-9-4.1.0.sql");
    }

	@Override
	public Map<String, Object> getConfigurationPropertiesToAdd() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getConfigurationPropertiesToRemove() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateConfiguration(PropertiesConfiguration configuration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void migrateSerializedData() throws MigrationException {
		// TODO Auto-generated method stub
		
	}

}
