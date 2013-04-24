package com.mirth.connect.donkey.server.event;

public abstract class Event {
    
    public Event() {
        date = System.nanoTime();
    }
    
    private long date;

    public long getDate() {
        return date;
    }

}
