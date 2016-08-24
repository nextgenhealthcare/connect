/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.tools.ScriptRunner;

public class ChannelControllerTest extends TestCase {
    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private List<Channel> sampleChannelList;

    protected void setUp() throws Exception {
        super.setUp();
        // clear all database tables
        ScriptRunner.runScript(new File("conf/" + ControllerTestSuite.database + "/" + ControllerTestSuite.database + "-database.sql"));
        sampleChannelList = new ArrayList<Channel>();

        // TEST ADD
        for (int i = 0; i < 10; i++) {
            Channel sampleChannel = new Channel();
            sampleChannel.setId(configurationController.generateGuid());
            sampleChannel.setName("Channel" + i);
            sampleChannel.setDescription("This is a sample channel");
            sampleChannel.setRevision(0);

            sampleChannel.setPostprocessingScript("return 1;");
            sampleChannel.setDeployScript("return 1;");
            sampleChannel.setPreprocessingScript("return 1;");
            sampleChannel.setUndeployScript("return 1;");

            Connector sourceConnector = new Connector();
            sourceConnector.setMode(Connector.Mode.SOURCE);
            sourceConnector.setTransportName("File Reader");

            sampleChannel.setSourceConnector(sourceConnector);

            sampleChannelList.add(sampleChannel);
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testUpdateChannel() throws ControllerException {
        Channel sampleChannel = sampleChannelList.get(0);
        channelController.updateChannel(sampleChannel, null, true);

        Set<String> channelIds = new LinkedHashSet<String>();
        channelIds.add(sampleChannel.getId());

        List<Channel> testChannelList = channelController.getChannels(channelIds);
        Channel testChannel = testChannelList.get(0);

        Assert.assertEquals(1, testChannelList.size());
        Assert.assertEquals(sampleChannel, testChannel);
    }

    public void testGetChannel() throws ControllerException {
        insertSampleChannels();

        List<Channel> testChannelList = channelController.getChannels(null);

        for (Iterator<Channel> iter = sampleChannelList.iterator(); iter.hasNext();) {
            Channel sampleChannel = iter.next();
            Assert.assertTrue(testChannelList.contains(sampleChannel));
        }
    }

    public void testRemoveChannel() throws ControllerException {
        insertSampleChannels();

        Channel sampleChannel = sampleChannelList.get(0);
        channelController.removeChannel(sampleChannel, null);
        List<Channel> testChannelList = channelController.getChannels(null);

        Assert.assertFalse(testChannelList.contains(sampleChannel));
    }

    public void testRemoveAllChannels() throws ControllerException {
        insertSampleChannels();

        channelController.removeChannel(null, null);
        List<Channel> testChannelList = channelController.getChannels(null);

        Assert.assertTrue(testChannelList.isEmpty());
    }

    public void insertSampleChannels() throws ControllerException {
        for (Iterator<Channel> iter = sampleChannelList.iterator(); iter.hasNext();) {
            Channel sampleChannel = iter.next();
            channelController.updateChannel(sampleChannel, null, true);
        }
    }

}
