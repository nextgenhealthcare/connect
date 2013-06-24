package com.mirth.connect.server.userutil;

import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;

public class AlertSender {
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private String channelId;
    private Integer metaDataId;
    private String connectorName;

    public AlertSender(String channelId) {
        this.channelId = channelId;
    }

    public AlertSender(ConnectorMessage connectorMessage) {
        channelId = connectorMessage.getChannelId();
        metaDataId = connectorMessage.getMetaDataId();
        connectorName = connectorMessage.getConnectorName();
    }

    public void sendAlert(String errorMessage) {
        eventController.dispatchEvent(new ErrorEvent(channelId, metaDataId, ErrorEventType.USER_DEFINED_TRANSFORMER, connectorName, errorMessage, null));
    }
}
