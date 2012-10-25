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

    private int numDestinations;
    private String respondFromName;

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
        return (respondFromName != null);
    }

    /**
     * Get the appropriate response for the given message
     * 
     * @param message
     *            The composite message
     */
    public Response getResponse(Message message) {
        if (respondFromName == Constants.RESPONSE_SOURCE_TRANSFORMED) {
            return new Response(message.getConnectorMessages().get(0).getStatus(), null);
        } else if (respondFromName == Constants.RESPONSE_DESTINATIONS_COMPLETED) {
            if (message.getConnectorMessages().size() - 1 < numDestinations) {
                return new Response(Status.ERROR, null);
            }

            Response response = new Response();
            Integer highestPrecedence = null;

            for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
                if (connectorMessage.getMetaDataId() > 0) {
                    Integer precedence = statusPrecedenceMap.get(connectorMessage.getStatus());

                    if (precedence != null && (highestPrecedence == null || precedence > highestPrecedence)) {
                        response.setStatus(connectorMessage.getStatus());
                        highestPrecedence = precedence;
                    }
                }
            }

            return response;
        } else if (respondFromName != null) {
            // if the message did finish processing, get the appropriate response
            return message.getConnectorMessages().get(0).getResponseMap().get(respondFromName);
        } else {
            return null;
        }
    }
}
