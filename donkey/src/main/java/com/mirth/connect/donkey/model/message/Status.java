/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

/**
 * Denotes the status of a connector message or response. Available statuses:
 * 
 * RECEIVED, FILTERED, TRANSFORMED, SENT, QUEUED, ERROR, PENDING
 */
public enum Status {
    RECEIVED('R'), FILTERED('F'), TRANSFORMED('T'), SENT('S'), QUEUED('Q'), ERROR(
            'E'), PENDING('P');

    private char status;

    private Status(char status) {
        this.status = status;
    }

    /**
     * Returns the character code of this status.
     */
    public char getStatusCode() {
        return status;
    }

    /**
     * Returns true if this status is SENT or FILTERED. Used to determine
     * whether to remove content upon completion of a message.
     */
    public boolean isCompleted() {
        return (status == SENT.getStatusCode() || status == FILTERED.getStatusCode());
    }

    /**
     * Converts a character code into the appropriate status.
     * 
     * @param status
     *            The character code to convert.
     * @return The associated Status instance, or null if none exists.
     */
    public static Status fromChar(char status) {
        // @formatter:off
        if (status == 'R') return RECEIVED;
        if (status == 'F') return FILTERED;
        if (status == 'T') return TRANSFORMED;
        if (status == 'S') return SENT;
        if (status == 'Q') return QUEUED;
        if (status == 'E') return ERROR;
        if (status == 'P') return PENDING;
        // @formatter:on

        return null;
    }
}
