package com.mirth.connect.connectors.jms;

public class JmsReceiverProperties extends JmsConnectorProperties {
    private String clientId;
    private String selector;
    private boolean durableTopic;

    public JmsReceiverProperties() {
        clientId = "";
        selector = "";
        durableTopic = false;
    }

    @Override
    public String getName() {
        return "JMS Listener";
    }

    @Override
    public String toFormattedString() {
        return null;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public boolean isDurableTopic() {
        return durableTopic;
    }

    public void setDurableTopic(boolean durableTopic) {
        this.durableTopic = durableTopic;
    }
}
