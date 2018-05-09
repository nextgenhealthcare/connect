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

public class DefaultFileConfiguration implements FileConfiguration {

    @Override
    public void configureConnectorDeploy(Connector connector, ConnectorProperties connectorProperties) throws Exception {
        FileConnector fileConnector = new FileConnector(connector.getChannelId(), connectorProperties, connector);

        if (connector instanceof FileReceiver) {
            ((FileReceiver) connector).setFileConnector(fileConnector);
        } else if (connector instanceof FileDispatcher) {
            ((FileDispatcher) connector).setFileConnector(fileConnector);
        }
    }

    @Override
    public void configureConnectorUndeploy(Connector connector) {}
}