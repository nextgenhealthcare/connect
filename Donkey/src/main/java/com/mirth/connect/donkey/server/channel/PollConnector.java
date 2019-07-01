/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.concurrent.atomic.AtomicBoolean;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import com.mirth.connect.donkey.model.channel.PollConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollConnectorPropertiesInterface;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.util.PollConnectorJobHandler;

public abstract class PollConnector extends SourceConnector {
    private PollConnectorJobHandler handler;
    private AtomicBoolean terminated = new AtomicBoolean(true);

    private JobDetail job;
    private Scheduler scheduler;

    @Override
    public void start() throws ConnectorTaskException, InterruptedException {
        super.start();

        terminated.set(false);
        PollConnectorProperties pollConnectorProperties = ((PollConnectorPropertiesInterface) getConnectorProperties()).getPollConnectorProperties();
        handler = new PollConnectorJobHandler(pollConnectorProperties, getChannelId(), true);

        try {
            handler.configureJob(PollConnectorJob.class, new PollConnectorJobFactory(this), "PollConnector");
            handler.scheduleJob(true);

            job = handler.getJob();
            scheduler = handler.getScheduler();
        } catch (Exception e) {
            throw new ConnectorTaskException(e);
        }
    }

    @Override
    public void stop() throws ConnectorTaskException, InterruptedException {
        terminated.set(true);

        if (scheduler != null) {
            try {
                scheduler.shutdown(true);
            } catch (SchedulerException e) {
                throw new ConnectorTaskException(e);
            }
        }

        super.stop();
    }

    @Override
    public void halt() throws ConnectorTaskException, InterruptedException {
        terminated.set(true);

        if (scheduler != null) {
            try {
                scheduler.interrupt(job.getKey());
            } catch (Exception e) {
                throw new ConnectorTaskException(e);
            }
        }

        super.halt();
    }

    public boolean isTerminated() {
        return terminated.get();
    }

    protected abstract void poll() throws InterruptedException;
}