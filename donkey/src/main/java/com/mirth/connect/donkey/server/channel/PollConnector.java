/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import com.mirth.connect.donkey.model.channel.PollConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollConnectorPropertiesInterface;
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;

public abstract class PollConnector extends SourceConnector {
    private Timer timer;
    private PollConnectorTask task;
    private AtomicBoolean terminated = new AtomicBoolean(true);

    @Override
    public void start() throws StartException {
        super.start();

        terminated.set(false);

        timer = new Timer();
        scheduleTask();
    }

    @Override
    public void stop() throws StopException {
        terminated.set(true);

        if (task != null) {
            try {
                task.terminate();
            } catch (InterruptedException e) {
                throw new StopException(e);
            }
            task = null;
        }
        super.stop();
    }

    @Override
    public void halt() throws HaltException {
        terminated.set(true);
        if (task != null) {
            // Interrupt the poll thread
            task.interrupt();
        }
        super.halt();

        if (task != null) {
            try {
                task.terminate();
            } catch (InterruptedException e) {
                throw new HaltException(e);
            }
            task = null;
        }
    }

    public boolean isTerminated() {
        return terminated.get();
    }

    protected abstract void poll() throws InterruptedException;

    public void scheduleTask() {
        boolean firstTime = (task == null);

        task = new PollConnectorTask(this);

        PollConnectorProperties connectorProperties = ((PollConnectorPropertiesInterface) getConnectorProperties()).getPollConnectorProperties();

        if (connectorProperties.getPollingType().equals(PollConnectorProperties.POLLING_TYPE_INTERVAL)) {
            if (firstTime) {
                timer.schedule(task, 0);
            } else {
                timer.schedule(task, connectorProperties.getPollingFrequency());
            }
        } else if (connectorProperties.getPollingType().equals(PollConnectorProperties.POLLING_TYPE_TIME)) {
            Calendar triggerTime = Calendar.getInstance();

            /*
             * Save the current time to check against the trigger time later. We MUST check against
             * the original time the calendar object was created since it is used to set the
             * day/month/year. Otherwise it is possible to incorrectly advance the day of month.
             */
            long currentTime = triggerTime.getTimeInMillis();

            triggerTime.set(Calendar.HOUR_OF_DAY, connectorProperties.getPollingHour());
            triggerTime.set(Calendar.MINUTE, connectorProperties.getPollingMinute());
            triggerTime.set(Calendar.SECOND, 0);
            triggerTime.set(Calendar.MILLISECOND, 0);

            /*
             * If the scheduled time is in the past, set it to execute the following day. This works
             * correctly even at the end of the month.
             */
            if (triggerTime.getTimeInMillis() < currentTime) {
                triggerTime.set(Calendar.DAY_OF_MONTH, triggerTime.get(Calendar.DAY_OF_MONTH) + 1);
            }

            /*
             * Schedule the task at the specified time. If the specified time was crossed between
             * creating the triggerTime and scheduling it, it will be executed immediately.
             */
            timer.schedule(task, triggerTime.getTime());
        }
    }

    private class PollConnectorTask extends TimerTask {
        private PollConnector pollConnector;
        private Thread thread;
        private ReentrantLock lock;

        public PollConnectorTask(PollConnector pollConnector) {
            this.pollConnector = pollConnector;
            lock = new ReentrantLock();
        }

        @Override
        public void run() {
            lock.lock();
            try {
                if (!isTerminated()) {
                    thread = Thread.currentThread();

                    try {
                        pollConnector.poll();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    if (!isTerminated()) {
                        pollConnector.scheduleTask();
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        public void terminate() throws InterruptedException {
            lock.lockInterruptibly();
            try {
                timer.cancel();
                timer.purge();
            } finally {
                lock.unlock();
            }
        }

        public void interrupt() {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
        }
    }
}
