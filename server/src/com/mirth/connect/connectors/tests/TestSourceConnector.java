/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tests;

import java.util.ArrayList;
import java.util.List;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.MessageResponse;
import com.mirth.connect.donkey.server.channel.SourceConnector;

public class TestSourceConnector extends SourceConnector {
    private List<MessageResponse> recoveredMessageResponses = new ArrayList<MessageResponse>();

    @Override
    public void onDeploy() throws DeployException {
        // TODO Auto-generated method stub
    }

    @Override
    public void onUndeploy() throws UndeployException {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStart() throws StartException {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStop() throws StopException {
        // TODO Auto-generated method stub
    }

    @Override
    public void handleRecoveredResponse(MessageResponse messageResponse) {
        recoveredMessageResponses.add(messageResponse);
    }

    public MessageResponse readTestMessage(String raw) throws ChannelException {
        RawMessage rawMessage = new RawMessage(raw);
        MessageResponse messageResponse = null;
        
        try {
            messageResponse = handleRawMessage(rawMessage);
        } finally {
            storeMessageResponse(messageResponse);
        }
        
        return messageResponse;
    }

    public List<MessageResponse> getRecoveredResponses() {
        return recoveredMessageResponses;
    }
}
