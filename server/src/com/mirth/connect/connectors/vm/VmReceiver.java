/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.vm;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;

public class VmReceiver extends SourceConnector {

    private ConnectorType connectorType = ConnectorType.LISTENER;
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();

    @Override
    public void onDeploy() throws DeployException {}

    @Override
    public void onUndeploy() throws UndeployException {}

    @Override
    public void onStart() throws StartException {
        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.INITIALIZED);
    }

    @Override
    public void onStop() throws StopException {
        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.DISCONNECTED);
    }

    @Override
    public void handleRecoveredResponse(DispatchResult dispatchResult) {
        finishDispatch(dispatchResult);
    }

    @Override
    public DispatchResult dispatchRawMessage(RawMessage rawMessage) throws ChannelException {
        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.BUSY);
        return super.dispatchRawMessage(rawMessage);
    }

    @Override
    public void finishDispatch(DispatchResult dispatchResult, boolean attemptedResponse, String errorMessage) {
        super.finishDispatch(dispatchResult, attemptedResponse, errorMessage);
        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.DONE);
    }
}
