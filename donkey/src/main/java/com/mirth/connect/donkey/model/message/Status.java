/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

public enum Status {
    RECEIVED('R'), FILTERED('F'), TRANSFORMED('T'), SENT('S'), QUEUED('Q'), ERROR('E'), PENDING('P');

    private char status;

    private Status(char status) {
        this.status = status;
    }

    public char getStatusCode() {
        return status;
    }

    public boolean isCompleted() {
        return (status == SENT.getStatusCode() || status == FILTERED.getStatusCode());
    }

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
