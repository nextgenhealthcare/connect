/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.ssl.SSLContexts;
import org.apache.log4j.Logger;

public class MirthSSLUtil {

    public static final String KEY_SUPPORTED_PROTOCOLS = "supportedProtocols";
    public static final String KEY_SUPPORTED_CIPHER_SUITES = "supportedCipherSuites";
    public static final String KEY_ENABLED_CLIENT_PROTOCOLS = "enabledClientProtocols";
    public static final String KEY_ENABLED_SERVER_PROTOCOLS = "enabledServerProtocols";
    public static final String KEY_ENABLED_CIPHER_SUITES = "enabledCipherSuites";

    public static final String[] DEFAULT_HTTPS_CLIENT_PROTOCOLS = new String[] { "TLSv1.2",
            "TLSv1.1" };
    public static final String[] DEFAULT_HTTPS_SERVER_PROTOCOLS = new String[] { "TLSv1.2",
            "TLSv1.1", "SSLv2Hello" };
    public static final String[] DEFAULT_HTTPS_CIPHER_SUITES = new String[] {
            "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_RSA_WITH_AES_256_GCM_SHA384", "TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384", "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_DHE_DSS_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_DHE_DSS_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
            "TLS_RSA_WITH_AES_256_CBC_SHA256", "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384", "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256",
            "TLS_DHE_DSS_WITH_AES_256_CBC_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA",
            "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA", "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA",
            "TLS_DHE_RSA_WITH_AES_256_CBC_SHA", "TLS_DHE_DSS_WITH_AES_256_CBC_SHA",
            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_RSA_WITH_AES_128_CBC_SHA256", "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_DHE_DSS_WITH_AES_128_CBC_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA",
            "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA", "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
            "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
            "TLS_EMPTY_RENEGOTIATION_INFO_SCSV" };

    private static Logger logger = Logger.getLogger(MirthSSLUtil.class);

    public static String[] getSupportedHttpsProtocols() {
        return SSLContexts.createDefault().getSupportedSSLParameters().getProtocols();
    }

    public static String[] getSupportedHttpsCipherSuites() {
        return SSLContexts.createDefault().getSupportedSSLParameters().getCipherSuites();
    }

    public static String[] getEnabledHttpsProtocols(String[] requestedProtocols) {
        logger.debug("Requested SSL protocols: " + Arrays.toString(requestedProtocols));
        SSLContext sslContext = SSLContexts.createDefault();
        String[] supportedProtocols = sslContext.getSupportedSSLParameters().getProtocols();
        Set<String> enabledProtocols = new LinkedHashSet<String>();

        for (String protocol : requestedProtocols) {
            if (ArrayUtils.contains(supportedProtocols, protocol)) {
                enabledProtocols.add(protocol);
            }
        }

        logger.debug("Enabled SSL protocols: " + String.valueOf(enabledProtocols));
        return enabledProtocols.toArray(new String[enabledProtocols.size()]);
    }

    public static String[] getEnabledHttpsCipherSuites(String[] requestedCipherSuites) {
        logger.debug("Requested SSL cipher suites: " + Arrays.toString(requestedCipherSuites));
        SSLContext sslContext = SSLContexts.createDefault();
        String[] supportedCipherSuites = sslContext.getSupportedSSLParameters().getCipherSuites();
        Set<String> enabledCipherSuites = new LinkedHashSet<String>();

        for (String cipherSuite : requestedCipherSuites) {
            if (ArrayUtils.contains(supportedCipherSuites, cipherSuite)) {
                enabledCipherSuites.add(cipherSuite);
            }
        }

        logger.debug("Enabled SSL cipher suites: " + String.valueOf(enabledCipherSuites));
        return enabledCipherSuites.toArray(new String[enabledCipherSuites.size()]);
    }
}