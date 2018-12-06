/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.dashboardstatus;

import java.io.Serializable;
import java.text.SimpleDateFormat;

@SuppressWarnings("serial")
public class ConnectionLogItem implements Serializable {

    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

    private Long logId;
    private String serverId;
    private String channelId;
    private Long metadataId;
    private String dateAdded;
    private String channelName;
    private String connectorType;
    private String eventState;
    private String information;

    public ConnectionLogItem() {}

    public ConnectionLogItem(Long logId, String serverId, String channelId, Long metadataId, String dateAdded, String channelName, String connectorType, String eventState, String information) {
        this.logId = logId;
        this.serverId = serverId;
        this.channelId = channelId;
        this.metadataId = metadataId;
        this.dateAdded = dateAdded;
        this.channelName = channelName;
        this.connectorType = connectorType;
        this.eventState = eventState;
        this.information = information;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Long getMetadataId() {
        return metadataId;
    }

    public void setMetadataId(Long metadataId) {
        this.metadataId = metadataId;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public String getEventState() {
        return eventState;
    }

    public void setEventState(String eventState) {
        this.eventState = eventState;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

}
