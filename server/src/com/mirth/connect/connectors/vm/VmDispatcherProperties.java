/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.vm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorPropertiesInterface;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.purge.PurgeUtil;

public class VmDispatcherProperties extends ConnectorProperties implements DestinationConnectorPropertiesInterface {
    private DestinationConnectorProperties destinationConnectorProperties;

    private String channelId;
    private String channelTemplate;
    private List<String> mapVariables;

    public VmDispatcherProperties() {
        destinationConnectorProperties = new DestinationConnectorProperties(false);

        this.channelId = "none";
        this.channelTemplate = "${message.encodedData}";
        this.mapVariables = new ArrayList<String>();
    }

    public VmDispatcherProperties(VmDispatcherProperties props) {
        super(props);
        destinationConnectorProperties = new DestinationConnectorProperties(props.getDestinationConnectorProperties());

        channelId = props.getChannelId();
        channelTemplate = props.getChannelTemplate();
        mapVariables = props.getMapVariables();
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
    
    public List<String> getMapVariables() {
        return mapVariables;
    }

    public void setMapVariables(List<String> sourceMap) {
        this.mapVariables = sourceMap;
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
        builder.append("[MAP VARIABLES]");
        if (mapVariables != null) {
            for (String variable : mapVariables) {
                builder.append(newLine);
                builder.append(variable);
            }
        }
        builder.append(newLine);

        builder.append(newLine);
        builder.append("[CONTENT]");
        builder.append(newLine);
        builder.append(channelTemplate);
        return builder.toString();
    }

    @Override
    public DestinationConnectorProperties getDestinationConnectorProperties() {
        return destinationConnectorProperties;
    }

    @Override
    public ConnectorProperties clone() {
        return new VmDispatcherProperties(this);
    }

    @Override
    public boolean canValidateResponse() {
        return true;
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
    public void migrate3_1_0(DonkeyElement element) {
        super.migrate3_1_0(element);
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("destinationConnectorProperties", destinationConnectorProperties.getPurgedProperties());
        purgedProperties.put("channelTemplateLines", PurgeUtil.countLines(channelTemplate));
        return purgedProperties;
    }
}
