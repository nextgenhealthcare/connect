/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mirth.connect.donkey.model.channel.PollConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollConnectorPropertiesInterface;
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;

public abstract class PollConnector extends SourceConnector {
    private Timer timer;
    private PollConnectorTask task;
    private AtomicBoolean terminated;

    @Override
    public void start() throws StartException {
        super.start();

        terminated = new AtomicBoolean(false);

        PollConnectorProperties connectorProperties = ((PollConnectorPropertiesInterface) getConnectorProperties()).getPollConnectorProperties();

        timer = new Timer();

        if (connectorProperties.getPollingType().equals(PollConnectorProperties.POLLING_TYPE_INTERVAL)) {
            // if polling type is interval, schedule the polling task to execute at the given frequency in milliseconds
            task = new PollConnectorTask(this, false);
            timer.schedule(task, 0, connectorProperties.getPollingFrequency());
        } else if (connectorProperties.getPollingType().equals(PollConnectorProperties.POLLING_TYPE_TIME)) {
            task = new PollConnectorTask(this, true);
            // if polling type is time, schedule the polling task to execute at the given time, repeating every 24 hours
            scheduleTimeBasedTask();
        }
    }

    @Override
    public void stop() throws StopException {
        terminated.set(true);
        //TODO Possible nullpointerexception if channel start fails
        if (task != null) {
            task.terminate(false);
        }
        super.stop();
    }

    @Override
    public void halt() throws HaltException {
        terminated.set(true);
        //TODO Possible nullpointerexception if halted before the first start task was ever completed.
        if (task != null) {
            task.terminate(true);
        }
        super.halt();
    }

    public boolean isTerminated() {
        return terminated.get();
    }

    protected abstract void poll() throws InterruptedException;

    public void scheduleTimeBasedTask() {
        PollConnectorProperties connectorProperties = ((PollConnectorPropertiesInterface) getConnectorProperties()).getPollConnectorProperties();

        Calendar triggerTime = Calendar.getInstance();
        triggerTime.setTimeInMillis(System.currentTimeMillis());
        triggerTime.set(Calendar.HOUR, connectorProperties.getPollingHour());
        triggerTime.set(Calendar.MINUTE, connectorProperties.getPollingMinute());
        triggerTime.set(Calendar.SECOND, 0);
        triggerTime.set(Calendar.MILLISECOND, 0);

        // if the scheduled time is in the past, set it to execute tomorrow
        // 10000 milliseconds are added to allow sufficient time to schedule the task
        if (triggerTime.getTimeInMillis() <= (System.currentTimeMillis() + 10000)) {
            triggerTime.set(Calendar.DAY_OF_MONTH, triggerTime.get(Calendar.DAY_OF_MONTH) + 1);
        }

        timer.schedule(new PollConnectorTask(this, true), triggerTime.getTime());
    }

    private class PollConnectorTask extends TimerTask {
        private PollConnector pollConnector;
        private boolean reschedule;
        private Thread thread;

        public PollConnectorTask(PollConnector pollConnector, boolean reschedule) {
            this.pollConnector = pollConnector;
            this.reschedule = reschedule;
        }

        @Override
        public void run() {
            synchronized (this) {
                if (!isTerminated()) {
                    thread = Thread.currentThread();

                    try {
                        pollConnector.poll();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    if (reschedule && !isTerminated()) {
                        pollConnector.scheduleTimeBasedTask();
                    }
                }
            }
        }

        public void terminate(boolean interrupt) {
            if (interrupt && thread != null && thread.isAlive()) {
                thread.interrupt();
            }

            synchronized (this) {
                timer.cancel();
                timer.purge();
            }
        }
    }
}
