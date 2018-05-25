package com.mirth.connect.plugins.dashboardstatus;

import java.util.concurrent.atomic.AtomicInteger;

import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;

public class ConnectionStateItem {
    private String serverId;
    private String channelId;
    private String metadataId;
    private ConnectionStatusEventType status;
    private AtomicInteger connectionCount;
    private AtomicInteger maxConnectionCount;

    public ConnectionStateItem(String serverId, String channelId, String metadataId, ConnectionStatusEventType status, int connectionCount, int maxConnectionCount) {
        this.serverId = serverId;
        this.channelId = channelId;
        this.metadataId = metadataId;
        this.status = status;
        this.connectionCount = new AtomicInteger(connectionCount);
        this.maxConnectionCount = new AtomicInteger(maxConnectionCount);
    }

    public int addToConnectionCount(int value) {
        return connectionCount.addAndGet(value);
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String newId) {
        serverId = newId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String newId) {
        channelId = newId;
    }

    public String getMetadataId() {
        return metadataId;
    }

    public void setMetadataId(String newId) {
        metadataId = newId;
    }

    public ConnectionStatusEventType getStatus() {
        return status;
    }

    public void setStatus(ConnectionStatusEventType newStatus) {
        status = newStatus;
    }

    public int getConnectionCount() {
        return connectionCount.get();
    }

    public void setConnectionCount(int newCount) {
        connectionCount = new AtomicInteger(newCount);
    }

    public int getMaxConnectionCount() {
        return maxConnectionCount.get();
    }

    public void setMaxConnectionCount(int newMax) {
        maxConnectionCount = new AtomicInteger(newMax);
    }
}
