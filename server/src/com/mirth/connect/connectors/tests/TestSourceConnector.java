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
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.SourceConnector;

public class TestSourceConnector extends SourceConnector {
    private List<DispatchResult> recoveredMessageResponses = new ArrayList<DispatchResult>();

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
    public void onHalt() throws HaltException {
        // TODO Auto-generated method stub
    }

    @Override
    public void handleRecoveredResponse(DispatchResult messageResponse) {
        recoveredMessageResponses.add(messageResponse);
    }

    public DispatchResult readTestMessage(String raw) throws ChannelException {
        RawMessage rawMessage = new RawMessage(raw);
        DispatchResult dispatchResult = null;
        
        try {
            dispatchResult = dispatchRawMessage(rawMessage);
        } finally {
            finishDispatch(dispatchResult);
        }
        
        return dispatchResult;
    }

    public List<DispatchResult> getRecoveredResponses() {
        return recoveredMessageResponses;
    }
}
