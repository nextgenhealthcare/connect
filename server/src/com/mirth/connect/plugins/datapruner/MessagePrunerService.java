/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.messagepruner;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute;
import static org.quartz.CronScheduleBuilder.monthlyOnDayAndHourAndMinute;
import static org.quartz.CronScheduleBuilder.weeklyOnDayAndHourAndMinute;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.text.DateFormatter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

import com.mirth.connect.client.core.Operations;
import com.mirth.connect.client.core.TaskConstants;
import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.ServicePlugin;
import com.mirth.connect.util.PropertyLoader;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class MessagePrunerService implements ServicePlugin {
    public static final String PLUGINPOINT = "Message Pruner";
    private static final int DEFAULT_PRUNING_BLOCK_SIZE = 0;
    private static final String PRUNER_JOB_KEY = "prunerJob";
    private static final String PRUNER_TRIGGER_KEY = "prunerTrigger";
    private static final String DATE_FORMAT = "MM/dd/yyyy hh:mm aa";

    public static MessagePruner pruner = new MessagePruner();

    private Scheduler scheduler;
    private SchedulerFactory schedulerFactory;
    private ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
    private JobDetail jobDetail;
    private Logger logger = Logger.getLogger(this.getClass());

    @Override
    public String getPluginPointName() {
        return PLUGINPOINT;
    }

    @Override
    public void start() {
        try {
            scheduler.start();
        } catch (Exception e) {
            logger.error("could not start message pruner", e);
        }
    }

    @Override
    public void stop() {
        try {
            scheduler.shutdown();
        } catch (Exception e) {
            logger.error("could not exit message pruner", e);
        }
    }

    @Override
    public void init(Properties properties) {
        try {
            schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();

            applyPrunerSettings(properties);

            jobDetail = newJob(MessagePrunerJob.class).withIdentity(PRUNER_JOB_KEY).build();

            Trigger trigger = createTrigger(properties);

            if (trigger == null) {
                logger.debug("Message pruner disabled");
            } else {
                logger.debug("Scheduling message pruner job");
                scheduler.scheduleJob(jobDetail, trigger);
            }
        } catch (Exception e) {
            logger.error("error encountered in database pruner initialization", e);
        }
    }

    @Override
    public void update(Properties properties) {
        try {
            scheduler.deleteJob(new JobKey(PRUNER_JOB_KEY));
            applyPrunerSettings(properties);
            Trigger trigger = createTrigger(properties);

            if (trigger != null) {
                scheduler.scheduleJob(jobDetail, trigger);
                logger.debug("Scheduled job to " + new SimpleDateFormat(DATE_FORMAT).format(trigger.getNextFireTime()));
            }
        } catch (Exception e) {
            logger.error("could not reschedule the message pruner", e);
        }
    }
    
    @Override
    public Object invoke(String method, Object object, String sessionId) {
        if (method.equals("getStatus")) {
            return getStatusMap();
        } else if (method.equals("start")) {
            pruner.start();
            return pruner.getPrunerStatus().getStartTime();
        } else if (method.equals("stop")) {
            if (pruner.isRunning()) {
                try {
                    pruner.stop();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Stopped waiting for the message pruner to stop, due to a thread interruption.", e);
                }
            }
        }

        return null;
    }
    
    private Map<String, String> getStatusMap() {
        Map<String, String> statusMap = new HashMap<String, String>();
        StringBuilder stringBuilder = new StringBuilder();
        MessagePrunerStatus status = pruner.getPrunerStatus();
        
        if (pruner.isRunning()) {
            statusMap.put("isRunning", "true");
            
            if (status.isArchiving()) {
                stringBuilder.append("Archiving");
            } else if (status.isPruning()) {
                stringBuilder.append("Pruning");
            } else {
                stringBuilder.append("Processing");
            }
            
            if (status.getCurrentChannelName() != null) {
                stringBuilder.append(" channel \"" + status.getCurrentChannelName() + "\"");
                
                if (status.isArchiving()) {
                    int count = pruner.getMessageExporter().getNumExported();
                    stringBuilder.append(", " + count + " message" + ((count != 1) ? "s" : "") + " archived");
                }
                
                stringBuilder.append(", " + getElapsedTimeText(status.getTaskStartTime(), Calendar.getInstance()) + " elapsed");
            }
    
            statusMap.put("currentState", stringBuilder.toString());

            int processedCount = status.getProcessedChannelIds().size();
            int failedCount = status.getFailedChannelIds().size();
            int totalCount = status.getPendingChannelIds().size() + processedCount + failedCount;
    
            stringBuilder = new StringBuilder();
            stringBuilder.append("Initiated " + new SimpleDateFormat(DATE_FORMAT).format(status.getStartTime().getTime()));
            stringBuilder.append(", " + processedCount + " of " + totalCount + " channel" + (totalCount == 1 ? "" : "s") + " processed");
    
            if (failedCount > 0) {
                stringBuilder.append(", " + failedCount + " channel" + (failedCount == 1 ? "" : "s") + " failed");
            }
            
            stringBuilder.append(", " + getElapsedTimeText(status.getStartTime(), Calendar.getInstance()) + " elapsed");
    
            statusMap.put("currentProcess", stringBuilder.toString());
        } else {
            statusMap.put("isRunning", "false");
            statusMap.put("currentState", "Not running");
            statusMap.put("currentProcess", "-");
        }
        
        MessagePrunerStatus lastStatus = pruner.getLastPrunerStatus();
        
        if (lastStatus == null) {
            statusMap.put("lastProcess", "-");
        } else {
            stringBuilder = new StringBuilder();
        
            int processedCount = lastStatus.getProcessedChannelIds().size();
            int failedCount = lastStatus.getFailedChannelIds().size();
            int totalCount = lastStatus.getPendingChannelIds().size() + processedCount + failedCount;
            int skippedCount = totalCount - processedCount - failedCount;
            
            stringBuilder.append("Initiated " + new SimpleDateFormat(DATE_FORMAT).format(lastStatus.getStartTime().getTime()));
            stringBuilder.append(", " + processedCount + " of " + totalCount + " channel" + (totalCount == 1 ? "" : "s") + " processed");

            if (failedCount > 0) {
                stringBuilder.append(", " + failedCount + " channel" + (failedCount == 1 ? "" : "s") + " failed");
            }
            
            if (skippedCount > 0) {
                stringBuilder.append(", " + skippedCount + " channel" + (skippedCount == 1 ? "" : "s") + " skipped");
            }
            
            stringBuilder.append(", " + getElapsedTimeText(lastStatus.getStartTime(), lastStatus.getEndTime()) + " duration");
            
            statusMap.put("lastProcess", stringBuilder.toString());
        }
        
        statusMap.put("nextProcess", "Not scheduled");
        
        Trigger trigger = null;

        try {
            trigger = scheduler.getTrigger(new TriggerKey(PRUNER_TRIGGER_KEY));
        } catch (SchedulerException e) {
        }
        
        if (trigger != null) {
            Date nextFireTime = trigger.getNextFireTime();

            if (nextFireTime != null) {
                statusMap.put("nextProcess", "Scheduled " + new SimpleDateFormat(DATE_FORMAT).format(nextFireTime));
            }
        }

        return statusMap;
    }

    @Override
    public Properties getDefaultProperties() {
        Properties properties = new Properties();
        properties.put("interval", "disabled");
        properties.put("time", "12:00 AM");
        properties.put("pruningBlockSize", String.valueOf(DEFAULT_PRUNING_BLOCK_SIZE));
        properties.put("archiveEnabled", serializer.serialize(false));
        properties.put("includeAttachments", serializer.serialize(false));
        properties.put("archiverOptions", serializer.serialize(new MessageWriterOptions()));
        return properties;
    }

    @Override
    public ExtensionPermission[] getExtensionPermissions() {
        ExtensionPermission viewPermission = new ExtensionPermission(PLUGINPOINT, "View Settings", "Displays the Message Pruner settings.", new String[] { Operations.PLUGIN_PROPERTIES_GET.getName() }, new String[] { TaskConstants.SETTINGS_REFRESH });
        ExtensionPermission savePermission = new ExtensionPermission(PLUGINPOINT, "Save Settings", "Allows changing the Message Pruner settings.", new String[] { Operations.PLUGIN_PROPERTIES_SET.getName() }, new String[] { TaskConstants.SETTINGS_SAVE });

        return new ExtensionPermission[] { viewPermission, savePermission };
    }

    private void applyPrunerSettings(Properties properties) {
        if (StringUtils.isNotEmpty(properties.getProperty("pruningBlockSize"))) {
            pruner.setBlockSize(Integer.parseInt(properties.getProperty("pruningBlockSize")));
        } else {
            pruner.setBlockSize(DEFAULT_PRUNING_BLOCK_SIZE);
        }

        pruner.setArchiveEnabled(Boolean.parseBoolean(properties.getProperty("archiveEnabled", Boolean.FALSE.toString())));
//        boolean includeAttachments = Boolean.parseBoolean(properties.getProperty("includeAttachments", Boolean.FALSE.toString()));

        if (pruner.isArchiveEnabled()) {
            pruner.setArchiverOptions((MessageWriterOptions) serializer.fromXML(properties.getProperty("archiverOptions")));
        }
    }

    private Trigger createTrigger(Properties properties) throws ParseException {
        String interval = PropertyLoader.getProperty(properties, "interval");

        if (interval.equals("disabled")) {
            return null;
        }

        ScheduleBuilder<?> schedule = null;

        if (interval.equals("hourly")) {
            schedule = cronSchedule("0 0 * * * ?");
        } else {
            SimpleDateFormat timeDateFormat = new SimpleDateFormat("hh:mm aa");
            DateFormatter timeFormatter = new DateFormatter(timeDateFormat);

            String time = PropertyLoader.getProperty(properties, "time");
            Date timeDate = (Date) timeFormatter.stringToValue(time);
            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.setTime(timeDate);

            if (interval.equals("daily")) {
                schedule = dailyAtHourAndMinute(timeCalendar.get(Calendar.HOUR_OF_DAY), timeCalendar.get(Calendar.MINUTE));
            } else if (interval.equals("weekly")) {
                SimpleDateFormat dayDateFormat = new SimpleDateFormat("EEEEEEEE");
                DateFormatter dayFormatter = new DateFormatter(dayDateFormat);

                String dayOfWeek = PropertyLoader.getProperty(properties, "dayOfWeek");
                Date dayDate = (Date) dayFormatter.stringToValue(dayOfWeek);
                Calendar dayCalendar = Calendar.getInstance();
                dayCalendar.setTime(dayDate);

                schedule = weeklyOnDayAndHourAndMinute(dayCalendar.get(Calendar.DAY_OF_WEEK), timeCalendar.get(Calendar.HOUR_OF_DAY), timeCalendar.get(Calendar.MINUTE));
            } else if (interval.equals("monthly")) {
                SimpleDateFormat dayDateFormat = new SimpleDateFormat("DD");
                DateFormatter dayFormatter = new DateFormatter(dayDateFormat);

                String dayOfMonth = PropertyLoader.getProperty(properties, "dayOfMonth");
                Date dayDate = (Date) dayFormatter.stringToValue(dayOfMonth);
                Calendar dayCalendar = Calendar.getInstance();
                dayCalendar.setTime(dayDate);

                schedule = monthlyOnDayAndHourAndMinute(dayCalendar.get(Calendar.DAY_OF_MONTH), timeCalendar.get(Calendar.HOUR_OF_DAY), timeCalendar.get(Calendar.MINUTE));
            } else {
                logger.error("Invalid pruner interval: " + interval);
                return null;
            }
        }

        return newTrigger()// @formatter:off
                .withIdentity(PRUNER_TRIGGER_KEY)
                .withSchedule(schedule)
                .startNow()
                .build(); // @formatter:on
    }
    
    private String getElapsedTimeText(Calendar startTime, Calendar endTime) {
        long minsElapsed = (endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 60000;
        return (minsElapsed + " minute" + ((minsElapsed != 1) ? "s" : ""));
    }
}
