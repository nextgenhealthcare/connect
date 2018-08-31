/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.Security;

import org.apache.commons.ssl.TrustMaterial;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eviware.soapui.SoapUI;
import com.mirth.connect.server.api.ServletTestBase;

public class WebServiceConnectorServletTest extends ServletTestBase {

    @BeforeClass
    public static void setup() throws Exception {
        ServletTestBase.setup();
    }

    @Test
    public void testCommonsSSL() throws Exception {
        // Emulate the default in Java9+
        Security.setProperty("keystore.type", "pkcs12");

        // This would have thrown an exception before
        TrustMaterial defaultTrustMaterial = TrustMaterial.DEFAULT;

        assertTrue(defaultTrustMaterial.getCertificates().size() > 0);
    }

    @Test
    public void testSoapUICore() throws Exception {
        assertNull(SoapUI.getSoapUICore());

        // Load static initialization
        new WebServiceConnectorServlet(request, sc);

        assertNotNull(SoapUI.getSoapUICore());
    }
}
