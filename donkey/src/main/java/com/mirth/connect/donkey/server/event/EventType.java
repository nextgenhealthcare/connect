/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.event;

public enum EventType {
    // @formatter:off
    
    // add events here
//    EXAMPLE_EVENT(1)
    ;
    
    // @formatter:on

    private int eventCode;

    private EventType(int eventCode) {
        this.eventCode = eventCode;
    }

    public int getEventCode() {
        return eventCode;
    }

    public static EventType fromEventCode(int eventCode) {
        // @formatter:off
        switch (eventCode) {
//            case 1: return EXAMPLE_EVENT;
        }
        // @formatter:on

        return null;
    }
}
