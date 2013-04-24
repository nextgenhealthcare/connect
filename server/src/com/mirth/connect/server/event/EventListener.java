package com.mirth.connect.server.event;

import java.util.concurrent.BlockingQueue;

import com.mirth.connect.donkey.server.event.Event;
import com.mirth.connect.server.util.UUIDGenerator;

public abstract class EventListener {
    
    private String key = UUIDGenerator.getUUID();

    public String getKey() {
        return key;
    }
    
    public abstract BlockingQueue<Event> getQueue();
}
