package com.mirth.connect.server.migration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.Test;

import com.mirth.connect.client.core.Version;

public class Migrate3_12_0Test {

	@SuppressWarnings("unchecked")
	@Test
	public void testMigrateUpgrade() throws Exception {
		Migrate3_12_0 migrator = new Migrate3_12_0();
		migrator.setStartingVersion(Version.v3_11_0);
		
		Map<String, Object> configurationProperties = migrator.getConfigurationPropertiesToAdd();
		assertTrue(configurationProperties.containsKey(Migrate3_12_0.REQUIRE_REQUESTED_WITH_PROPERTY));
		
		MutablePair<Object, String> property = (MutablePair<Object, String>)configurationProperties.get(Migrate3_12_0.REQUIRE_REQUESTED_WITH_PROPERTY);
		assertFalse((Boolean)property.getLeft());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testMigrateNewInstall() throws Exception {
		Migrate3_12_0 migrator = new Migrate3_12_0();
		migrator.setStartingVersion(Version.v3_12_0);
		
		Map<String, Object> configurationProperties = migrator.getConfigurationPropertiesToAdd();
		assertTrue(configurationProperties.containsKey(Migrate3_12_0.REQUIRE_REQUESTED_WITH_PROPERTY));
		
		MutablePair<Object, String> property = (MutablePair<Object, String>)configurationProperties.get(Migrate3_12_0.REQUIRE_REQUESTED_WITH_PROPERTY);
		assertTrue((Boolean)property.getLeft());
	}
	
}
