package com.mirth.connect.server.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.mirth.connect.client.core.Version;

public class Migrate3_12_1Test {
	@Test
    public void testClientProtocols() throws Exception {
        testConfiguration("https.client.protocols", "TLSv1.3,TLSv1.2,TLSv1.1", "TLSv1.3,TLSv1.2");
    }

    @Test
    public void testServerProtocols() throws Exception {
        testConfiguration("https.server.protocols", "TLSv1.3,TLSv1.2,TLSv1.1,SSLv2Hello", "TLSv1.3,TLSv1.2,SSLv2Hello");
    }

    @Test
    public void testCipherSuites() throws Exception {
        testConfiguration("https.ciphersuites", Migrate3_12_1.OLD_DEFAULT_CIPHERSUITES,  Migrate3_12_1.NEW_DEFAULT_CIPHERSUITES);
    }

    private void testConfiguration(String key, String oldDefault, String newDefault) throws Exception {
        testConfiguration(key, oldDefault, newDefault, null);
        testConfiguration(key, oldDefault, newDefault, Version.v3_12_0);
        testConfigurationVersionLatest(key, oldDefault, newDefault);
    }

    private void testConfiguration(String key, String oldDefault, String newDefault, Version startingVersion) throws Exception {
    	Migrate3_12_1 migrator = new Migrate3_12_1();
        migrator.setStartingVersion(startingVersion);
        migrator.setLogger(spy(migrator.getLogger()));

        // Already set to new default
        reset(migrator.getLogger());
        PropertiesConfiguration configuration = spy(new PropertiesConfiguration());
        configuration.setProperty(key, newDefault);
        migrator.updateConfiguration(configuration);
        assertEquals(newDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(1)).error(any());

        // Set to old default
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        configuration.setProperty(key, oldDefault);
        migrator.updateConfiguration(configuration);
        assertEquals(newDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(1)).error(any());

        // Set to something custom
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        configuration.setProperty(key, "test");
        migrator.updateConfiguration(configuration);
        assertEquals(newDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertEquals("test", StringUtils.join(configuration.getStringArray(key + ".old"), ','));
        assertFalse(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(2)).error(any());

        // Set to empty
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        configuration.setProperty(key, "");
        migrator.updateConfiguration(configuration);
        assertEquals(newDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(1)).error(any());

        // Set to blank
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        configuration.setProperty(key, "   ");
        migrator.updateConfiguration(configuration);
        assertEquals(newDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(1)).error(any());

        // Not set at all
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        migrator.updateConfiguration(configuration);
        assertEquals(newDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(1)).error(any());
    }

    private void testConfigurationVersionLatest(String key, String oldDefault, String newDefault) throws Exception {
        Migrate3_12_1 migrator = new Migrate3_12_1();
        migrator.setStartingVersion(Version.getLatest());
        migrator.setLogger(spy(migrator.getLogger()));

        // Already set to new default
        reset(migrator.getLogger());
        PropertiesConfiguration configuration = spy(new PropertiesConfiguration());
        configuration.setProperty(key, newDefault);
        migrator.updateConfiguration(configuration);
        assertEquals(newDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(0)).error(any());

        // Set to old default
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        configuration.setProperty(key, oldDefault);
        migrator.updateConfiguration(configuration);
        assertEquals(oldDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(0)).error(any());

        // Set to something custom
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        configuration.setProperty(key, "test");
        migrator.updateConfiguration(configuration);
        assertEquals("test", StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(0)).error(any());

        // Set to empty
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        configuration.setProperty(key, "");
        migrator.updateConfiguration(configuration);
        assertEquals("", StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(0)).error(any());

        // Set to blank
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        configuration.setProperty(key, "   ");
        migrator.updateConfiguration(configuration);
        assertEquals("   ", StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(0)).error(any());

        // Not set at all
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        migrator.updateConfiguration(configuration);
        assertFalse(configuration.containsKey(key));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(0)).error(any());
    }
}
