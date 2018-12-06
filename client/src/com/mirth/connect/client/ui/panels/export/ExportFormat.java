/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.panels.export;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.model.message.ContentType;

public class ExportFormat implements Serializable {
    private boolean destination = false;
    private String connectorType;
    private ContentType contentType;

    public ExportFormat(boolean destination, String connectorType, ContentType contentType) {
        this.destination = destination;
        this.contentType = contentType;
        this.connectorType = connectorType;
    }

    public boolean isDestination() {
        return destination;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String toString() {
        if (contentType != null) {
            return StringUtils.isNotEmpty(connectorType) ? connectorType + " - " + contentType.toString() : contentType.toString();
        }

        return "";
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }
}
