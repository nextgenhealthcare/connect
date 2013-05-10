/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test.util;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DispatcherConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.QueueConnectorProperties;

@SuppressWarnings("serial")
public class TestConnectorProperties extends ConnectorProperties implements DispatcherConnectorPropertiesInterface {
    private QueueConnectorProperties queueConnectorProperties = new QueueConnectorProperties();

    @Override
    public String toFormattedString() {
        return null;
    }

    @Override
    public String getProtocol() {
        return "Test Protocol";
    }

    @Override
    public String getName() {
        return "Test Connector";
    }

    @Override
    public QueueConnectorProperties getQueueConnectorProperties() {
        return queueConnectorProperties;
    }
    
    @Override
    public ConnectorProperties clone() {
        return this;
    }
}
