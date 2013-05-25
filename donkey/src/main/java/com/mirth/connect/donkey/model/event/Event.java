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
