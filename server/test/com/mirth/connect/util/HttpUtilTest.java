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

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class HttpUtilTest {

    @Test
    public void testExecuteGetRequest() throws Exception {
        // Test GET request to real website
        assertFalse(StringUtils.isBlank(HttpUtil.executeGetRequest("https://www.amazon.com", 30000, true, MirthSSLUtil.DEFAULT_HTTPS_CLIENT_PROTOCOLS, MirthSSLUtil.DEFAULT_HTTPS_CIPHER_SUITES)));

        // Test GET request to fake website
        assertTrue(StringUtils.isBlank(HttpUtil.executeGetRequest("https://www." + UUID.randomUUID().toString() + UUID.randomUUID().toString() + ".com", 30000, true, MirthSSLUtil.DEFAULT_HTTPS_CLIENT_PROTOCOLS, MirthSSLUtil.DEFAULT_HTTPS_CIPHER_SUITES)));
    }
}
