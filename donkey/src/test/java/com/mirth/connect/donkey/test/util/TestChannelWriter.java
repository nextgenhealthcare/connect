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
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.channel.DispatchResult;

public class TestChannelWriter extends DestinationConnector {
    private Channel destinationChannel;

    public TestChannelWriter(Channel destinationChannel) {
        this.destinationChannel = destinationChannel;
    }

    @Override
    public void onDeploy() throws DeployException {}

    @Override
    public void onUndeploy() throws UndeployException {}

    @Override
    public void onStart() throws StartException {}

    @Override
    public void onStop() throws StopException {}
    
    @Override
    public void onHalt() throws HaltException {}

    @Override
    public void replaceConnectorProperties(ConnectorProperties connectorProperties, ConnectorMessage message) {}

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage message) {
        DispatchResult dispatchResult = null;

        try {
            dispatchResult = destinationChannel.getSourceConnector().dispatchRawMessage(new RawMessage(message.getEncoded().getContent()));
        } catch (ChannelException e) {
            return new Response(Status.ERROR, null);
        } finally {
            destinationChannel.getSourceConnector().finishDispatch(dispatchResult);
        }

        Response response = dispatchResult.getSelectedResponse();
        
        if (response == null) {
            response = new Response();
        }
        
        return response;
    }
}
