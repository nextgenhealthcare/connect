package com.mirth.connect.donkey.server.channel;

public interface ChannelProcessLock {
    public void acquire() throws InterruptedException;
    
    public void release();
    
    public void reset();
}
