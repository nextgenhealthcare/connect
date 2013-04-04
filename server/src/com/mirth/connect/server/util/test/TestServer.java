/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util.test;

import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.mirth.connect.client.core.Client;
import com.mirth.connect.connectors.js.JavaScriptDispatcherProperties;
import com.mirth.connect.connectors.js.JavaScriptReceiverProperties;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.util.ImportConverter;
import com.mirth.connect.server.controllers.tests.TestUtils;
import com.mirth.connect.server.message.DataTypeFactory;

public class TestServer {
    private final static String CLIENT_ADDRESS = "https://localhost:8443";
    private final static String VERSION = "3.0.0";
    private final static String CHANNEL_XML_FILE = "testchannel.xml";

    @Test
    public final void test() throws Exception {
        Channel channel = getChannel();
        String channelId = channel.getId();

        Client client = new Client(CLIENT_ADDRESS);
        client.login("admin", "admin", VERSION);
        client.updateChannel(channel, true);
        client.startChannel(channelId);
        client.processMessage(channelId, TestUtils.TEST_HL7_MESSAGE);
        client.stopChannel(channelId);
    }

    private Channel getChannel() {
        JavaScriptReceiverProperties receiverProperties = new JavaScriptReceiverProperties();
        receiverProperties.setScript("");

        Transformer sourceTransformer = new Transformer();
        sourceTransformer.setInboundDataType("HL7V2");

        Connector sourceConnector = new Connector();
        sourceConnector.setProperties(receiverProperties);
        sourceConnector.setEnabled(true);
        sourceConnector.setName("testsourceconnector");
        sourceConnector.setVersion("0");
        sourceConnector.setTransportName("JavaScript Reader");
        sourceConnector.setTransformer(sourceTransformer);

        Channel channel = new Channel();
        channel.setId("testchannel");
        channel.setName("testchannel");
        channel.setVersion("0");
        channel.setLastModified(Calendar.getInstance());
        channel.setRevision(0);
        channel.setSourceConnector(sourceConnector);
        channel.setDescription("Test Channel");

        JavaScriptDispatcherProperties dispatcherProperties = new JavaScriptDispatcherProperties();
        dispatcherProperties.setScript("");
        dispatcherProperties.getQueueConnectorProperties().setSendFirst(true);

        Transformer destinationTransformer = new Transformer();
        destinationTransformer.setOutboundDataType("HL7V2");

        Connector destinationConnector = new Connector();
        destinationConnector.setProperties(dispatcherProperties);
        destinationConnector.setEnabled(true);
        destinationConnector.setName("testdestinationconnector");
        destinationConnector.setVersion("0");
        destinationConnector.setTransportName("JavaScript Writer");
        destinationConnector.setTransformer(destinationTransformer);

        List<Connector> destinationConnectors = new ArrayList<Connector>();
        destinationConnectors.add(destinationConnector);

        channel.getDestinationConnectors().addAll(destinationConnectors);

        return channel;
    }

    private Channel getChannelFromXmlFile() throws Exception {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(CHANNEL_XML_FILE);

        if (stream == null) {
            fail("Could not find " + CHANNEL_XML_FILE);
        }

        String channelXml = ImportConverter.convertChannelString(IOUtils.toString(stream));
        return (Channel) ObjectXMLSerializer.getInstance().fromXML(channelXml);
    }
}
