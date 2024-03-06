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

import javax.net.ssl.SSLContext;

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
        
        SSLContext tempContext = SSLContext.getDefault();

        // TLSv1.3 supported in newer Java updates
        if (ArrayUtils.contains(tempContext.getSupportedSSLParameters().getProtocols(), "TLSv1.3")) {
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
        
        SSLContext tempContext = SSLContext.getDefault();

        // TLS_AES_256_GCM_SHA384 supported in newer Java updates
        if (ArrayUtils.contains(tempContext.getSupportedSSLParameters().getCipherSuites(), "TLS_AES_256_GCM_SHA384")) {
            assertTrue(ArrayUtils.contains(enabledClientCipherSuites, "TLS_AES_256_GCM_SHA384"));
        } else {
            assertFalse(ArrayUtils.contains(enabledClientCipherSuites, "TLS_AES_256_GCM_SHA384"));
        }
    }
}
