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
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.SourceConnector;

public class TestSourceConnector extends SourceConnector {
    protected TestConnectorProperties connectorProperties;
    private List<DispatchResult> recoveredResponses = new ArrayList<DispatchResult>();
    private boolean isDeployed = false;
    private List<Long> messageIds = new ArrayList<Long>();

    public List<DispatchResult> getRecoveredResponses() {
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
    public void handleRecoveredResponse(DispatchResult messageResponse) {
        recoveredResponses.add(messageResponse);
    }

    public DispatchResult readTestMessage(String raw) throws ChannelException {
        RawMessage rawMessage = new RawMessage(raw);
        DispatchResult dispatchResult = null;

        try {
            dispatchResult = dispatchRawMessage(rawMessage);
        } finally {
            finishDispatch(dispatchResult);
        }

        if (dispatchResult != null) {
            messageIds.add(dispatchResult.getMessageId());
        }

        return dispatchResult;
    }
}
