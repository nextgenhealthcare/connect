/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.mirth.connect.client.core.Client;
import com.mirth.connect.connectors.vm.VmDispatcherProperties;
import com.mirth.connect.connectors.vm.VmReceiverProperties;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.ChannelLock;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.data.passthru.PassthruDaoFactory;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.Filter;
import com.mirth.connect.model.Transformer;

public class TestUtils {
    public static Channel getChannel(String channelId) {
        Transformer sourceTransformer = new Transformer();
        sourceTransformer.setInboundDataType("HL7");
        sourceTransformer.setOutboundDataType("HL7");

        Connector sourceConnector = new Connector();
        sourceConnector.setEnabled(true);
        sourceConnector.setMetaDataId(0);
        sourceConnector.setMode(Mode.SOURCE);
        sourceConnector.setFilter(new Filter());
        sourceConnector.setTransformer(sourceTransformer);
        sourceConnector.setTransportName("Channel Reader");
        sourceConnector.setProperties(new VmReceiverProperties());

        Transformer destinationTransformer = new Transformer();
        destinationTransformer.setInboundDataType("HL7");
        destinationTransformer.setOutboundDataType("HL7");

        Connector destinationConnector = new Connector();
        destinationConnector.setEnabled(true);
        destinationConnector.setMetaDataId(1);
        destinationConnector.setMode(Mode.DESTINATION);
        destinationConnector.setFilter(new Filter());
        destinationConnector.setTransformer(destinationTransformer);
        destinationConnector.setTransportName("Channel Writer");
        destinationConnector.setProperties(new VmDispatcherProperties());

        Channel channel = new Channel();
        channel.setId(channelId);
        channel.setEnabled(true);
        channel.setName(channelId);
        channel.setRevision(1);
        channel.setSourceConnector(sourceConnector);
        channel.addDestination(destinationConnector);
        channel.setLastModified(Calendar.getInstance());

        return channel;
    }

    public static DashboardStatus getDashboardStatus(Client client, String channelId) throws Exception {
        List<DashboardStatus> statuses = client.getChannelStatusList();
        DashboardStatus channelStatus = null;

        for (DashboardStatus status : statuses) {
            if (status.getChannelId() == channelId) {
                channelStatus = status;
            }
        }

        return channelStatus;
    }

    public static Message createTestProcessedMessage() {
        return createTestProcessedMessage("testchannelid", "testserverid", 1, com.mirth.connect.server.controllers.tests.TestUtils.TEST_HL7_MESSAGE);
    }

    public static Message createTestProcessedMessage(String channelId, String serverId, long messageId, String content) {
        Calendar receivedDate = Calendar.getInstance();

        Message message = new Message();
        message.setMessageId(messageId);
        message.setChannelId(channelId);
        message.setServerId(serverId);
        message.setReceivedDate(receivedDate);
        message.setProcessed(true);

        ConnectorMessage sourceMessage = new ConnectorMessage(channelId, message.getMessageId(), 0, serverId, message.getReceivedDate(), Status.TRANSFORMED);
        message.getConnectorMessages().put(0, sourceMessage);

        ConnectorMessage destinationMessage = new ConnectorMessage(channelId, message.getMessageId(), 1, serverId, message.getReceivedDate(), Status.SENT);
        message.getConnectorMessages().put(1, destinationMessage);

        sourceMessage.setRaw(new MessageContent(channelId, message.getMessageId(), 0, ContentType.RAW, content, null, false));
        destinationMessage.setRaw(new MessageContent(channelId, message.getMessageId(), 1, ContentType.RAW, content, null, false));

        return message;
    }

    public static boolean tableExists(Connection connection, String tableName) throws SQLException {
        ResultSet resultSet = null;

        try {
            DatabaseMetaData metaData = connection.getMetaData();
            resultSet = metaData.getTables(null, null, tableName, null);

            if (resultSet.next()) {
                return true;
            }

            resultSet = metaData.getTables(null, null, tableName.toUpperCase(), null);
            return resultSet.next();
        } finally {
            resultSet.close();
        }
    }

    public static class DummyChannel extends com.mirth.connect.donkey.server.channel.Channel {
        private List<RawMessage> rawMessages = new ArrayList<RawMessage>();
        private long messageIdSequence = 1;

        public DummyChannel(String channelId, String serverId) {
            setChannelId(channelId);
            setServerId(serverId);
            lock(ChannelLock.DEBUG);
            setDaoFactory(new PassthruDaoFactory());
        }

        @Override
        protected DispatchResult dispatchRawMessage(RawMessage rawMessage, boolean batch) throws ChannelException {
            rawMessages.add(rawMessage);
            long messageId = messageIdSequence++;

            if (getSourceConnector().isRespondAfterProcessing()) {
                return new DummyDispatchResult(messageId, TestUtils.createTestProcessedMessage(getChannelId(), getServerId(), messageId, rawMessage.getRawData()), null, true, false, false, true);
            }

            return new DummyDispatchResult(messageId, null, null, false, false, false, false);
        }

        public List<RawMessage> getRawMessages() {
            return rawMessages;
        }
    }

    // extend DispatchResult so that we can access it's protected constructor
    public static class DummyDispatchResult extends DispatchResult {
        public DummyDispatchResult(long messageId, Message processedMessage, Response selectedResponse, boolean markAsProcessed, boolean removeContent, boolean removeAttachments, boolean lockAcquired) {
            super(messageId, processedMessage, selectedResponse, markAsProcessed, removeContent, removeAttachments, lockAcquired);
        }
    }
}
