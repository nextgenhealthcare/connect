/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.event;

public abstract class Event {
    private long dateTime;
    private long nanoTime;

    public Event() {
        dateTime = System.currentTimeMillis();
        nanoTime = System.nanoTime();
    }

    public long getDateTime() {
        return dateTime;
    }

    public long getNanoTime() {
        return nanoTime;
    }
}
