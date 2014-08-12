/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.vm;

import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.message.batch.BatchMessageException;
import com.mirth.connect.donkey.server.message.batch.ResponseHandler;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;

public class VmReceiver extends SourceConnector {

    private EventController eventController = ControllerFactory.getFactory().createEventController();

    @Override
    public void onDeploy() throws ConnectorTaskException {}

    @Override
    public void onUndeploy() throws ConnectorTaskException {}

    @Override
    public void onStart() throws ConnectorTaskException {
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));
    }

    @Override
    public void onStop() throws ConnectorTaskException {
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.DISCONNECTED));
    }

    @Override
    public void onHalt() throws ConnectorTaskException {
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.DISCONNECTED));
    }

    @Override
    public void handleRecoveredResponse(DispatchResult dispatchResult) {
        finishDispatch(dispatchResult);
    }

    @Override
    public DispatchResult dispatchRawMessage(RawMessage rawMessage) throws ChannelException {
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.RECEIVING));
        try {
            return super.dispatchRawMessage(rawMessage);
        } finally {
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));
        }
    }

    @Override
    public void dispatchBatchMessage(BatchRawMessage batchRawMessage, ResponseHandler responseHandler) throws BatchMessageException {
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.RECEIVING));
        try {
            super.dispatchBatchMessage(batchRawMessage, responseHandler);
        } finally {
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));
        }
    }
}
