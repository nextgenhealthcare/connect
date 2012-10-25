/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test.util;

import java.util.ArrayList;
import java.util.List;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.MessageResponse;
import com.mirth.connect.donkey.server.channel.SourceConnector;

public class TestSourceConnector extends SourceConnector {
    protected TestConnectorProperties connectorProperties;
    private List<MessageResponse> recoveredResponses = new ArrayList<MessageResponse>();
    private boolean isDeployed = false;
    private List<Long> messageIds = new ArrayList<Long>();

    public List<MessageResponse> getRecoveredResponses() {
        return recoveredResponses;
    }

    public boolean isDeployed() {
        return isDeployed;
    }

    public List<Long> getMessageIds() {
        return messageIds;
    }

    @Override
    public void onDeploy() {
        this.connectorProperties = (TestConnectorProperties) getConnectorProperties();
        isDeployed = true;
    }

    @Override
    public void onUndeploy() {
        isDeployed = false;
    }

    @Override
    public void onStart() throws StartException {}

    @Override
    public void onStop() throws StopException {}

    @Override
    public void handleRecoveredResponse(MessageResponse messageResponse) {
        recoveredResponses.add(messageResponse);
    }

    public MessageResponse readTestMessage(String raw) throws ChannelException {
        RawMessage rawMessage = new RawMessage(raw);
        MessageResponse messageResponse = null;

        try {
            messageResponse = handleRawMessage(rawMessage);
        } finally {
            storeMessageResponse(messageResponse);
        }

        if (messageResponse != null) {
            messageIds.add(messageResponse.getMessageId());
        }

        return messageResponse;
    }
}
