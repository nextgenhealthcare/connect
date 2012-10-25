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
import java.util.List;

public class EventList extends ArrayList<Event> implements EventListener {
    private List<Event> events = new ArrayList<Event>();

    @Override
    public void readEvent(Event event) {
        add(event);
    }
}
