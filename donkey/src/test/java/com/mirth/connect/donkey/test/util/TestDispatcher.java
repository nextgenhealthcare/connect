/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test.util;

import java.util.ArrayList;
import java.util.List;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;

public class TestDispatcher extends DestinationConnector {
    protected TestDispatcherProperties connectorProperties;
    final public static String TEST_RESPONSE_PREFIX = "response";
    private volatile boolean queueThreadRunning = false;
    private volatile Status returnStatus = Status.QUEUED;
    private List<Long> messageIds = new ArrayList<Long>();
    private boolean isDeployed = false;

    public TestDispatcher() {
        this.connectorProperties = new TestDispatcherProperties();
    }

    public List<Long> getMessageIds() {
        return messageIds;
    }

    public boolean isDeployed() {
        return isDeployed;
    }

    public boolean isQueueThreadRunning() {
        return queueThreadRunning;
    }

    public Status getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(Status returnStatus) {
        this.returnStatus = returnStatus;
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage message) {
        messageIds.add(message.getMessageId());
        return new Response(returnStatus, TEST_RESPONSE_PREFIX + message.getMessageId());
    }

    @Override
    public void run() {
        queueThreadRunning = true;
        super.run();
        queueThreadRunning = false;
    }

    @Override
    public void onDeploy() throws DeployException {
        isDeployed = true;
        this.connectorProperties = (TestDispatcherProperties) getConnectorProperties();
    }

    @Override
    public void onUndeploy() throws UndeployException {
        isDeployed = false;
    }

    @Override
    public void onStart() throws StartException {}

    @Override
    public void onStop() throws StopException {}
    
    @Override
    public void onHalt() throws HaltException {}

    @Override
    public void replaceConnectorProperties(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) {
        TestDispatcherProperties testDispatcherProperties = (TestDispatcherProperties) connectorProperties;
        testDispatcherProperties.setTemplate(TestUtils.TEST_HL7_MESSAGE);
    }
}
