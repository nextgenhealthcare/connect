/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.dashboardstatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;

public class ConnectionStateItem {
    private String serverId;
    private String channelId;
    private String metadataId;
    private ConnectionStatusEventType status;
    private AtomicInteger connectionCount;
    private AtomicInteger maxConnectionCount;
    private Logger logger = Logger.getLogger(getClass());

    public ConnectionStateItem(String serverId, String channelId, String metadataId, ConnectionStatusEventType status, int connectionCount, int maxConnectionCount) {
        this.serverId = serverId;
        this.channelId = channelId;
        this.metadataId = metadataId;
        this.status = status;
        this.connectionCount = new AtomicInteger(connectionCount);
        this.maxConnectionCount = new AtomicInteger(maxConnectionCount);
    }
    
    public ConnectionStateItem(ResultSet resultSet) {
        try {
            serverId = resultSet.getString("server_id");
            channelId = resultSet.getString("channel_id");
            metadataId = resultSet.getString("metadata_id").trim();
            status = ConnectionStatusEventType.valueOf(resultSet.getString("connection_status"));
            connectionCount = new AtomicInteger(resultSet.getInt("connection_count"));
            maxConnectionCount = new AtomicInteger(resultSet.getInt("connection_count_max"));
        } catch (SQLException e) {
            logger.error("Error instantiating a ConnectionStateItem from database ResultSet", e);
        }
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
    
    @Override
    public boolean equals(Object otherObject) {
        if (!(otherObject instanceof ConnectionStateItem)) {
            return false;
        }
        
        ConnectionStateItem otherStateItem = (ConnectionStateItem) otherObject;
        if (!(otherStateItem.getServerId().equals(getServerId()))) {
            return false;
        }
        
        if (!(otherStateItem.getChannelId().equals(getChannelId()))) {
            return false;
        }
        
        if (!(otherStateItem.getMetadataId().equals(getMetadataId()))) {
            return false;
        }
        
        if (!(otherStateItem.getStatus().equals(getStatus()))) {
            return false;
        }
        
        if (otherStateItem.getConnectionCount() != getConnectionCount()) {
            return false;
        }
        
        if (otherStateItem.getMaxConnectionCount() != getMaxConnectionCount()) {
            return false;
        }
        
        return true;
    }
}
