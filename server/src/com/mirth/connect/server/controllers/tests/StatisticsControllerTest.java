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

import junit.framework.TestCase;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.tools.ScriptRunner;

public class StatisticsControllerTest extends TestCase {
    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private Channel sampleChannel;

    protected void setUp() throws Exception {
        super.setUp();
        // clear all database tables
        ScriptRunner.runScript(new File("conf/" + ControllerTestSuite.database + "/" + ControllerTestSuite.database + "-database.sql"));

        // create a sample channel
        sampleChannel = new Channel();
        sampleChannel.setId(configurationController.generateGuid());
        sampleChannel.setName("Sample Channel");
        sampleChannel.setDescription("This is a sample channel");
        sampleChannel.setRevision(0);
        sampleChannel.setSourceConnector(new Connector());
        sampleChannel.setPreprocessingScript("return 1;");

        channelController.updateChannel(sampleChannel, null, true);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}