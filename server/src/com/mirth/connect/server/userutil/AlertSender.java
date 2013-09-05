/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.userutil.ImmutableConnectorMessage;

/**
 * Allows users to dispatch error events which can be alerted on.
 */
public class AlertSender {
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private String channelId;
    private Integer metaDataId;
    private String connectorName;

    /**
     * Instantiates a new AlertSender.
     * 
     * @param channelId
     *            The ID of the channel to associate dispatched alert events
     *            with.
     */
    public AlertSender(String channelId) {
        this.channelId = channelId;
    }

    /**
     * Instantiates a new AlertSender.
     * 
     * @param connectorMessage
     *            The connector message to associate dispatched alert events
     *            with.
     */
    public AlertSender(ImmutableConnectorMessage connectorMessage) {
        channelId = connectorMessage.getChannelId();
        metaDataId = connectorMessage.getMetaDataId();
        connectorName = connectorMessage.getConnectorName();
    }

    /**
     * Dispatches an error event that can be alerted on.
     * 
     * @param errorMessage
     *            A custom error message to include with the error event.
     */
    public void sendAlert(String errorMessage) {
        eventController.dispatchEvent(new ErrorEvent(channelId, metaDataId, ErrorEventType.USER_DEFINED_TRANSFORMER, connectorName, null, errorMessage, null));
    }
}
