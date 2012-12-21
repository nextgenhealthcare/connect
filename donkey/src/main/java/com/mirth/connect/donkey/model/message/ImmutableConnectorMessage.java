/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

import java.util.Calendar;
import java.util.Collections;
import java.util.Map;

public class ImmutableConnectorMessage {
    private ConnectorMessage connectorMessage;

    public ImmutableConnectorMessage(ConnectorMessage connectorMessage) {
        this.connectorMessage = connectorMessage;
    }

    public int getMetaDataId() {
        return connectorMessage.getMetaDataId();
    }

    public String getChannelId() {
        return connectorMessage.getChannelId();
    }

    public String getConnectorName() {
        return connectorMessage.getConnectorName();
    }

    public String getServerId() {
        return connectorMessage.getServerId();
    }

    public Calendar getDateCreated() {
        return (Calendar) connectorMessage.getDateCreated().clone();
    }

    public Status getStatus() {
        return connectorMessage.getStatus();
    }

    public ImmutableMessageContent getContent(ContentType contentType) {
        if (connectorMessage.getContent(contentType) != null) {
            return new ImmutableMessageContent(connectorMessage.getContent(contentType));
        }

        return null;
    }

    public ImmutableMessageContent getRaw() {
        if (connectorMessage.getRaw() != null) {
            return new ImmutableMessageContent(connectorMessage.getRaw());
        }

        return null;
    }

    public String getRawData() {
        if (connectorMessage.getRaw() != null) {
            return connectorMessage.getRaw().getContent();
        } else {
            return null;
        }
    }

    public ImmutableMessageContent getProcessedRaw() {
        if (connectorMessage.getProcessedRaw() != null) {
            return new ImmutableMessageContent(connectorMessage.getProcessedRaw());
        }

        return null;
    }

    public String getProcessedRawData() {
        if (connectorMessage.getProcessedRaw() != null) {
            return connectorMessage.getProcessedRaw().getContent();
        } else {
            return null;
        }
    }

    public ImmutableMessageContent getTransformed() {
        if (connectorMessage.getTransformed() != null) {
            return new ImmutableMessageContent(connectorMessage.getTransformed());
        }

        return null;
    }

    public String getTransformedData() {
        if (connectorMessage.getTransformed() != null) {
            return connectorMessage.getTransformed().getContent();
        } else {
            return null;
        }
    }

    public ImmutableMessageContent getEncoded() {
        if (connectorMessage.getEncoded() != null) {
            return new ImmutableMessageContent(connectorMessage.getEncoded());
        }

        return null;
    }

    public String getEncodedData() {
        if (connectorMessage.getEncoded() != null) {
            return connectorMessage.getEncoded().getContent();
        } else {
            return null;
        }
    }

    public ImmutableMessageContent getSent() {
        if (connectorMessage.getSent() != null) {
            return new ImmutableMessageContent(connectorMessage.getSent());
        }

        return null;
    }

    public String getSentData() {
        if (connectorMessage.getSent() != null) {
            return connectorMessage.getSent().getContent();
        } else {
            return null;
        }
    }

    public ImmutableMessageContent getResponse() {
        if (connectorMessage.getResponse() != null) {
            return new ImmutableMessageContent(connectorMessage.getResponse());
        }

        return null;
    }

    public Response getResponseData() {
        if (connectorMessage.getResponse() != null) {
            return Response.fromString(connectorMessage.getResponse().getContent());
        } else {
            return null;
        }
    }

    public ImmutableMessageContent getProcessedResponse() {
        if (connectorMessage.getProcessedResponse() != null) {
            return new ImmutableMessageContent(connectorMessage.getProcessedResponse());
        }

        return null;
    }

    public Response getProcessedResponseData() {
        if (connectorMessage.getProcessedResponse() != null) {
            return Response.fromString(connectorMessage.getProcessedResponse().getContent());
        } else {
            return null;
        }
    }

    public long getMessageId() {
        return connectorMessage.getMessageId();
    }

    public Map<String, Object> getConnectorMap() {
        return Collections.unmodifiableMap(connectorMessage.getConnectorMap());
    }

    public Map<String, Object> getChannelMap() {
        return Collections.unmodifiableMap(connectorMessage.getChannelMap());
    }

    public Map<String, Object> getResponseMap() {
        return Collections.unmodifiableMap(connectorMessage.getResponseMap());
    }

    public String getErrors() {
        return connectorMessage.getErrors();
    }

    public String toString() {
        return connectorMessage.toString();
    }
}
