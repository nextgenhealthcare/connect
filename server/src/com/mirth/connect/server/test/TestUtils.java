/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.test;

import java.util.Calendar;
import java.util.List;

import com.mirth.connect.client.core.Client;
import com.mirth.connect.connectors.vm.VmReceiverProperties;
import com.mirth.connect.connectors.vm.VmDispatcherProperties;
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
        channel.getDestinationConnectors().add(destinationConnector);
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
}
