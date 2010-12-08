/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.util.Calendar;
import java.util.Properties;

import com.mirth.connect.connectors.file.FileWriterProperties;
import com.mirth.connect.connectors.mllp.LLPListenerProperties;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.model.Filter;
import com.mirth.connect.model.MessageObject.Protocol;
import com.mirth.connect.model.Transformer;

public abstract class ChannelWizardPlugin extends ClientPlugin {

    public ChannelWizardPlugin(String name) {
        super(name);
    }
    
    public abstract Channel runWizard();

    public Channel getDefaultNewChannel() {
        Channel channel = new Channel();

        channel.setName("New Channel");
        channel.setEnabled(true);
        channel.getProperties().setProperty("initialState", "Started");
        channel.setLastModified(Calendar.getInstance());

        Connector sourceConnector = new Connector();
        sourceConnector.setEnabled(true);
        sourceConnector.setFilter(new Filter());
        Transformer sourceTransformer = new Transformer();
        sourceTransformer.setInboundProtocol(Protocol.HL7V2);
        sourceConnector.setTransformer(sourceTransformer);
        sourceConnector.setMode(Mode.SOURCE);
        sourceConnector.setName("sourceConnector");
        sourceConnector.setTransportName(LLPListenerProperties.name);
        Properties sourceProperties = new LLPListenerProperties().getDefaults();
        sourceConnector.setProperties(sourceProperties);
        channel.setSourceConnector(sourceConnector);

        Connector destinationConnector = new Connector();
        destinationConnector.setEnabled(true);
        destinationConnector.setFilter(new Filter());
        destinationConnector.setTransformer(new Transformer());
        destinationConnector.setMode(Mode.DESTINATION);
        destinationConnector.setName("Destination 1");
        destinationConnector.setTransportName(FileWriterProperties.name);
        Properties destinationConnectorProperties = new FileWriterProperties().getDefaults();
        destinationConnector.setProperties(destinationConnectorProperties);
        channel.getDestinationConnectors().add(destinationConnector);

        return channel;
    }
}
