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

import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

public class PollConnectorJob implements InterruptableJob {
    private PollConnector pollConnector;
    private Thread thread;
    private ReentrantLock lock;

    public PollConnectorJob(PollConnector pollConnector, ReentrantLock lock) {
        this.lock = lock;
        this.pollConnector = pollConnector;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        boolean locked = false;
        if (!lock.isLocked()) {
            synchronized (lock) {
                if (!lock.isLocked()) {
                    lock.lock();
                    locked = true;
                }
            }
        }

        if (locked) {
            try {
                if (!pollConnector.isTerminated()) {
                    thread = Thread.currentThread();
                    String originalThreadName = thread.getName();

                    try {
                        thread.setName(pollConnector.getConnectorProperties().getName() + " Polling Thread on " + pollConnector.getChannel().getName() + " (" + pollConnector.getChannelId() + ") < " + originalThreadName);
                        pollConnector.poll();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        thread.setName(originalThreadName);
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }
}