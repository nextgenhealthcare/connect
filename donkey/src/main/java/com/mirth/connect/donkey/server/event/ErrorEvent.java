/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.event;

import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.event.Event;

public class ErrorEvent extends Event {

    private String channelId;
    private Integer metaDataId;
    private ErrorEventType type;
    private String connectorName;
    private String connectorType;
    private String customMessage;
    private Throwable throwable;

    public ErrorEvent(String channelId, Integer metaDataId, ErrorEventType type, String connectorName, String connectorType, String customMessage, Throwable throwable) {
        this.channelId = channelId;
        this.metaDataId = metaDataId;
        this.type = type;
        this.connectorName = connectorName;
        this.connectorType = connectorType;
        this.customMessage = customMessage;
        this.throwable = throwable;
    }

    public Integer getMetaDataId() {
        return metaDataId;
    }

    public void setMetaDataId(Integer metaDataId) {
        this.metaDataId = metaDataId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public ErrorEventType getType() {
        return type;
    }

    public void setType(ErrorEventType type) {
        this.type = type;
    }

    public String getConnectorName() {
        return connectorName;
    }

    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
