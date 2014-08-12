package com.mirth.connect.donkey.server.channel;

import java.util.concurrent.locks.ReentrantLock;

public class DefaultChannelProcessLock implements ChannelProcessLock {
    private ReentrantLock lock = new ReentrantLock(true);

    @Override
    public void acquire() throws InterruptedException {
        lock.lockInterruptibly();
    }

    @Override
    public void release() {
        lock.unlock();
    }

    @Override
    public void reset() {
        lock = new ReentrantLock(true);
    }
}
