/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelSummary;

public class MirthServletTest extends ServletTestBase {

    @BeforeClass
    public static void setup() throws Exception {
        ServletTestBase.setup();
    }

    @Test
    public void redactChannels() throws Exception {
        MirthServlet servlet = new MirthServlet(request, sc, controllerFactory) {
        };
        List<Channel> channels = new ArrayList<Channel>();
        channels.add(CHANNEL1);
        channels.add(CHANNEL2);
        List<Channel> redactedChannels = servlet.redactChannels(channels);
        assertEquals(channels, redactedChannels);

        servlet.setOperation(null);
        List<Channel> channels2 = new ArrayList<Channel>();
        channels2.add(CHANNEL1);
        channels2.add(CHANNEL2);
        channels2.add(DISALLOWED_CHANNEL);
        redactedChannels = servlet.redactChannels(channels2);
        assertEquals(channels, redactedChannels);
    }

    @Test
    public void redactChannelIds() throws Exception {
        MirthServlet servlet = new MirthServlet(request, sc, controllerFactory) {
        };
        Set<String> channelIds = new HashSet<String>();
        channelIds.add(CHANNEL_ID1);
        channelIds.add(CHANNEL_ID2);
        Set<String> redactedChannelIds = servlet.redactChannelIds(channelIds);
        assertEquals(channelIds, redactedChannelIds);

        servlet.setOperation(null);
        Set<String> channelIds2 = new HashSet<String>();
        channelIds2.add(CHANNEL_ID1);
        channelIds2.add(CHANNEL_ID2);
        channelIds2.add(DISALLOWED_CHANNEL_ID);
        redactedChannelIds = servlet.redactChannelIds(channelIds2);
        assertEquals(channelIds, redactedChannelIds);
    }

    @Test
    public void redactChannelIds2() throws Exception {
        MirthServlet servlet = new MirthServlet(request, sc, controllerFactory) {
        };
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(CHANNEL_ID1, null);
        map.put(CHANNEL_ID2, null);
        Map<String, Object> redactedMap = servlet.redactChannelIds(map);
        assertEquals(map, redactedMap);

        servlet.setOperation(null);
        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put(CHANNEL_ID1, null);
        map2.put(CHANNEL_ID2, null);
        map2.put(DISALLOWED_CHANNEL_ID, null);
        redactedMap = servlet.redactChannelIds(map2);
        assertEquals(map, redactedMap);
    }

    @Test
    public void redactChannelSummaries() throws Exception {
        MirthServlet servlet = new MirthServlet(request, sc, controllerFactory) {
        };
        List<ChannelSummary> channelSummaries = new ArrayList<ChannelSummary>();
        channelSummaries.add(new ChannelSummary(CHANNEL_ID1));
        channelSummaries.add(new ChannelSummary(CHANNEL_ID2));
        List<ChannelSummary> redactedChannelSummaries = servlet.redactChannelSummaries(channelSummaries);
        assertEquals(channelSummaries.size(), redactedChannelSummaries.size());
        for (int i = 0; i < channelSummaries.size(); i++) {
            assertEquals(channelSummaries.get(i).getChannelId(), redactedChannelSummaries.get(i).getChannelId());
        }

        servlet.setOperation(null);
        List<ChannelSummary> channelSummaries2 = new ArrayList<ChannelSummary>();
        channelSummaries2.add(new ChannelSummary(CHANNEL_ID1));
        channelSummaries2.add(new ChannelSummary(CHANNEL_ID2));
        channelSummaries2.add(new ChannelSummary(DISALLOWED_CHANNEL_ID));
        redactedChannelSummaries = servlet.redactChannelSummaries(channelSummaries2);
        assertEquals(channelSummaries.size(), redactedChannelSummaries.size());
        for (int i = 0; i < channelSummaries.size(); i++) {
            assertEquals(channelSummaries.get(i).getChannelId(), redactedChannelSummaries.get(i).getChannelId());
        }
    }

    @Test
    public void isChannelRedacted() throws Exception {
        MirthServlet servlet = new MirthServlet(request, sc, controllerFactory) {
        };
        servlet.setOperation(null);

        assertFalse(servlet.isChannelRedacted(CHANNEL_ID1));
        assertFalse(servlet.isChannelRedacted(CHANNEL_ID2));

        assertTrue(servlet.isChannelRedacted(DISALLOWED_CHANNEL_ID));
    }
}
