/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.server.event.ChannelEvent;
import com.mirth.connect.donkey.server.event.ConnectorEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.donkey.server.event.Event;
import com.mirth.connect.donkey.server.event.EventType;
import com.mirth.connect.donkey.server.event.MessageEvent;
import com.mirth.connect.server.event.EventListener;

public class DefaultEventController extends EventController {
    private Logger logger = Logger.getLogger(this.getClass());

    private static DefaultEventController instance = null;

    private static Map<String, BlockingQueue<Event>> errorEventQueues = new ConcurrentHashMap<String, BlockingQueue<Event>>();
    private static Map<String, BlockingQueue<Event>> messageEventQueues = new ConcurrentHashMap<String, BlockingQueue<Event>>();
    private static Map<String, BlockingQueue<Event>> channelEventQueues = new ConcurrentHashMap<String, BlockingQueue<Event>>();
    private static Map<String, BlockingQueue<Event>> connectorEventQueues = new ConcurrentHashMap<String, BlockingQueue<Event>>();

    private DefaultEventController() {
    }

    public static EventController create() {
        synchronized (DefaultEventController.class) {
            if (instance == null) {
                instance = new DefaultEventController();
            }

            return instance;
        }
    }
    
    @Override
    public String addListener(EventListener listener, Set<EventType> types) {
        String key = listener.getKey();
        BlockingQueue<Event> queue = listener.getQueue();
        
        if (types.contains(EventType.ERROR)) {
            errorEventQueues.put(key, queue);
        }

        if (types.contains(EventType.MESSAGE)) {
            messageEventQueues.put(key, queue);
        }

        if (types.contains(EventType.CHANNEL)) {
            channelEventQueues.put(key, queue);
        }

        if (types.contains(EventType.CONNECTOR)) {
            connectorEventQueues.put(key, queue);
        }
        
        return key;
    }

    @Override
    public void removeListener(EventListener listener) {
        String key = listener.getKey();
        
        errorEventQueues.remove(key);
        messageEventQueues.remove(key);
        channelEventQueues.remove(key);
        connectorEventQueues.remove(key);
    }

    @Override
    public void dispatchEvent(Event event) {
        try {
            Map<String, BlockingQueue<Event>> queues = null;
            /*
             * Using instanceof is several thousand times faster than using a map to store the
             * different queue sets.
             */
            if (event instanceof ErrorEvent) {
                queues = errorEventQueues;
            } else if (event instanceof MessageEvent) {
                queues = messageEventQueues;
            } else if (event instanceof ChannelEvent) {
                queues = channelEventQueues;
            } else if (event instanceof ConnectorEvent) {
                queues = connectorEventQueues;
            }

            if (queues != null) {
                for (BlockingQueue<Event> queue : queues.values()) {
                    queue.put(event);
                }
            }
        } catch (InterruptedException e) {

        }
    }

}
