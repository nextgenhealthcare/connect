/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.mirth.connect.model.Attachment;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelStatistics;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.filters.MessageObjectFilter;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ChannelStatisticsController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.DefaultChannelStatisticsController;
import com.mirth.connect.server.controllers.DefaultExtensionController;
import com.mirth.connect.server.controllers.MessageObjectController;
import com.mirth.connect.server.tools.ScriptRunner;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.server.util.UUIDGenerator;

public class MessageObjectControllerTest extends TestCase {
    private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private ChannelStatisticsController channelStatisticsController = ControllerFactory.getFactory().createChannelStatisticsController();
    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private List<MessageObject> sampleMessageObjectList;
    private String channelId;

    private boolean forceTempTableCreation = true;
    private String messageTempTableUID = "test";

    // private Calendar dateCreated

    protected void setUp() throws Exception {
        super.setUp();
        // clear all database tables
        // ScriptRunner.runScript(new File("conf/derby/derby-database.sql"));
        ScriptRunner.runScript(new File("conf/" + ControllerTestSuite.database + "/" + ControllerTestSuite.database + "-database.sql"));
        sampleMessageObjectList = new ArrayList<MessageObject>();
        channelId = UUID.randomUUID().toString();

        ChannelStatistics chanStats = new ChannelStatistics();
        chanStats.setChannelId(channelId);
        chanStats.setServerId(configurationController.getServerId());
        chanStats.setError(0);
        chanStats.setFiltered(0);
        chanStats.setQueued(0);
        chanStats.setReceived(0);
        chanStats.setSent(0);
        chanStats.setAlerted(0);

        DefaultChannelStatisticsController.create();
        DefaultChannelStatisticsController.getInstance().loadCache();
        DefaultChannelStatisticsController.getInstance().updateAllStatistics();

        // DefaultChannelStatisticsController.getInstance().createStatistics(channelId);
        // DefaultChannelStatisticsController.getInstance().clearStatistics(channelId,
        // true, true, true, true, true, true);
        // channelStatisticsController.start();
        // channelStatisticsController.createStatistics(channelId);
        // channelController.loadChannelCache();

        DefaultExtensionController.create().loadExtensions();

        Channel sampleChannel = new Channel();
        sampleChannel.setId(channelId);
        sampleChannel.setName("Sample Channel");
        sampleChannel.setDescription("This is a sample channel");
        sampleChannel.setEnabled(true);
        sampleChannel.setVersion(configurationController.getServerVersion());
        sampleChannel.setRevision(0);

        sampleChannel.setLastModified(Calendar.getInstance());

        sampleChannel.setPostprocessingScript("return 1;");
        sampleChannel.setDeployScript("return 1;");
        sampleChannel.setPreprocessingScript("return 1;");
        sampleChannel.setShutdownScript("return 1;");

        Connector sourceConnector = new Connector();
        sourceConnector.setMode(Connector.Mode.SOURCE);
        sourceConnector.setTransportName("File Reader");

        sampleChannel.setSourceConnector(sourceConnector);

        Properties sampleProperties = new Properties();
        sampleProperties.setProperty("testProperty", "true");
        sampleChannel.setProperties(sampleProperties);

        channelController.updateChannel(sampleChannel, null, true);

        for (int i = 0; i < 10; i++) {
            MessageObject sampleMessageObject = new MessageObject();
            sampleMessageObject.setId(UUID.randomUUID().toString());
            sampleMessageObject.setServerId(configurationController.getServerId());
            sampleMessageObject.setChannelId(channelId);

            sampleMessageObject.setSource("SendingFacility" + i);
            sampleMessageObject.setType("ADT-A0" + i);

            sampleMessageObject.setConnectorName("SampleConnector");
            sampleMessageObject.setDateCreated(Calendar.getInstance());
            sampleMessageObject.setVersion("2.3.1");
            sampleMessageObject.setEncrypted(false);
            sampleMessageObject.setStatus(MessageObject.Status.TRANSFORMED);

            sampleMessageObject.setEncodedData("sample encoded data");
            sampleMessageObject.setEncodedDataProtocol(MessageObject.Protocol.XML);

            sampleMessageObject.setRawData("<raw_data></raw_data>");
            sampleMessageObject.setRawDataProtocol(MessageObject.Protocol.XML);

            sampleMessageObject.setTransformedData("sample transformed data");
            sampleMessageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);

            Map<String, String> sampleVariableMap = new HashMap<String, String>();
            sampleVariableMap.put("testVariable", "testValue");

            sampleMessageObject.setConnectorMap(sampleVariableMap);
            sampleMessageObject.setErrors("invalid header");

            sampleMessageObjectList.add(sampleMessageObject);
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        SqlConfig.getSqlMapClient().update("Message.dropTempMessageTable", messageTempTableUID);
    }

    public void testUpdateMessage() throws ControllerException {
        MessageObject sampleMessageObject = sampleMessageObjectList.get(0);
        messageObjectController.updateMessage(sampleMessageObject, true);

        MessageObjectFilter testFilter = new MessageObjectFilter();
        testFilter.setId(sampleMessageObject.getId());
        messageObjectController.createMessagesTempTable(testFilter, messageTempTableUID, forceTempTableCreation);
        List<MessageObject> testMessageObjectList = messageObjectController.getMessagesByPage(-1, -1, 0, messageTempTableUID, true);
        MessageObject testMessageObject = testMessageObjectList.get(0);

        Assert.assertEquals(1, testMessageObjectList.size());
        Assert.assertEquals(sampleMessageObject, testMessageObject);
    }

    public void testGetMessages() throws ControllerException {
        insertSampleMessages();

        MessageObjectFilter testFilter = new MessageObjectFilter();
        testFilter.setChannelId(channelId);
        testFilter.setStartDate(Calendar.getInstance());
        testFilter.setEndDate(Calendar.getInstance());

        messageObjectController.createMessagesTempTable(testFilter, messageTempTableUID, forceTempTableCreation);
        List<MessageObject> testMessageObjectList = messageObjectController.getMessagesByPage(-1, -1, 0, messageTempTableUID, true);

        for (Iterator<MessageObject> iter = sampleMessageObjectList.iterator(); iter.hasNext();) {
            MessageObject sampleMessageObject = iter.next();
            Assert.assertTrue(testMessageObjectList.contains(sampleMessageObject));
        }
    }

    public void testGetMessagesByFilter() throws ControllerException {
        insertSampleMessages();

        List<MessageObject> testMessageObjectList = null;
        MessageObjectFilter testFilter = null;

        testFilter = new MessageObjectFilter();
        testFilter.setChannelId(channelId);
        testFilter.setStartDate(Calendar.getInstance());
        testFilter.setEndDate(Calendar.getInstance());
        messageObjectController.createMessagesTempTable(testFilter, messageTempTableUID, forceTempTableCreation);
        testMessageObjectList = messageObjectController.getMessagesByPage(-1, -1, 0, messageTempTableUID, true);
        Assert.assertEquals(10, testMessageObjectList.size());

        /*
         * testFilter = new MessageObjectFilter();
         * testFilter.setType("ADT-A01");
         * testFilter.setSource("SendingFacility1");
         * messageObjectController.createMessagesTempTable(testFilter,
         * messageTempTableUID, forceTempTableCreation); testMessageObjectList =
         * messageObjectController.getMessagesByPage(-1, -1, 0,
         * messageTempTableUID, true); Assert.assertEquals(1,
         * testMessageObjectList.size());
         */
    }

    public void testGetMessageCount() throws ControllerException {
        insertSampleMessages();

        MessageObjectFilter testFilter = new MessageObjectFilter();
        testFilter.setChannelId(channelId);
        messageObjectController.createMessagesTempTable(testFilter, messageTempTableUID, forceTempTableCreation);
        List<MessageObject> testMessageObjectList = messageObjectController.getMessagesByPage(-1, -1, 0, messageTempTableUID, true);
        // Assert.assertEquals(testMessageObjectList.size(),
        // messageObjectController.createMessagesTempTable(testFilter,
        // messageTempTableUID, forceTempTableCreation));
        Assert.assertEquals(10, testMessageObjectList.size());
    }

    public void testRemoveMessages() throws ControllerException {
        insertSampleMessages();

        MessageObject sampleMessageObject = sampleMessageObjectList.get(0);
        MessageObjectFilter testRemoveFilter = new MessageObjectFilter();
        testRemoveFilter.setId(sampleMessageObject.getId());
        testRemoveFilter.setStatus(MessageObject.Status.TRANSFORMED);
        messageObjectController.removeMessages(testRemoveFilter);

        MessageObjectFilter testFilter = new MessageObjectFilter();
        testFilter.setChannelId(channelId);
        messageObjectController.createMessagesTempTable(testFilter, messageTempTableUID, forceTempTableCreation);
        List<MessageObject> testMessageObjectList = messageObjectController.getMessagesByPage(-1, -1, 0, messageTempTableUID, true);

        Assert.assertEquals(sampleMessageObjectList.size() - 1, testMessageObjectList.size());
    }

    public void testClearMessages() throws ControllerException {
        insertSampleMessages();
        messageObjectController.clearMessages(channelId);

        MessageObjectFilter testFilter = new MessageObjectFilter();
        testFilter.setChannelId(channelId);
        messageObjectController.createMessagesTempTable(testFilter, messageTempTableUID, forceTempTableCreation);
        List<MessageObject> testMessageObjectList = messageObjectController.getMessagesByPage(-1, -1, 0, messageTempTableUID, true);

        Assert.assertEquals(0, testMessageObjectList.size());
    }

    public void testAttachment() throws ControllerException {
        insertSampleMessages();
        messageObjectController.clearMessages(channelId);

        // Create Attachment
        Attachment attachment = new Attachment();
        attachment.setAttachmentId(UUIDGenerator.getUUID());

        attachment.setData("test".getBytes());
        attachment.setSize("test".length());
        attachment.setType("test");
        MessageObject mo = sampleMessageObjectList.get(0);
        mo.getId();
        attachment.setMessageId(mo.getId());

        // Add attachment
        messageObjectController.insertAttachment(attachment);
        mo.setAttachment(true);
        // update message
        messageObjectController.updateMessage(mo, true);

        // test getting attachment by attachmentId
        Attachment attach1 = messageObjectController.getAttachment(attachment.getAttachmentId());
        // test getting attachment by messageid
        List<Attachment> attachments = messageObjectController.getAttachmentsByMessageId(mo.getId());
        Attachment attach2 = attachments.get(0);
        Assert.assertEquals(attach1, attach2);
        Assert.assertEquals("test".getBytes(), attach1.getData());
        Assert.assertEquals("test".length(), attach1.getSize());
        Assert.assertEquals("test", attach1.getType());
    }

    private void insertSampleMessages() {
        for (Iterator<MessageObject> iter = sampleMessageObjectList.iterator(); iter.hasNext();) {
            MessageObject messageObject = iter.next();
            messageObjectController.updateMessage(messageObject, true);
        }
    }
}
