/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.Test;

import com.mirth.connect.client.core.Version;

public class Migrate3_7_0Test {

    @Test
    public void testClientProtocols() throws Exception {
        testConfiguration("https.client.protocols", "TLSv1.2,TLSv1.1", "TLSv1.3,TLSv1.2,TLSv1.1");
    }

    @Test
    public void testServerProtocols() throws Exception {
        testConfiguration("https.server.protocols", "TLSv1.2,TLSv1.1,SSLv2Hello", "TLSv1.3,TLSv1.2,TLSv1.1,SSLv2Hello");
    }

    @Test
    public void testCipherSuites() throws Exception {
        testConfiguration("https.ciphersuites", "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384,TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,TLS_DHE_DSS_WITH_AES_256_GCM_SHA384,TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256,TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,TLS_DHE_DSS_WITH_AES_128_GCM_SHA256,TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_RSA_WITH_AES_256_CBC_SHA256,TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384,TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384,TLS_DHE_RSA_WITH_AES_256_CBC_SHA256,TLS_DHE_DSS_WITH_AES_256_CBC_SHA256,TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,TLS_RSA_WITH_AES_256_CBC_SHA,TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA,TLS_ECDH_RSA_WITH_AES_256_CBC_SHA,TLS_DHE_RSA_WITH_AES_256_CBC_SHA,TLS_DHE_DSS_WITH_AES_256_CBC_SHA,TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256,TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_DSS_WITH_AES_128_CBC_SHA256,TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA,TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA,TLS_ECDH_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,TLS_EMPTY_RENEGOTIATION_INFO_SCSV", "TLS_CHACHA20_POLY1305_SHA256,TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256,TLS_AES_256_GCM_SHA384,TLS_AES_128_GCM_SHA256,TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384,TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,TLS_DHE_DSS_WITH_AES_256_GCM_SHA384,TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256,TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,TLS_DHE_DSS_WITH_AES_128_GCM_SHA256,TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_RSA_WITH_AES_256_CBC_SHA256,TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384,TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384,TLS_DHE_RSA_WITH_AES_256_CBC_SHA256,TLS_DHE_DSS_WITH_AES_256_CBC_SHA256,TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,TLS_RSA_WITH_AES_256_CBC_SHA,TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA,TLS_ECDH_RSA_WITH_AES_256_CBC_SHA,TLS_DHE_RSA_WITH_AES_256_CBC_SHA,TLS_DHE_DSS_WITH_AES_256_CBC_SHA,TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256,TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_DSS_WITH_AES_128_CBC_SHA256,TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA,TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA,TLS_ECDH_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,TLS_EMPTY_RENEGOTIATION_INFO_SCSV");
    }

    @Test
    public void testRhinoLanguageVersion() throws Exception {
        MutablePair<Object, String> pair = (MutablePair<Object, String>) new Migrate3_7_0().getConfigurationPropertiesToAdd().get("rhino.languageversion");
        assertEquals("default", pair.getKey());
    }

    private void testConfiguration(String key, String oldDefault, String newDefault) throws Exception {
        testConfiguration(key, oldDefault, newDefault, null);
        testConfiguration(key, oldDefault, newDefault, Version.V3_6_1);
        testConfigurationVersionLatest(key, oldDefault, newDefault);
    }

    private void testConfiguration(String key, String oldDefault, String newDefault, Version startingVersion) throws Exception {
        Migrate3_7_0 migrator = new Migrate3_7_0();
        migrator.setStartingVersion(startingVersion);
        migrator.setLogger(spy(migrator.getLogger()));

        // Already set to new default
        reset(migrator.getLogger());
        PropertiesConfiguration configuration = spy(new PropertiesConfiguration());
        doNothing().when(configuration).save();
        configuration.setProperty(key, newDefault);
        migrator.updateConfiguration(configuration);
        assertEquals(newDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(configuration, times(1)).save();
        verify(migrator.getLogger(), times(0)).error(any());

        // Set to old default
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        doNothing().when(configuration).save();
        configuration.setProperty(key, oldDefault);
        migrator.updateConfiguration(configuration);
        assertEquals(newDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(configuration, times(1)).save();
        verify(migrator.getLogger(), times(0)).error(any());

        // Set to something custom
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        doNothing().when(configuration).save();
        configuration.setProperty(key, "test");
        migrator.updateConfiguration(configuration);
        assertEquals(newDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertEquals("test", StringUtils.join(configuration.getStringArray(key + ".old"), ','));
        assertFalse(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(configuration, times(1)).save();
        verify(migrator.getLogger(), times(1)).error(any());

        // Set to empty
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        doNothing().when(configuration).save();
        configuration.setProperty(key, "");
        migrator.updateConfiguration(configuration);
        assertEquals(newDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(configuration, times(1)).save();
        verify(migrator.getLogger(), times(0)).error(any());

        // Set to blank
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        doNothing().when(configuration).save();
        configuration.setProperty(key, "   ");
        migrator.updateConfiguration(configuration);
        assertEquals(newDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(configuration, times(1)).save();
        verify(migrator.getLogger(), times(0)).error(any());

        // Not set at all
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        doNothing().when(configuration).save();
        migrator.updateConfiguration(configuration);
        assertEquals(newDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(configuration, times(1)).save();
        verify(migrator.getLogger(), times(0)).error(any());
    }

    private void testConfigurationVersionLatest(String key, String oldDefault, String newDefault) throws Exception {
        Migrate3_7_0 migrator = new Migrate3_7_0();
        migrator.setStartingVersion(Version.getLatest());
        migrator.setLogger(spy(migrator.getLogger()));

        // Already set to new default
        reset(migrator.getLogger());
        PropertiesConfiguration configuration = spy(new PropertiesConfiguration());
        doNothing().when(configuration).save();
        configuration.setProperty(key, newDefault);
        migrator.updateConfiguration(configuration);
        assertEquals(newDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(configuration, times(0)).save();
        verify(migrator.getLogger(), times(0)).error(any());

        // Set to old default
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        doNothing().when(configuration).save();
        configuration.setProperty(key, oldDefault);
        migrator.updateConfiguration(configuration);
        assertEquals(oldDefault, StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(configuration, times(0)).save();
        verify(migrator.getLogger(), times(0)).error(any());

        // Set to something custom
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        doNothing().when(configuration).save();
        configuration.setProperty(key, "test");
        migrator.updateConfiguration(configuration);
        assertEquals("test", StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(configuration, times(0)).save();
        verify(migrator.getLogger(), times(0)).error(any());

        // Set to empty
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        doNothing().when(configuration).save();
        configuration.setProperty(key, "");
        migrator.updateConfiguration(configuration);
        assertEquals("", StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(configuration, times(0)).save();
        verify(migrator.getLogger(), times(0)).error(any());

        // Set to blank
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        doNothing().when(configuration).save();
        configuration.setProperty(key, "   ");
        migrator.updateConfiguration(configuration);
        assertEquals("   ", StringUtils.join(configuration.getStringArray(key), ','));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(configuration, times(0)).save();
        verify(migrator.getLogger(), times(0)).error(any());

        // Not set at all
        reset(migrator.getLogger());
        configuration = spy(new PropertiesConfiguration());
        doNothing().when(configuration).save();
        migrator.updateConfiguration(configuration);
        assertFalse(configuration.containsKey(key));
        assertFalse(configuration.containsKey(key + ".old"));
        assertTrue(StringUtils.isEmpty(configuration.getLayout().getComment(key + ".old")));
        verify(configuration, times(0)).save();
        verify(migrator.getLogger(), times(0)).error(any());
    }
}
