/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.vm;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.QueueConnectorProperties;
import com.mirth.connect.donkey.model.channel.QueueConnectorPropertiesInterface;

public class VmSenderProperties extends ConnectorProperties implements QueueConnectorPropertiesInterface {
    private QueueConnectorProperties queueConnectorProperties;
    
    private String channelId;
    private boolean isWaitForDestinations;
    private String channelTemplate;

    public VmSenderProperties() {
        queueConnectorProperties = new QueueConnectorProperties();
        
        this.channelId = "sink";
        this.isWaitForDestinations = false;
        this.channelTemplate = "${message.encodedData}";
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public boolean isWaitForDestinations() {
        return isWaitForDestinations;
    }

    public void setWaitForDestinations(boolean isWaitForDestinations) {
        this.isWaitForDestinations = isWaitForDestinations;
    }

    public String getChannelTemplate() {
        return channelTemplate;
    }

    public void setChannelTemplate(String channelTemplate) {
        this.channelTemplate = channelTemplate;
    }

    @Override
    public String getName() {
        return "Channel Writer";
    }

    @Override
    public String getProtocol() {
        return "VM";
    }

    @Override
    public String toFormattedString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QueueConnectorProperties getQueueConnectorProperties() {
        return queueConnectorProperties;
    }

}
