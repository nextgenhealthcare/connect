package com.mirth.connect.donkey.server.event;

import java.util.Map;

import com.mirth.connect.donkey.model.event.ChannelEventType;
import com.mirth.connect.donkey.model.message.Status;

public class DeployEvent extends ChannelEvent {

    private Map<Integer, Map<Status, Long>> connectorStatistics;

    public DeployEvent(String channelId, Map<Integer, Map<Status, Long>> connectorStatistics, ChannelEventType state) {
        super(channelId, state);
        this.connectorStatistics = connectorStatistics;
    }

    public Map<Integer, Map<Status, Long>> getConnectorStatistics() {
        return connectorStatistics;
    }

}
