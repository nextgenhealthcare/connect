/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.panels.export;

import java.io.Serializable;

import com.mirth.connect.donkey.model.message.ContentType;

public class ExportFormat implements Serializable {
    private boolean destination = false;
    private ContentType contentType;

    public ExportFormat(boolean destination, ContentType contentType) {
        this.destination = destination;
        this.contentType = contentType;
    }

    public boolean isDestination() {
        return destination;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String toString() {
        if (contentType != null) {
            return (destination ? "Destination" : "Source") + " - " + contentType.toString();
        }

        return "";
    }
}
