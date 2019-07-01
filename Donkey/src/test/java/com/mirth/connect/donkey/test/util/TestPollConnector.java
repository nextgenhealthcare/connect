/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test.util;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.PollConnector;

public class TestPollConnector extends PollConnector {
    protected TestPollConnectorProperties connectorProperties;

    @Override
    public void onDeploy() throws ConnectorTaskException {
        connectorProperties = (TestPollConnectorProperties) getConnectorProperties();
    }

    @Override
    public void onUndeploy() throws ConnectorTaskException {}

    @Override
    public void onStart() throws ConnectorTaskException {}

    @Override
    public void onStop() throws ConnectorTaskException {}

    @Override
    public void onHalt() throws ConnectorTaskException {}

    @Override
    protected void poll() {
        DispatchResult dispatchResult = null;

        try {
            dispatchResult = dispatchRawMessage(new RawMessage(TestUtils.TEST_HL7_MESSAGE));
        } catch (ChannelException e) {
            throw new RuntimeException(e);
        } finally {
            finishDispatch(dispatchResult);
        }
    }

    @Override
    public void handleRecoveredResponse(DispatchResult dispatchResult) {
        finishDispatch(dispatchResult);
    }
}
