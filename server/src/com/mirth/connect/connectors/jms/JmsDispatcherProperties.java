/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jms;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DispatcherConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.QueueConnectorProperties;

public class JmsDispatcherProperties extends JmsConnectorProperties implements DispatcherConnectorPropertiesInterface {
    private static final String NAME = "JMS Sender";
    private static final String PROTOCOL = "JMS";

    private String template;
    private QueueConnectorProperties queueConnectorProperties;

    public JmsDispatcherProperties() {
        super();
        template = "${message.encodedData}";
        queueConnectorProperties = new QueueConnectorProperties();
    }
    
    public JmsDispatcherProperties(JmsDispatcherProperties props) {
        super(props);
        template = props.getTemplate();
        queueConnectorProperties = new QueueConnectorProperties(props.getQueueConnectorProperties());
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
    public QueueConnectorProperties getQueueConnectorProperties() {
        return queueConnectorProperties;
    }

    @Override
    public ConnectorProperties clone() {
        return new JmsDispatcherProperties(this);
    }
}
