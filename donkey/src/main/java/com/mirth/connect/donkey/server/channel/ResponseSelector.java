/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.DataType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Constants;

public class ResponseSelector {
    private static Map<Status, Integer> statusPrecedenceMap = new HashMap<Status, Integer>();

    static {
        int i = Constants.RESPONSE_STATUS_PRECEDENCE.length;

        for (Status status : Constants.RESPONSE_STATUS_PRECEDENCE) {
            statusPrecedenceMap.put(status, i--);
        }
    }

    private DataType dataType;
    private int numDestinations;
    private String respondFromName;

    public ResponseSelector(DataType dataType) {
        this.dataType = dataType;
    }

    void setNumDestinations(int numDestinations) {
        this.numDestinations = numDestinations;
    }

    public String getRespondFromName() {
        return respondFromName;
    }

    public void setRespondFromName(String respondFromName) {
        this.respondFromName = respondFromName;
    }

    public boolean canRespond() {
        return respondFromName != null && !respondFromName.equals(Constants.RESPONSE_NONE);
    }

    /**
     * Get the appropriate response for the given message
     * 
     * @param message
     *            The composite message
     */
    public Response getResponse(ConnectorMessage sourceMessage, Message message) {
        if (respondFromName.equals(Constants.RESPONSE_AUTO_BEFORE)) {
            // Assume a successful status since we're responding before the message has been processed
            return dataType.getAutoResponder().getResponse(Status.SENT, sourceMessage.getRaw().getContent(), sourceMessage);
        } else if (respondFromName.equals(Constants.RESPONSE_SOURCE_TRANSFORMED)) {
            // Use the status and content from the source connector message
            return dataType.getAutoResponder().getResponse(sourceMessage.getStatus(), sourceMessage.getRaw().getContent(), sourceMessage);
        } else if (respondFromName.equals(Constants.RESPONSE_DESTINATIONS_COMPLETED)) {
            // Determine the status based on the destination statuses
            Status status = Status.SENT;

            if (message.getConnectorMessages().size() - 1 < numDestinations) {
                status = Status.ERROR;
            }

            Integer highestPrecedence = null;

            for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
                if (connectorMessage.getMetaDataId() > 0) {
                    Integer precedence = statusPrecedenceMap.get(connectorMessage.getStatus());

                    if (precedence != null && (highestPrecedence == null || precedence > highestPrecedence)) {
                        status = connectorMessage.getStatus();
                        highestPrecedence = precedence;
                    }
                }
            }

            return dataType.getAutoResponder().getResponse(status, sourceMessage.getRaw().getContent(), message.getMergedConnectorMessage());
        } else if (respondFromName != null) {
            // Get the appropriate response from the response map (includes the post-processor variable)
            return message.getMergedConnectorMessage().getResponseMap().get(respondFromName);
        }

        return null;
    }
}
