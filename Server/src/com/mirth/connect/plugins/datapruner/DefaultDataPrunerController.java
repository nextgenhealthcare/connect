/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datapruner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.quartz.Scheduler;

import com.mirth.connect.donkey.model.channel.PollConnectorProperties;
import com.mirth.connect.donkey.util.PollConnectorJobHandler;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class DefaultDataPrunerController extends DataPrunerController {
    private static final int MIN_PRUNING_BLOCK_SIZE = 50;
    private static final int MAX_PRUNING_BLOCK_SIZE = 10000;
    private static final int MAX_ARCHIVING_BLOCK_SIZE = 1000;
    private static final String DATE_FORMAT = "MM/dd/yyyy hh:mm aa";

    public static DataPruner pruner = new DataPruner();

    private boolean isEnabled;

    private Scheduler scheduler;
    private PollConnectorJobHandler handler;
    private ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
    private Logger logger = Logger.getLogger(this.getClass());

    @Override
    public void init(Properties properties) throws DataPrunerException {
        try {
            applyPrunerSettings(properties);

            handler = new PollConnectorJobHandler(pruner.getPollingProperties(), "DataPruner", false);
            handler.configureJob(DataPrunerJob.class, null, "DataPruner");

            if (!isEnabled) {
                logger.debug("Data pruner disabled");
            } else {
                handler.scheduleJob(false);
                scheduler = handler.getScheduler();
                logger.debug("Scheduling data pruner job");
            }
        } catch (Exception e) {
            throw new DataPrunerException(e);
        }
    }

    @Override
    public void update(Properties properties) throws DataPrunerException {
        try {
            if (scheduler != null && scheduler.checkExists(handler.getJob().getKey())) {
                stop(true);
            }

            applyPrunerSettings(properties);

            handler = new PollConnectorJobHandler(pruner.getPollingProperties(), "DataPruner", false);
            handler.configureJob(DataPrunerJob.class, null, "DataPruner");

            if (isEnabled) {
                handler.scheduleJob(true);
                scheduler = handler.getScheduler();
                logger.debug("Scheduled job to " + handler.getNextFireTime());
            }
        } catch (Exception e) {
            throw new DataPrunerException(e);
        }
    }

    @Override
    public void start() throws DataPrunerException {
        if (scheduler != null) {
            try {
                scheduler.start();
            } catch (Exception e) {
                throw new DataPrunerException(e);
            }
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

        String nextFireTime = handler.getNextFireTime();

        if (!StringUtils.isEmpty(nextFireTime)) {
            statusMap.put("nextProcess", "Scheduled " + nextFireTime);
        }

        return statusMap;
    }

    private void applyPrunerSettings(Properties properties) {
        if (StringUtils.isNotEmpty(properties.getProperty("pruningBlockSize"))) {
            int blockSize = NumberUtils.toInt(properties.getProperty("pruningBlockSize"));
            if (blockSize < MIN_PRUNING_BLOCK_SIZE) {
                blockSize = MIN_PRUNING_BLOCK_SIZE;
            } else if (blockSize > MAX_PRUNING_BLOCK_SIZE) {
                blockSize = DataPruner.DEFAULT_PRUNING_BLOCK_SIZE;
            }
            pruner.setPrunerBlockSize(blockSize);
        } else {
            pruner.setPrunerBlockSize(DataPruner.DEFAULT_PRUNING_BLOCK_SIZE);
        }

        isEnabled = Boolean.parseBoolean(properties.getProperty("enabled"));
        if (properties.containsKey("pollingProperties")) {
            pruner.setPollingProperties(serializer.deserialize(properties.getProperty("pollingProperties"), PollConnectorProperties.class));
        } else {
            PollConnectorProperties defaultProperties = new PollConnectorProperties();
            defaultProperties.setPollingFrequency(3600000);
            pruner.setPollingProperties(defaultProperties);
        }

        pruner.setArchiveEnabled(Boolean.parseBoolean(properties.getProperty("archiveEnabled", Boolean.FALSE.toString())));

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

        if (StringUtils.isNotEmpty(properties.getProperty("archiverBlockSize"))) {
            int blockSize = NumberUtils.toInt(properties.getProperty("archiverBlockSize"));
            if (blockSize <= 0 || blockSize > MAX_ARCHIVING_BLOCK_SIZE) {
                blockSize = DataPruner.DEFAULT_ARCHIVING_BLOCK_SIZE;
            }
            pruner.setArchiverBlockSize(blockSize);
        } else {
            pruner.setArchiverBlockSize(DataPruner.DEFAULT_ARCHIVING_BLOCK_SIZE);
        }
    }

    private String getElapsedTimeText(Calendar startTime, Calendar endTime) {
        long minsElapsed = (endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 60000;
        return (minsElapsed + " minute" + ((minsElapsed != 1) ? "s" : ""));
    }
}