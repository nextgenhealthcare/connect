package com.mirth.connect.plugins.dashboardstatus;

import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.Event;
import com.mirth.connect.donkey.server.event.EventType;
import com.mirth.connect.model.Connector;
import com.mirth.connect.server.ExtensionLoader;

public abstract class ConnectionStatusLogController {

    private static ConnectionStatusLogController instance = null;

    public static ConnectionStatusLogController getInstance() {
        synchronized (DefaultConnectionLogController.class) {
            if (instance == null) {
                instance = ExtensionLoader.getInstance().getControllerInstance(ConnectionStatusLogController.class);

                if (instance == null) {
                    instance = new DefaultConnectionLogController();
                }
            }

            return instance;
        }
    }

    public abstract void processEvent(Event event);

    public abstract LinkedList<ConnectionLogItem> getChannelLog(String serverId, String channelId, int fetchSize, Long lastLogId);

    public abstract Map<String, Object[]> getConnectorStateMap(String serverId);

    public abstract Map<String, Map<String, List<ConnectionStateItem>>> getConnectionStatesForServer(String serverId);

    public Set<EventType> getEventTypes() {
        Set<EventType> EventTypes = new HashSet<EventType>();

        EventTypes.add(EventType.CONNECTION_STATUS);

        return EventTypes;
    }

    public Connector getConnectorFromMetaDataId(List<Connector> connectors, int metaDataId) {
        for (Connector connector : connectors) {
            if (connector.getMetaDataId() == metaDataId) {
                return connector;
            }
        }

        return null;
    }

    public Color getColor(ConnectionStatusEventType type) {
        switch (type) {
            case IDLE:
            case CONNECTING:
            case WAITING_FOR_RESPONSE:
                return Color.yellow;

            case WRITING:
            case SENDING:
            case READING:
            case RECEIVING:
            case POLLING:
            case CONNECTED:
                return Color.green;

            case DISCONNECTED:
                return Color.red;

            case INFO:
                return Color.blue;

            default:
                return Color.black;
        }
    }
}
