package com.mirth.connect.plugins.datapruner;

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

import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.util.PropertyLoader;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class DefaultDataPrunerController extends DataPrunerController {
    private static final int DEFAULT_PRUNING_BLOCK_SIZE = 0;
    private static final String PRUNER_JOB_KEY = "prunerJob";
    private static final String DATE_FORMAT = "MM/dd/yyyy hh:mm aa";
    private static final String PRUNER_TRIGGER_KEY = "prunerTrigger";

    public static DataPruner pruner = new DataPruner();

    private Scheduler scheduler;
    private ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
    private SchedulerFactory schedulerFactory;
    private JobDetail jobDetail;
    private Logger logger = Logger.getLogger(this.getClass());

    @Override
    public void init(Properties properties) throws DataPrunerException {
        try {
            schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();

            applyPrunerSettings(properties);

            jobDetail = newJob(DataPrunerJob.class).withIdentity(PRUNER_JOB_KEY).build();

            Trigger trigger = createTrigger(properties);

            if (trigger == null) {
                logger.debug("Data pruner disabled");
            } else {
                logger.debug("Scheduling data pruner job");
                scheduler.scheduleJob(jobDetail, trigger);
            }
        } catch (Exception e) {
            throw new DataPrunerException(e);
        }
    }

    @Override
    public void update(Properties properties) throws DataPrunerException {
        if (scheduler != null) {
            try {
                scheduler.deleteJob(new JobKey(PRUNER_JOB_KEY));
                applyPrunerSettings(properties);
                Trigger trigger = createTrigger(properties);

                if (trigger != null) {
                    scheduler.scheduleJob(jobDetail, trigger);
                    logger.debug("Scheduled job to " + new SimpleDateFormat(DATE_FORMAT).format(trigger.getNextFireTime()));
                }
            } catch (Exception e) {
                throw new DataPrunerException(e);
            }
        }
    }

    @Override
    public void start() throws DataPrunerException {
        try {
            scheduler.start();
        } catch (Exception e) {
            throw new DataPrunerException(e);
        }
    }

    @Override
    public void stop(boolean waitForJobsToComplete) throws DataPrunerException {
        if (scheduler != null) {
            try {
                scheduler.shutdown(waitForJobsToComplete);
                scheduler = null;
            } catch (Exception e) {
                throw new DataPrunerException(e);
            }
        }
    }

    @Override
    public boolean isStarted() throws DataPrunerException {
        try {
            return (scheduler != null && scheduler.isStarted());
        } catch (Exception e) {
            throw new DataPrunerException(e);
        }
    }

    @Override
    public void startPruner() {
        pruner.start();
    }

    @Override
    public void stopPruner() throws InterruptedException {
        pruner.stop();
    }

    @Override
    public DataPrunerStatus getPrunerStatus() {
        return pruner.getPrunerStatus();
    }

    @Override
    public boolean isPrunerRunning() throws DataPrunerException {
        return pruner.isRunning();
    }

    @Override
    public Map<String, String> getStatusMap() {
        Map<String, String> statusMap = new HashMap<String, String>();
        StringBuilder stringBuilder = new StringBuilder();
        DataPrunerStatus status = pruner.getPrunerStatus();

        if (pruner.isRunning()) {
            statusMap.put("isRunning", "true");

            if (status.isArchiving()) {
                stringBuilder.append("Archiving");
            } else if (status.isPruning()) {
                stringBuilder.append("Pruning");
            } else if (status.isPruningEvents()) {
                stringBuilder.append("Pruning events");
            } else {
                stringBuilder.append("Processing");
            }

            if (status.getCurrentChannelName() != null) {
                stringBuilder.append(" channel \"" + status.getCurrentChannelName() + "\"");

                if (status.isArchiving()) {
                    int count = pruner.getNumExported();
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

        DataPrunerStatus lastStatus = pruner.getLastPrunerStatus();

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

    private void applyPrunerSettings(Properties properties) {
        if (StringUtils.isNotEmpty(properties.getProperty("pruningBlockSize"))) {
            pruner.setBlockSize(Integer.parseInt(properties.getProperty("pruningBlockSize")));
        } else {
            pruner.setBlockSize(DEFAULT_PRUNING_BLOCK_SIZE);
        }

        pruner.setArchiveEnabled(Boolean.parseBoolean(properties.getProperty("archiveEnabled", Boolean.FALSE.toString())));
//        boolean includeAttachments = Boolean.parseBoolean(properties.getProperty("includeAttachments", Boolean.FALSE.toString()));

        if (pruner.isArchiveEnabled()) {
            if (properties.contains("archiverOptions")) {
                pruner.setArchiverOptions(new MessageWriterOptions());
            } else {
                pruner.setArchiverOptions(serializer.deserialize(properties.getProperty("archiverOptions"), MessageWriterOptions.class));
            }
        }

        if (Boolean.parseBoolean(properties.getProperty("pruneEvents", Boolean.FALSE.toString()))) {
            pruner.setPruneEvents(true);
            pruner.setMaxEventAge(Integer.parseInt(properties.getProperty("maxEventAge")));
        } else {
            pruner.setPruneEvents(false);
            pruner.setMaxEventAge(null);
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
