/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

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
