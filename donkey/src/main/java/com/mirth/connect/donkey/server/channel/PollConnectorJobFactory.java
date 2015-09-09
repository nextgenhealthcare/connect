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

import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

public class PollConnectorJobFactory implements JobFactory {
    private PollConnector pollConnector;
    private ReentrantLock lock;

    public PollConnectorJobFactory(PollConnector pollConnector) {
        lock = new ReentrantLock();
        this.pollConnector = pollConnector;
    }

    @Override
    public Job newJob(TriggerFiredBundle triggerFiredBundle, Scheduler scheduler) throws SchedulerException {
        return new PollConnectorJob(pollConnector, lock);
    }
}