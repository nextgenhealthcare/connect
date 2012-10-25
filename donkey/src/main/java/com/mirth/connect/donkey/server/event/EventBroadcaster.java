/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.mirth.connect.donkey.model.message.Status;

public class EventBroadcaster {
    private static EventBroadcaster instance;
    private List<EventListener> listeners;

    public static EventBroadcaster getInstance() {
        synchronized (EventBroadcaster.class) {
            if (instance == null) {
                instance = new EventBroadcaster();
            }

            return instance;
        }
    }

    private EventBroadcaster() {
        listeners = new ArrayList<EventListener>();
    }

    public void addListener(EventListener listener) {
        listeners.add(listener);
    }

    public void submitEvent(Event event) {
        event.setEventDate(Calendar.getInstance());

        // Ignore the old status if it's RECEIVED, or if either the old or new status is QUEUED and queuing is disabled
        if (event.getOldMessageStatus() == Status.RECEIVED || ((event.getMessageStatus() == Status.QUEUED || event.getOldMessageStatus() == Status.QUEUED) && !event.isQueueEnabled())) {
            event.setOldMessageStatus(null);
        }

        for (EventListener eventListener : listeners) {
            eventListener.readEvent(event);
        }
    }
}
