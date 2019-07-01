/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jms;

import java.util.Map;

import com.mirth.connect.donkey.model.channel.SourceConnectorProperties;
import com.mirth.connect.donkey.model.channel.SourceConnectorPropertiesInterface;
import com.mirth.connect.donkey.util.purge.PurgeUtil;

public class JmsReceiverProperties extends JmsConnectorProperties implements SourceConnectorPropertiesInterface {
    private SourceConnectorProperties sourceConnectorProperties;
    private String selector;
    private String reconnectIntervalMillis;
    private boolean durableTopic;

    public JmsReceiverProperties() {
        super();
        sourceConnectorProperties = new SourceConnectorProperties();
        selector = "";
        reconnectIntervalMillis = "10000";
        durableTopic = false;
    }

    @Override
    public SourceConnectorProperties getSourceConnectorProperties() {
        return sourceConnectorProperties;
    }

    @Override
    public String getName() {
        return "JMS Listener";
    }

    @Override
    public String toFormattedString() {
        String newLine = "\n";
        StringBuilder builder = new StringBuilder(super.toFormattedString());

        if (!selector.isEmpty()) {
            builder.append("SELECTOR: " + selector + newLine);
        }

        if (!isUseJndi()) {
            builder.append("DURABLE TOPIC: " + (durableTopic ? "yes" : "no") + newLine);
        }

        return builder.toString();
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getReconnectIntervalMillis() {
        return reconnectIntervalMillis;
    }

    public void setReconnectIntervalMillis(String reconnectIntervalMillis) {
        this.reconnectIntervalMillis = reconnectIntervalMillis;
    }

    public boolean isDurableTopic() {
        return durableTopic;
    }

    public void setDurableTopic(boolean durableTopic) {
        this.durableTopic = durableTopic;
    }

    @Override
    public boolean canBatch() {
        return true;
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("sourceConnectorProperties", sourceConnectorProperties.getPurgedProperties());
        purgedProperties.put("reconnectIntervalMillis", PurgeUtil.getNumericValue(reconnectIntervalMillis));
        purgedProperties.put("durableTopic", durableTopic);
        return purgedProperties;
    }
}
