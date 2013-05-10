package com.mirth.connect.donkey.model.channel;

public interface DispatcherConnectorPropertiesInterface {
    
    public QueueConnectorProperties getQueueConnectorProperties();

    public ConnectorProperties clone();
}