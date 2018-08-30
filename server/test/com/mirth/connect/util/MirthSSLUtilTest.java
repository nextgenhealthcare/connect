/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Test;

public class MirthSSLUtilTest {

    @Test
    public void testEnabledProtocols() throws Exception {
        String[] defaultClientProtocols = MirthSSLUtil.DEFAULT_HTTPS_CLIENT_PROTOCOLS;
        String[] defaultServerProtocols = MirthSSLUtil.DEFAULT_HTTPS_SERVER_PROTOCOLS;

        assertTrue(ArrayUtils.contains(defaultClientProtocols, "TLSv1.3"));
        assertTrue(ArrayUtils.contains(defaultServerProtocols, "TLSv1.3"));

        String[] enabledClientProtocols = MirthSSLUtil.getEnabledHttpsProtocols(defaultClientProtocols);
        String[] enabledServerProtocols = MirthSSLUtil.getEnabledHttpsProtocols(defaultServerProtocols);

        // TLSv1.3 supported in Java 11+
        if (getJavaVersion() >= 11) {
            assertTrue(ArrayUtils.contains(enabledClientProtocols, "TLSv1.3"));
            assertTrue(ArrayUtils.contains(enabledServerProtocols, "TLSv1.3"));
        } else {
            assertFalse(ArrayUtils.contains(enabledClientProtocols, "TLSv1.3"));
            assertFalse(ArrayUtils.contains(enabledServerProtocols, "TLSv1.3"));
        }
    }

    @Test
    public void testEnabledCipherSuites() throws Exception {
        String[] defaultClientCipherSuites = MirthSSLUtil.DEFAULT_HTTPS_CIPHER_SUITES;

        assertTrue(ArrayUtils.contains(defaultClientCipherSuites, "TLS_AES_256_GCM_SHA384"));

        String[] enabledClientCipherSuites = MirthSSLUtil.getEnabledHttpsCipherSuites(defaultClientCipherSuites);

        // TLS_AES_256_GCM_SHA384 supported in Java 11+
        if (getJavaVersion() >= 11) {
            assertTrue(ArrayUtils.contains(enabledClientCipherSuites, "TLS_AES_256_GCM_SHA384"));
        } else {
            assertFalse(ArrayUtils.contains(enabledClientCipherSuites, "TLS_AES_256_GCM_SHA384"));
        }
    }

    private int getJavaVersion() {
        String version = System.getProperty("java.version");

        int index = version.indexOf('-');
        if (index > 0) {
            version = version.substring(0, index);
        }

        index = version.indexOf('.');
        if (index > 0) {
            String tempVersion = version.substring(0, index);

            if (NumberUtils.toInt(tempVersion) == 1) {
                int index2 = version.indexOf('.', index + 1);
                if (index2 > 0) {
                    version = version.substring(index + 1, index2);
                } else {
                    version = tempVersion;
                }
            } else {
                version = tempVersion;
            }
        }

        return NumberUtils.toInt(version);
    }
}
