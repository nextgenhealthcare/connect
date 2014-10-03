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

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorPropertiesInterface;
import com.mirth.connect.donkey.util.purge.PurgeUtil;

public class JmsDispatcherProperties extends JmsConnectorProperties implements DestinationConnectorPropertiesInterface {
    private static final String NAME = "JMS Sender";
    private static final String PROTOCOL = "JMS";

    private String template;
    private DestinationConnectorProperties destinationConnectorProperties;

    public JmsDispatcherProperties() {
        super();
        template = "${message.encodedData}";
        destinationConnectorProperties = new DestinationConnectorProperties();
    }

    public JmsDispatcherProperties(JmsDispatcherProperties props) {
        super(props);
        template = props.getTemplate();
        destinationConnectorProperties = new DestinationConnectorProperties(props.getDestinationConnectorProperties());
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toFormattedString() {
        return super.toFormattedString() + "\n[CONTENT]\n" + template;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    @Override
    public DestinationConnectorProperties getDestinationConnectorProperties() {
        return destinationConnectorProperties;
    }

    @Override
    public ConnectorProperties clone() {
        return new JmsDispatcherProperties(this);
    }

    @Override
    public boolean canValidateResponse() {
        return false;
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("templateLines", PurgeUtil.countLines(template));
        purgedProperties.put("destinationConnectorProperties", destinationConnectorProperties.getPurgedProperties());
        return purgedProperties;
    }
}
