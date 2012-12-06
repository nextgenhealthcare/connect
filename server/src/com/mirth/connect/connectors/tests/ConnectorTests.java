/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tests;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import com.mirth.connect.connectors.js.JavaScriptDispatcher;
import com.mirth.connect.connectors.js.JavaScriptDispatcherProperties;
import com.mirth.connect.connectors.js.JavaScriptReceiver;
import com.mirth.connect.connectors.js.JavaScriptReceiverProperties;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.server.controllers.tests.TestUtils;
import com.mirth.connect.server.util.GlobalVariableStore;

public class ConnectorTests {
    final public static int TEST_SIZE = 10;

    @Test
    public final void testJavaScriptReader() throws Exception {
        final String script = "return \"" + TestUtils.TEST_HL7_MESSAGE.replace("\n", "\\n") + "\";";

        JavaScriptReceiverProperties connectorProperties = new JavaScriptReceiverProperties();
        connectorProperties.setScript(script);
        connectorProperties.getPollConnectorProperties().setPollingFrequency(500);

        JavaScriptReceiver javaScriptReceiver = new JavaScriptReceiver();
        javaScriptReceiver.setConnectorProperties(connectorProperties);

        ChannelController.getInstance().deleteAllMessages(TestUtils.CHANNEL_ID);

        DummyChannel channel = new DummyChannel(TestUtils.CHANNEL_ID, TestUtils.SERVER_ID, javaScriptReceiver, null);
        channel.start();
        Thread.sleep(3250);
        channel.stop();

        assertEquals(7, channel.getProcessedMessageIds().size());
    }

    @Test
    public final void testJavaScriptWriter() throws Exception {
        final String script = "globalMap.put('message' + messageObject.getMessageId(), messageObject.getEncoded().getContent() + '123');";

        JavaScriptDispatcherProperties connectorProperties = new JavaScriptDispatcherProperties();
        connectorProperties.setScript(script);

        JavaScriptDispatcher javaScriptWriter = new JavaScriptDispatcher();
        javaScriptWriter.setConnectorProperties(connectorProperties);

        ChannelController.getInstance().deleteAllMessages(TestUtils.CHANNEL_ID);

        DummyChannel channel = new DummyChannel(TestUtils.CHANNEL_ID, TestUtils.SERVER_ID, null, javaScriptWriter);
        channel.start();

        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();
        
        for (int i = 0; i < TEST_SIZE; i++) {
            ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(TestUtils.TEST_HL7_MESSAGE), channel.getChannelId(), channel.getServerId(), dao).getConnectorMessages().get(0);
            dao.commit();

            Message message = channel.process(sourceMessage, true);
            connectorProperties = (JavaScriptDispatcherProperties) SerializationUtils.deserialize(message.getConnectorMessages().get(1).getSent().getContent().getBytes());

            assertEquals(TestUtils.TEST_HL7_MESSAGE + "123", GlobalVariableStore.getInstance().getVariables().get("message" + message.getMessageId()));
        }

        channel.stop();
        dao.close();
    }
}
