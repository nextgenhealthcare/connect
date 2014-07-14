/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.vm;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DispatcherConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.QueueConnectorProperties;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.purge.PurgeUtil;

public class VmDispatcherProperties extends ConnectorProperties implements DispatcherConnectorPropertiesInterface {
    private QueueConnectorProperties queueConnectorProperties;

    private String channelId;
    private String channelTemplate;

    public VmDispatcherProperties() {
        queueConnectorProperties = new QueueConnectorProperties();

        this.channelId = "none";
        this.channelTemplate = "${message.encodedData}";
    }

    public VmDispatcherProperties(VmDispatcherProperties props) {
        super(props);
        queueConnectorProperties = new QueueConnectorProperties(props.getQueueConnectorProperties());

        channelId = props.getChannelId();
        channelTemplate = props.getChannelTemplate();
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
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
        StringBuilder builder = new StringBuilder();
        String newLine = "\n";
        builder.append("CHANNEL ID: ");
        builder.append(channelId);
        builder.append(newLine);

        builder.append(newLine);
        builder.append("[CONTENT]");
        builder.append(newLine);
        builder.append(channelTemplate);
        return builder.toString();
    }

    @Override
    public QueueConnectorProperties getQueueConnectorProperties() {
        return queueConnectorProperties;
    }

    @Override
    public ConnectorProperties clone() {
        return new VmDispatcherProperties(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("queueConnectorProperties", queueConnectorProperties.getPurgedProperties());
        purgedProperties.put("channelId", channelId);
        purgedProperties.put("channelTemplateLines", PurgeUtil.countLines(channelTemplate));
        return purgedProperties;
    }
}
