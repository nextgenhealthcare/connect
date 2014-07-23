/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.server.channel.Connector;

public interface FileConfiguration {

    public void configureConnectorDeploy(Connector connector, ConnectorProperties connectorProperties) throws Exception;

    public void configureConnectorUndeploy(Connector connector);
}