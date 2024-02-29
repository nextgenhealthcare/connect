/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.quartz.CronScheduleBuilder;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TimeOfDay;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.calendar.BaseCalendar;
import org.quartz.impl.calendar.DailyCalendar;
import org.quartz.impl.calendar.MonthlyCalendar;
import org.quartz.impl.calendar.WeeklyCalendar;

import com.mirth.connect.donkey.model.channel.CronProperty;
import com.mirth.connect.donkey.model.channel.PollConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollConnectorPropertiesAdvanced;

public class TriggerFactory {
    private PollConnectorProperties pollConnectorProperties;
    private PollConnectorPropertiesAdvanced advancedProperties;
    private String id;

    private JobDetail job;
    private Trigger dailyIntervalTrigger;

    private BaseCalendar baseCalendar;
    private BaseCalendar finalTriggerCalendar;

    public TriggerFactory(PollConnectorProperties pollConnectorProperties, String id) {
        this(null, pollConnectorProperties, id);
    }

    public TriggerFactory(JobDetail job, PollConnectorProperties pollConnectorProperties, String id) {
        this.id = id;
        this.job = job;
        this.pollConnectorProperties = pollConnectorProperties;
        advancedProperties = pollConnectorProperties.getPollConnectorPropertiesAdvanced();

        baseCalendar = new BaseCalendar();
        if (advancedProperties.isWeekly()) { // false means to include the day
            baseCalendar = new WeeklyCalendar();
            ((WeeklyCalendar) baseCalendar).setDaysExcluded(advancedProperties.getInactiveDays());
        } else {
            boolean[] days = new boolean[32];
            Arrays.fill(days, true);
            days[advancedProperties.getDayOfMonth() - 1] = false;

            baseCalendar = new MonthlyCalendar();
            ((MonthlyCalendar) baseCalendar).setDaysExcluded(days);
        }
        finalTriggerCalendar = baseCalendar;
    }

    public Trigger createDailyInterval() {
        boolean invertTime = true;
        boolean allDay = advancedProperties.isAllDay();

        int startingHour = allDay ? 0 : advancedProperties.getStartingHour();
        int startingMinute = allDay ? 0 : advancedProperties.getStartingMinute();
        int endingHour = allDay ? 23 : advancedProperties.getEndingHour();
        int endingMinute = allDay ? 59 : advancedProperties.getEndingMinute();

        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, startingHour);
        startTime.set(Calendar.MINUTE, startingMinute);
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);

        if (advancedProperties.getStartingHour() > endingHour) { // create inverted range to exclude triggers during this time
            endingHour = startingHour;
            endingMinute = startingMinute;
            startingHour = advancedProperties.getEndingHour();
            startingMinute = advancedProperties.getEndingMinute();

            invertTime = false;
            startTime.set(Calendar.HOUR_OF_DAY, 0);
            startTime.set(Calendar.MINUTE, 0);
        }

        finalTriggerCalendar = new DailyCalendar(baseCalendar, startingHour, startingMinute, 0, 0, endingHour, endingMinute, 0, 0);
        ((DailyCalendar) finalTriggerCalendar).setInvertTimeRange(invertTime); // false means to exclude triggers in time range

        SimpleScheduleBuilder schedule = SimpleScheduleBuilder.simpleSchedule();
        schedule.withIntervalInMilliseconds(pollConnectorProperties.getPollingFrequency());
        schedule.withMisfireHandlingInstructionNextWithExistingCount().withRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);

        TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger();
        triggerBuilder.forJob(job).withIdentity("PollingJobTrigger", id);
        triggerBuilder.withDescription("Daily interval trigger");
        triggerBuilder.withSchedule(schedule);

        dailyIntervalTrigger = triggerBuilder.modifiedByCalendar("Calendar").startAt(startTime.getTime()).build();

        return dailyIntervalTrigger;
    }

    public Trigger createTimeTrigger() {
        DailyTimeIntervalScheduleBuilder dailySchedule = DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule();
        dailySchedule.withInterval(24, IntervalUnit.HOUR);
        dailySchedule.withMisfireHandlingInstructionDoNothing().withRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        dailySchedule.startingDailyAt(new TimeOfDay(pollConnectorProperties.getPollingHour(), pollConnectorProperties.getPollingMinute()));

        TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger();
        triggerBuilder.forJob(job).withIdentity("PollingJobTrigger", id);
        triggerBuilder.withDescription("Daily time trigger");
        triggerBuilder.withSchedule(dailySchedule);
        dailyIntervalTrigger = triggerBuilder.modifiedByCalendar("Calendar").build();

        return dailyIntervalTrigger;
    }

    public List<Trigger> createCronTriggers() {
        List<Trigger> triggerList = new ArrayList<Trigger>();

        for (CronProperty property : pollConnectorProperties.getCronJobs()) {
            TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger();
            triggerBuilder.forJob(job).withIdentity("CronTrigger#" + triggerList.size(), id);
            triggerBuilder.withDescription(property.getDescription());

            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(property.getExpression()).withMisfireHandlingInstructionDoNothing());
            triggerList.add(triggerBuilder.build());
        }

        return triggerList;
    }

    public BaseCalendar getCalendar() {
        return finalTriggerCalendar;
    }
}