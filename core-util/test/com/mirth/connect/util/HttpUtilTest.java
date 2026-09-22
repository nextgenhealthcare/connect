/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.security.KeyStore;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.ssl.SSLContexts;
import org.junit.Test;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.util.MessageMaps;

public class HttpUtilTest {

    @Test
    public void testExecuteGetRequest() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null, null);
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(trustStore, new TrustAllStrategy()).build();

        // Test GET request to real website
        assertFalse(StringUtils.isBlank(HttpUtil.doExecuteGetRequest("https://www.nextgen.com", 30000, true, MirthSSLUtil.DEFAULT_HTTPS_CLIENT_PROTOCOLS, MirthSSLUtil.DEFAULT_HTTPS_CIPHER_SUITES, sslContext)));

        // Test GET request to fake website
        try {
            HttpUtil.doExecuteGetRequest("https://www." + UUID.randomUUID().toString() + UUID.randomUUID().toString() + ".com", 30000, true, MirthSSLUtil.DEFAULT_HTTPS_CLIENT_PROTOCOLS, MirthSSLUtil.DEFAULT_HTTPS_CIPHER_SUITES, sslContext);
            fail("Exception should have been thrown");
        } catch (Exception e) {
        }
    }

    @Test
    public void testMapOrderPreserved() {
        final String MAP_KEY = "MyMap";

        Map<String, Object> initial = new LinkedHashMap<>();
        initial.put("First", 1);
        initial.put("Second", 2);
        initial.put("Third", 3);

        ConnectorMessage cm = new ConnectorMessage();
        cm.getSourceMap().put(MAP_KEY, initial);

        Map<?, ?> copied = HttpUtil.getTableMap(MAP_KEY, new MessageMaps(), cm);
        assertArrayEquals("Failed to preserve key order", initial.keySet().toArray(), copied.keySet().toArray());
    }
}
