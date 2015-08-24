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