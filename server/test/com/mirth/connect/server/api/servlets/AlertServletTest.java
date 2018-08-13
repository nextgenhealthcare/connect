/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.servlets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.client.core.api.servlets.AlertServletInterface;
import com.mirth.connect.model.ChannelHeader;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.alert.AlertInfo;
import com.mirth.connect.server.alert.action.ChannelProtocol;
import com.mirth.connect.server.api.ServletTestBase;

@SuppressWarnings("unchecked")
public class AlertServletTest extends ServletTestBase {

    @BeforeClass
    public static void setup() throws Exception {
        ServletTestBase.setup();
    }

    @Test
    public void getAlertInfo() throws Throwable {
        Map<String, ChannelHeader> cachedChannels = new HashMap<String, ChannelHeader>();
        cachedChannels.put(CHANNEL_ID1, new ChannelHeader(1, Calendar.getInstance(), false));
        cachedChannels.put(CHANNEL_ID2, new ChannelHeader(1, Calendar.getInstance(), false));
        cachedChannels.put(DISALLOWED_CHANNEL_ID, new ChannelHeader(1, Calendar.getInstance(), false));

        AlertInfo info = (AlertInfo) ih.invoke(new AlertServlet(request, sc, controllerFactory), AlertServletInterface.class.getMethod("getAlertInfo", String.class, Map.class), new Object[] {
                "test", cachedChannels });
        assertAlertInfo(info);

        info = (AlertInfo) ih.invoke(new AlertServlet(request, sc, controllerFactory), AlertServletInterface.class.getMethod("getAlertInfo", Map.class), new Object[] {
                cachedChannels });
        assertAlertInfo(info);
    }

    private void assertAlertInfo(AlertInfo info) {
        boolean foundChannel1 = false;
        boolean foundChannel2 = false;
        for (ChannelSummary channelSummary : info.getChangedChannels()) {
            assertNotSame(DISALLOWED_CHANNEL_ID, channelSummary.getChannelId());

            if (channelSummary.getChannelId().equals(CHANNEL_ID1)) {
                foundChannel1 = true;
            } else if (channelSummary.getChannelId().equals(CHANNEL_ID2)) {
                foundChannel2 = true;
            }
        }
        assertTrue(foundChannel1);
        assertTrue(foundChannel2);

        Map<String, String> channelOptions = info.getProtocolOptions().get(ChannelProtocol.NAME);
        assertTrue(channelOptions.containsKey(CHANNEL_ID1));
        assertTrue(channelOptions.containsKey(CHANNEL_ID2));
        assertFalse(channelOptions.containsKey(DISALLOWED_CHANNEL_ID));
    }

    @Test
    public void getAlertProtocolOptions() throws Throwable {
        Map<String, Map<String, String>> options = (Map<String, Map<String, String>>) ih.invoke(new AlertServlet(request, sc, controllerFactory), AlertServletInterface.class.getMethod("getAlertProtocolOptions"), new Object[] {});

        Map<String, String> channelOptions = options.get(ChannelProtocol.NAME);
        assertTrue(channelOptions.containsKey(CHANNEL_ID1));
        assertTrue(channelOptions.containsKey(CHANNEL_ID2));
        assertFalse(channelOptions.containsKey(DISALLOWED_CHANNEL_ID));
    }
}
