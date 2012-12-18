/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.channel;

public enum ChannelState {
    STARTING('T'), STARTED('S'), PAUSING('A'), PAUSED('P'), STOPPING('O'), STOPPED('D');

    private char state;

    private ChannelState(char state) {
        this.state = state;
    }

    public char getStatusCode() {
        return state;
    }

    public static ChannelState fromChar(char state) {
        if (state == 'T')
            return STARTING;
        if (state == 'S')
            return STARTED;
        if (state == 'A')
            return PAUSING;
        if (state == 'P')
            return PAUSED;
        if (state == 'O')
            return STOPPING;
        if (state == 'D')
            return STOPPED;

        return null;
    }
}
