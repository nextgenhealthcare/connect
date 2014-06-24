package com.mirth.connect.donkey.server.channel;

import java.util.concurrent.Semaphore;

public class DefaultChannelProcessLock implements ChannelProcessLock {
    private Semaphore semaphore = new Semaphore(1, true);

    @Override
    public void acquire() throws InterruptedException {
        semaphore.acquire();
    }

    @Override
    public void release() {
        semaphore.release();
    }

    @Override
    public void reset() {
        semaphore = new Semaphore(1, true);
    }
}
