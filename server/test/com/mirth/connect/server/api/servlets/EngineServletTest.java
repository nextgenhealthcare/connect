/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.servlets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.client.core.api.servlets.EngineServletInterface;
import com.mirth.connect.server.api.ServletTestBase;
import com.mirth.connect.server.controllers.EngineController;

public class EngineServletTest extends ServletTestBase {

    @BeforeClass
    public static void setup() throws Exception {
        ServletTestBase.setup();

        EngineController engineController = mock(EngineController.class);
        when(controllerFactory.createEngineController()).thenReturn(engineController);
    }

    @Test
    public void redeployAllChannels() throws Throwable {
        assertForbiddenInvocation(new EngineServlet(request, sc, controllerFactory), EngineServletInterface.class.getMethod("redeployAllChannels", boolean.class), new Object[] {
                false });
    }
}
