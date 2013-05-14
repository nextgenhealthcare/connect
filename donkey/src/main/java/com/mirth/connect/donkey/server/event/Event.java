package com.mirth.connect.donkey.server.event;

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
