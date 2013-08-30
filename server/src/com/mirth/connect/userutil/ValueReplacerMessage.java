/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.userutil;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mirth.connect.donkey.model.message.ImmutableMessage;

/**
 * This class represents an overall message and is used to retrieve details such
 * as the message ID, specific connector messages, or the merged connector
 * message.
 */
public class ValueReplacerMessage {
    private ImmutableMessage message;

    /**
     * Instantiates an ValueReplacerMessage object.
     * 
     * @param message
     *            - The ImmutableMessage object that this object will reference
     *            for retrieving data.
     */
    public ValueReplacerMessage(ImmutableMessage message) {
        this.message = message;
    }

    /**
     * Returns the sequential ID of this message, as a Long.
     */
    public Long getMessageId() {
        return message.getMessageId();
    }

    /**
     * Returns the ID of the server associated with this message.
     */
    public String getServerId() {
        return message.getServerId();
    }

    /**
     * Returns the ID of the channel associated with this message.
     */
    public String getChannelId() {
        return message.getChannelId();
    }

    /**
     * Returns the original date/time that this message was created by the
     * channel. If the message is reprocessed at a later point, this date will
     * remain the same and instead the connector message received dates will be
     * updated.
     */
    public Calendar getReceivedDate() {
        return message.getReceivedDate();
    }

    /**
     * Returns whether this message has finished processing through a channel. A
     * message is considered "processed" if it correctly flows through each
     * applicable connector and the postprocessor script finishes. Even if a
     * non-fatal error occurs on a particular connector message and the status
     * ends up as ERROR, or if a message is queued by a destination and has not
     * yet been sent to the outbound system, it can still be considered
     * processed.
     */
    public boolean isProcessed() {
        return message.isProcessed();
    }

    /**
     * Returns a map of connector messages associated with this message. The
     * keys are the metadata IDs (as Integer objects), and the values are the
     * connector messages themselves.
     */
    public Map<Integer, ValueReplacerConnectorMessage> getConnectorMessages() {
        Map<Integer, ValueReplacerConnectorMessage> map = new LinkedHashMap<Integer, ValueReplacerConnectorMessage>();

        for (Integer key : message.getConnectorMessages().keySet()) {
            map.put(key, new ValueReplacerConnectorMessage(message.getConnectorMessages().get(key)));
        }

        return map;
    }

    @Override
    public String toString() {
        return message.toString();
    }
}
