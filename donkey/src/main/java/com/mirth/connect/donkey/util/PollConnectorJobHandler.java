/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.quartz.CronExpression;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.calendar.BaseCalendar;
import org.quartz.spi.JobFactory;

import com.mirth.connect.donkey.model.channel.PollConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollingType;

public class PollConnectorJobHandler {
    private PollConnectorProperties pollConnectorProperties;
    private TriggerFactory triggerFactory;
    private String id;
    private boolean initialTriggerFired;
    private boolean isPollConnector;

    private JobDetail job;
    private Scheduler scheduler;

    private List<Trigger> triggerList;
    private BaseCalendar calendar;

    public PollConnectorJobHandler(PollConnectorProperties pollConnectorProperties, String id, boolean isPollConnector) {
        this.pollConnectorProperties = pollConnectorProperties;
        this.id = id;
        this.isPollConnector = isPollConnector;
        initialTriggerFired = false;
    }

    public void configureJob(Class className, JobFactory jobFactory, String identity) throws SchedulerException {
        JobBuilder jobBuilder = JobBuilder.newJob(className != null ? className : DummyJob.class).withIdentity(identity + id, id);
        jobBuilder.storeDurably(true);
        job = jobBuilder.build();

        createTriggers();

        if (className != null) { // Only create scheduler if using a valid quartz job
            Properties schedulerProperties = new Properties();
            schedulerProperties.setProperty("org.quartz.scheduler.instanceName", id);
            schedulerProperties.setProperty("org.quartz.threadPool.threadCount", String.valueOf(triggerList.size()));

            StdSchedulerFactory factory = new StdSchedulerFactory();
            factory.initialize(schedulerProperties);

            scheduler = factory.getScheduler();
            if (jobFactory != null) {
                scheduler.setJobFactory(jobFactory);
            }

            if (calendar != null) {
                scheduler.addCalendar("Calendar", calendar, true, true);
            }
        }
    }

    private void createTriggers() throws SchedulerException {
        triggerList = new ArrayList<Trigger>();
        triggerFactory = new TriggerFactory(job, pollConnectorProperties, id);
        PollingType pollingType = pollConnectorProperties.getPollingType();
        if (pollingType.equals(PollingType.CRON)) {
            triggerList.addAll(triggerFactory.createCronTriggers());
        } else {
            if (pollingType.equals(PollingType.INTERVAL)) {
                triggerList.add(triggerFactory.createDailyInterval());
            } else if (pollingType.equals(PollingType.TIME)) {
                triggerList.add(triggerFactory.createTimeTrigger());
            }

            calendar = triggerFactory.getCalendar();
        }
    }

    public void scheduleJob(boolean start) throws SchedulerException {
        if (!triggerList.isEmpty()) {
            scheduler.addJob(job, false);

            for (Trigger trigger : triggerList) {
                scheduler.scheduleJob(trigger);
            }

            if (isPollConnector && pollConnectorProperties.isPollOnStart() && !initialTriggerFired) {
                scheduler.triggerJob(job.getKey());
                initialTriggerFired = true;
            }

            if (start) {
                scheduler.start();
            }
        }

    }

    public JobDetail getJob() {
        return job;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public String getNextFireTime() {
        String time = "";
        Date earliestTriggerTime = null;
        Calendar currentTime = Calendar.getInstance();
        try {
            for (Trigger trigger : triggerList) {
                Date nextFireTime = trigger.getFireTimeAfter(currentTime.getTime());
                if (earliestTriggerTime == null || nextFireTime != null && nextFireTime.before(earliestTriggerTime)) {
                    nextFireTime = nextFireTime == null ? trigger.getNextFireTime() : nextFireTime;
                    earliestTriggerTime = nextFireTime;
                }
            }

            PollingType pollingType = pollConnectorProperties.getPollingType();
            if (pollingType != PollingType.CRON) {
                boolean isTimeIncluded = calendar.isTimeIncluded(earliestTriggerTime.getTime());
                if (!isTimeIncluded) {
                    if (pollingType == PollingType.TIME) {
                        for (int count = 0; !isTimeIncluded && count < 60; count++) {
                            earliestTriggerTime = triggerList.get(0).getFireTimeAfter(earliestTriggerTime);
                            isTimeIncluded = calendar.isTimeIncluded(earliestTriggerTime.getTime());
                        }
                    } else {
                        Calendar includedTime = Calendar.getInstance();
                        includedTime.setTimeInMillis(calendar.getNextIncludedTime(currentTime.getTimeInMillis()));
                        earliestTriggerTime = includedTime.getTime();
                    }
                }
            }

            if (earliestTriggerTime != null) {
                time = new SimpleDateFormat("EEEE, MMM d, h:mm:ss a").format(earliestTriggerTime);
            }
        } catch (Exception e) {
            time = "Invalid Schedule.";
        }

        return time;
    }

    public static boolean validateExpression(String expression) {
        return CronExpression.isValidExpression(expression);
    }
}