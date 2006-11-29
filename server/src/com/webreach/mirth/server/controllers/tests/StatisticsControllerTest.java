package com.webreach.mirth.server.controllers.tests;

import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelStatistics;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.ChannelStatisticsController;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.tools.ScriptRunner;

public class StatisticsControllerTest extends TestCase {
	private ChannelStatisticsController statisticsController = new ChannelStatisticsController();
	private ChannelController channelController = new ChannelController();
	private ConfigurationController configurationController = new ConfigurationController();
	private Channel sampleChannel;

	protected void setUp() throws Exception {
		super.setUp();
		// clear all database tables
		ScriptRunner.runScript("database.sql");

		// initialize the configuration controller to cache encryption key
		configurationController.initialize();

		// create a sample channel
		sampleChannel = new Channel();
		sampleChannel.setId(configurationController.getGuid());
		sampleChannel.setName("Sample Channel");
		sampleChannel.setDescription("This is a sample channel");
		sampleChannel.setEnabled(true);
		sampleChannel.setVersion(configurationController.getVersion());
		sampleChannel.setRevision(0);
		sampleChannel.setDirection(Channel.Direction.INBOUND);
		sampleChannel.setProtocol(Channel.Protocol.HL7);
		sampleChannel.setMode(Channel.Mode.ROUTER);
		sampleChannel.setSourceConnector(new Connector());
		sampleChannel.setPreprocessingScript("return 1;");

		Properties sampleProperties = new Properties();
		sampleProperties.setProperty("testProperty", "true");
		sampleChannel.setProperties(sampleProperties);

		channelController.updateChannel(sampleChannel, true);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetStatistics() throws ControllerException {
		// Important: the received count is incremented twice to simulate the
		// errant behavior of Mule
		statisticsController.incReceivedCount(sampleChannel.getId());
		statisticsController.incReceivedCount(sampleChannel.getId());

		statisticsController.incSentCount(sampleChannel.getId());
		statisticsController.incErrorCount(sampleChannel.getId());

		ChannelStatistics testStatistics = statisticsController.getStatistics(sampleChannel.getId());

		Assert.assertEquals(sampleChannel.getId(), testStatistics.getChannelId());
		Assert.assertEquals(1, testStatistics.getReceivedCount());
		Assert.assertEquals(1, testStatistics.getSentCount());
		Assert.assertEquals(1, testStatistics.getErrorCount());
	}

}