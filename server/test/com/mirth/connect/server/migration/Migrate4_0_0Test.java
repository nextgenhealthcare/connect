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

public class Migrate4_0_0Test {
	@Test
    public void testClientProtocols() throws Exception {
        testConfiguration("https.client.protocols", "TLSv1.3,TLSv1.2,TLSv1.1", "TLSv1.3,TLSv1.2", "TLSv1.2,TLSv1.1", "TLSv1.2");
    }

    @Test
    public void testServerProtocols() throws Exception {
        testConfiguration("https.server.protocols", "TLSv1.3,TLSv1.2,TLSv1.1,SSLv2Hello", "TLSv1.3,TLSv1.2,SSLv2Hello", "TLSv1.3,TLSv1.2,TLSv1.1", "TLSv1.3,TLSv1.2");
    }

    @Test
    public void testCipherSuites() throws Exception {
        testConfiguration("https.ciphersuites", Migrate4_0_0.OLD_DEFAULT_CIPHERSUITES,  Migrate4_0_0.NEW_DEFAULT_CIPHERSUITES, "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_RSA_WITH_AES_256_CBC_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384");
    }

    private void testConfiguration(String key, String oldDefault, String newDefault, String customValue, String newCustomValue) throws Exception {
        testConfiguration(key, oldDefault, newDefault, null, customValue, newCustomValue);
        testConfiguration(key, oldDefault, newDefault, Version.v3_12_0, customValue, newCustomValue);
        testConfigurationVersionLatest(key, oldDefault, newDefault, customValue, newCustomValue);
    }
    
    private void testConfiguration(String key, String oldDefault, String newDefault, Version startingVersion, String customValue, String newCustomValue) throws Exception {
    	Migrate4_0_0 migrator = new Migrate4_0_0();
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
        verify(migrator.getLogger(), times(1)).error(any(String.class));

        // Set to old default
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        configuration.setProperty(key, oldDefault);
        migrator.updateConfiguration(configuration);
        assertEquals(newDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(1)).error(any(String.class));

        // Set to something custom
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        configuration.setProperty(key, customValue);
        migrator.updateConfiguration(configuration);
        assertEquals(newCustomValue, StringUtils.join(configuration.getStringArray(key), ','));
        assertEquals(customValue, StringUtils.join(configuration.getStringArray(key + ".old"), ','));
        assertFalse(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(2)).error(any(String.class));


        // Set to empty
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        configuration.setProperty(key, "");
        migrator.updateConfiguration(configuration);
        assertEquals(newDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(1)).error(any(String.class));


        // Set to blank
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        configuration.setProperty(key, "   ");
        migrator.updateConfiguration(configuration);
        assertEquals(newDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(1)).error(any(String.class));


        // Not set at all
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        migrator.updateConfiguration(configuration);
        assertEquals(newDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(1)).error(any(String.class));

    }

    private void testConfigurationVersionLatest(String key, String oldDefault, String newDefault, String customValue, String newCustomValue) throws Exception {
    	Migrate4_0_0 migrator = new Migrate4_0_0();
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
        verify(migrator.getLogger(), times(0)).error(any(String.class));


        // Set to old default
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        configuration.setProperty(key, oldDefault);
        migrator.updateConfiguration(configuration);
        assertEquals(oldDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(0)).error(any(String.class));


        // Set to something custom
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        configuration.setProperty(key, customValue);
        migrator.updateConfiguration(configuration);
        assertEquals(customValue, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(0)).error(any(String.class));

        // Set to empty
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        configuration.setProperty(key, "");
        migrator.updateConfiguration(configuration);
        assertEquals("", StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(0)).error(any(String.class));


        // Set to blank
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        configuration.setProperty(key, "   ");
        migrator.updateConfiguration(configuration);
        assertEquals("   ", StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(0)).error(any(String.class));


        // Not set at all
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        migrator.updateConfiguration(configuration);
        assertFalse(configuration.containsKey(key));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(migrator.getLogger(), times(0)).error(any(String.class));

    }
}
