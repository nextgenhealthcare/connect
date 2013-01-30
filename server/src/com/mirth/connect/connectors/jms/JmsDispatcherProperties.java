package com.mirth.connect.connectors.jms;

import com.mirth.connect.donkey.model.channel.QueueConnectorProperties;
import com.mirth.connect.donkey.model.channel.QueueConnectorPropertiesInterface;

public class JmsDispatcherProperties extends JmsConnectorProperties implements QueueConnectorPropertiesInterface {
    private static final String NAME = "JMS Sender";
    private static final String PROTOCOL = "JMS";
    
    private String template;
    private QueueConnectorProperties queueConnectorProperties;
    
    public JmsDispatcherProperties() {
        template = "${message.encodedData}";
        queueConnectorProperties = new QueueConnectorProperties();
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
        return super.toFormattedString() + "[CONTENT]\n" + template;
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
}
