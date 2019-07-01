/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.concurrent.Semaphore;

public class DefaultChannelProcessLock implements ChannelProcessLock {
    private int permits;
    private Semaphore lock;

    public DefaultChannelProcessLock(int permits) {
        if (permits < 1) {
            permits = 1;
        }
        this.permits = permits;
        reset();
    }

    @Override
    public void acquire() throws InterruptedException {
        lock.acquire();
    }

    @Override
    public void acquireAll() throws InterruptedException {
        lock.acquire(permits);
    }

    @Override
    public void release() {
        lock.release();
    }

    @Override
    public void releaseAll() {
        lock.release(permits);
    }

    @Override
    public void reset() {
        lock = new Semaphore(permits, true);
    }
}