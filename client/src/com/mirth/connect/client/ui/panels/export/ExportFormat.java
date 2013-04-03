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
