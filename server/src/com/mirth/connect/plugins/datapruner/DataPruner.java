/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datapruner;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.util.ThreadUtils;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelProperties;
import com.mirth.connect.model.InvalidChannel;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.ServerEvent.Level;
import com.mirth.connect.model.ServerEvent.Outcome;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.MessageExporter;
import com.mirth.connect.util.messagewriter.MessageWriter;
import com.mirth.connect.util.messagewriter.MessageWriterFactory;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class DataPruner implements Runnable {
    /**
     * The maximum allowed list size for executing DELETE ... WHERE IN ([list]).
     * Oracle does not allow the list size to exceed 1000.
     */
    private final static int LIST_LIMIT = 1000;

    public enum Strategy {
        INCLUDE_LIST, EXCLUDE_LIST, INCLUDE_RANGES, EXCLUDE_RANGES;
    }

    private int retryCount;
    private boolean skipIncomplete;
    private Status[] skipStatuses;
    private Integer blockSize;
    private int pageSize = 1000;
    private Strategy strategy;
    private boolean archiveEnabled;
    private MessageWriterOptions archiverOptions;
    private boolean pruneEvents;
    private Integer maxEventAge;
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private AtomicBoolean running = new AtomicBoolean(false);
    private Thread pruneThread;
    private MessageExporter messageExporter = new MessageExporter();
    private DataPrunerStatus status = new DataPrunerStatus();
    private DataPrunerStatus lastStatus;
    private Logger logger = Logger.getLogger(getClass());

    public DataPruner() {
        this.retryCount = 3;
        this.skipIncomplete = true;
        this.skipStatuses = new Status[] { Status.ERROR, Status.QUEUED };
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isSkipIncomplete() {
        return skipIncomplete;
    }

    public void setSkipIncomplete(boolean skipIncomplete) {
        this.skipIncomplete = skipIncomplete;
    }

    public Status[] getSkipStatuses() {
        return skipStatuses;
    }

    public void setSkipStatuses(Status[] skipStatuses) {
        this.skipStatuses = skipStatuses;
    }

    public Integer getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(Integer blockSize) {
        if (blockSize != null && blockSize > 0) {
            this.blockSize = blockSize;
        } else {
            this.blockSize = null;
        }
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int archiverPageSize) {
        this.pageSize = archiverPageSize;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public boolean isArchiveEnabled() {
        return archiveEnabled;
    }

    public void setArchiveEnabled(boolean archiveEnabled) {
        this.archiveEnabled = archiveEnabled;
    }

    public MessageWriterOptions getArchiverOptions() {
        return archiverOptions;
    }

    public void setArchiverOptions(MessageWriterOptions archiverOptions) {
        this.archiverOptions = archiverOptions;
    }

    public boolean isPruneEvents() {
        return pruneEvents;
    }

    public void setPruneEvents(boolean pruneEvents) {
        this.pruneEvents = pruneEvents;
    }

    public Integer getMaxEventAge() {
        return maxEventAge;
    }

    public void setMaxEventAge(Integer maxEventAge) {
        this.maxEventAge = maxEventAge;
    }

    public DataPrunerStatus getPrunerStatus() {
        return status;
    }

    public DataPrunerStatus getLastPrunerStatus() {
        return lastStatus;
    }

    public MessageExporter getMessageExporter() {
        return messageExporter;
    }

    public boolean isRunning() {
        return running.get();
    }

    public synchronized boolean start() {
        if (!running.compareAndSet(false, true)) {
            logger.warn("The data pruner is already running");
            return false;
        }

        status = new DataPrunerStatus();
        status.setStartTime(Calendar.getInstance());

        logger.debug("Triggering data pruner task");
        pruneThread = new Thread(this);
        pruneThread.start();

        return true;
    }

    public synchronized void stop() throws InterruptedException {
        if (running.get()) {
            logger.debug("Halting Data Pruner");
            if (pruneThread != null) {
                pruneThread.interrupt();

                logger.debug("Waiting for Data Pruner to terminate");
                pruneThread.join();
            }

            logger.debug("Data Pruner halted successfully");
        }
    }

    private Queue<PrunerTask> buildTaskQueue() throws Exception {
        List<Channel> channels = com.mirth.connect.server.controllers.ChannelController.getInstance().getChannels(null);

        Queue<PrunerTask> queue = new LinkedList<PrunerTask>();

        for (Channel channel : channels) {
            if (!(channel instanceof InvalidChannel)) {
                ChannelProperties properties = channel.getProperties();
                Integer pruneMetaDataDays = properties.getPruneMetaDataDays();
                Integer pruneContentDays = properties.getPruneContentDays();
                Calendar contentDateThreshold = null;
                Calendar messageDateThreshold = null;

                switch (channel.getProperties().getMessageStorageMode()) {
                    case DEVELOPMENT:
                    case PRODUCTION:
                    case RAW:
                        if (pruneContentDays != null) {
                            contentDateThreshold = Calendar.getInstance();
                            contentDateThreshold.set(Calendar.DAY_OF_MONTH, contentDateThreshold.get(Calendar.DAY_OF_MONTH) - pruneContentDays);
                        }

                    case METADATA:
                        if (pruneMetaDataDays != null) {
                            messageDateThreshold = Calendar.getInstance();
                            messageDateThreshold.set(Calendar.DAY_OF_MONTH, messageDateThreshold.get(Calendar.DAY_OF_MONTH) - pruneMetaDataDays);
                        }

                        if (messageDateThreshold != null || contentDateThreshold != null) {
                            queue.add(new PrunerTask(channel.getId(), channel.getName(), messageDateThreshold, contentDateThreshold));
                            status.getPendingChannelIds().add(channel.getId());
                        }
                        break;

                    case DISABLED:
                        break;

                    default:
                        String errorMessage = "Unrecognized message storage mode: " + properties.getMessageStorageMode().toString();
                        logger.error(errorMessage);

                        Map<String, String> attributes = new HashMap<String, String>();
                        attributes.put("Channel", channel.getName());
                        attributes.put("Error", errorMessage);

                        eventController.dispatchEvent(new ServerEvent(DataPrunerService.PLUGINPOINT, Level.ERROR, Outcome.FAILURE, attributes));
                        break;
                }
            }
        }

        return queue;
    }

    @Override
    public void run() {
        try {
            logger.debug("Executing pruner, started at " + new SimpleDateFormat("MM/dd/yyyy hh:mm aa").format(Calendar.getInstance().getTime()));

            if (pruneEvents) {
                pruneEvents();
            }

            String date = new SimpleDateFormat(MessageWriterFactory.ARCHIVE_DATE_PATTERN).format(Calendar.getInstance().getTime());
            String archiveFolder = (archiveEnabled) ? archiverOptions.getRootFolder() + IOUtils.DIR_SEPARATOR + date : null;
            Queue<PrunerTask> taskQueue;

            try {
                taskQueue = buildTaskQueue();
            } catch (Exception e) {
                // the error should already be logged
                return;
            }

            logger.debug("Pruner task queue built, " + taskQueue.size() + " channels will be processed");

            while (!taskQueue.isEmpty()) {
                ThreadUtils.checkInterruptedStatus();
                PrunerTask task = taskQueue.poll();

                try {
                    status.setCurrentChannelId(task.getChannelId());
                    status.setCurrentChannelName(task.getChannelName());
                    status.setTaskStartTime(Calendar.getInstance());

                    PruneResult result = pruneChannel(task.getChannelId(), task.getMessageDateThreshold(), task.getContentDateThreshold(), archiveFolder);

                    status.getProcessedChannelIds().add(task.getChannelId());

                    Map<String, String> attributes = new HashMap<String, String>();
                    attributes.put("Channel ID", task.getChannelId());
                    attributes.put("Channel Name", task.getChannelName());

                    if (archiveEnabled) {
                        attributes.put("Messages Archived", Long.toString(result.numMessagesArchived));
                    }

                    attributes.put("Messages Pruned", Long.toString(result.numMessagesPruned));
                    attributes.put("Content Rows Pruned", Long.toString(result.numContentPruned));
                    attributes.put("Time Elapsed", getTimeElapsed());
                    eventController.dispatchEvent(new ServerEvent(DataPrunerService.PLUGINPOINT, Level.INFORMATION, Outcome.SUCCESS, attributes));
                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    status.getFailedChannelIds().add(task.getChannelId());

                    Map<String, String> attributes = new HashMap<String, String>();
                    attributes.put("channel", task.getChannelName());
                    attributes.put("error", e.getMessage());
                    attributes.put("trace", ExceptionUtils.getStackTrace(e));
                    eventController.dispatchEvent(new ServerEvent(DataPrunerService.PLUGINPOINT, Level.ERROR, Outcome.FAILURE, attributes));
                    logger.error("Could not prune messages for channel: " + task.getChannelName(), e);
                } finally {
                    status.getPendingChannelIds().remove(task.getChannelId());
                    status.setCurrentChannelId(null);
                    status.setCurrentChannelName(null);
                }
            }

            logger.debug("Pruner job finished executing");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            eventController.dispatchEvent(new ServerEvent(DataPrunerService.PLUGINPOINT + " Halted", Level.INFORMATION, Outcome.SUCCESS, null));
            logger.debug("Data Pruner halted");
        } catch (Throwable t) {
            logger.error("An error occurred while executing the data pruner", t);
        } finally {
            status.setEndTime(Calendar.getInstance());
            lastStatus = SerializationUtils.clone(status);
            running.set(false);
        }
    }

    private void pruneEvents() {
        logger.debug("Pruning events");
        status.setPruningEvents(true);

        try {
            status.setTaskStartTime(Calendar.getInstance());

            Calendar dateThreshold = Calendar.getInstance();
            dateThreshold.set(Calendar.DAY_OF_MONTH, dateThreshold.get(Calendar.DAY_OF_MONTH) - maxEventAge);

            SqlSession session = SqlConfig.getSqlSessionManager().openSession(true);

            try {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("dateThreshold", dateThreshold);

                int numEventsPruned = session.delete("Message.pruneEvents", params);

                Map<String, String> attributes = new HashMap<String, String>();
                attributes.put("Events Pruned", Integer.toString(numEventsPruned));
                attributes.put("Time Elapsed", getTimeElapsed());
                eventController.dispatchEvent(new ServerEvent(DataPrunerService.PLUGINPOINT, Level.INFORMATION, Outcome.SUCCESS, attributes));
            } finally {
                session.close();
            }
        } finally {
            status.setEndTime(Calendar.getInstance());
            status.setPruningEvents(false);
        }
    }

    public PruneResult pruneChannel(String channelId, Calendar messageDateThreshold, Calendar contentDateThreshold, String archiveFolder) throws InterruptedException, DataPrunerException {
        logger.debug("Executing pruner for channel: " + channelId);

        if (messageDateThreshold == null && contentDateThreshold == null) {
            return new PruneResult();
        }

        // the content date threshold is only used/needed if it is later than the message date threshold
        if (messageDateThreshold != null && contentDateThreshold != null && contentDateThreshold.getTimeInMillis() <= messageDateThreshold.getTimeInMillis()) {
            contentDateThreshold = null;
        }

        int retries = retryCount;
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);

        while (true) {
            ThreadUtils.checkInterruptedStatus();

            try {
                /*
                 * Choose the method of pruning. If we are not archiving, then
                 * prune by date.
                 * Otherwise select the message ids to prune first, then
                 * constrain the deletion to
                 * those ids. This is necessary when archiving in order to be
                 * sure that only
                 * messages that were successfully archived get deleted.
                 */
                if (!archiveEnabled && strategy == null && !DatabaseUtil.statementExists("Message.pruneMessagesByIds")) {
                    return pruneChannelByDate(localChannelId, messageDateThreshold, contentDateThreshold);
                } else {
                    PruneIds ids;
                    PruneResult result = new PruneResult();

                    if (!archiveEnabled) {
                        ids = getIdsToPrune(localChannelId, messageDateThreshold, contentDateThreshold);
                    } else {
                        ids = archive(channelId, messageDateThreshold, contentDateThreshold, archiveFolder);
                        result.numMessagesArchived = ids.messageIds.size();
                    }

                    Integer blockSize = getBlockSize();

                    if (blockSize == null) {
                        result.numContentPruned = pruneChannelByIds(localChannelId, ids.contentMessageIds, true);
                        result.numMessagesPruned = pruneChannelByIds(localChannelId, ids.messageIds, false);
                    } else {
                        int listSize = ids.contentMessageIds.size();

                        for (int i = 0; i < listSize; i += blockSize) {
                            result.numContentPruned = pruneChannelByIds(localChannelId, ids.contentMessageIds.subList(i, Math.min(listSize, i + blockSize)), true);
                        }

                        listSize = ids.messageIds.size();

                        for (int i = 0; i < listSize; i += blockSize) {
                            result.numMessagesPruned = pruneChannelByIds(localChannelId, ids.messageIds.subList(i, Math.min(listSize, i + blockSize)), false);
                        }
                    }

                    return result;
                }
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                if (retries > 0) {
                    retries--;
                } else {
                    throw new DataPrunerException("Failed to prune messages", e);
                }
            }
        }
    }

    private PruneResult pruneChannelByDate(long localChannelId, Calendar messageDateThreshold, Calendar contentDateThreshold) throws InterruptedException {
        Integer blockSize = getBlockSize();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("localChannelId", localChannelId);
        params.put("skipIncomplete", isSkipIncomplete());

        if (getSkipStatuses().length > 0) {
            params.put("skipStatuses", getSkipStatuses());
        }

        if (blockSize != null) {
            params.put("limit", blockSize);
        }

        PruneResult result = new PruneResult();
        long startTime = System.currentTimeMillis();

        if (contentDateThreshold != null) {
            logger.debug("Pruning content");
            params.put("dateThreshold", contentDateThreshold);
            result.numContentPruned += runDelete("Message.pruneMessageContent", params, blockSize);
        }

        logger.debug("Pruning messages");
        params.put("dateThreshold", messageDateThreshold);

        if (DatabaseUtil.statementExists("Message.pruneAttachments")) {
            runDelete("Message.pruneAttachments", params, blockSize);
        }

        if (DatabaseUtil.statementExists("Message.pruneCustomMetaData")) {
            runDelete("Message.pruneCustomMetaData", params, blockSize);
        }

        runDelete("Message.pruneMessageContent", params, blockSize);

        if (DatabaseUtil.statementExists("Message.pruneConnectorMessages")) {
            runDelete("Message.pruneConnectorMessages", params, blockSize);
        }

        result.numMessagesPruned += runDelete("Message.pruneMessages", params, blockSize);

        logger.debug("Pruning completed in " + (System.currentTimeMillis() - startTime) + "ms");
        return result;
    }

    private PruneIds getIdsToPrune(long localChannelId, Calendar messageDateThreshold, Calendar contentDateThreshold) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("localChannelId", localChannelId);
        params.put("skipIncomplete", isSkipIncomplete());
        params.put("dateThreshold", (contentDateThreshold == null) ? messageDateThreshold : contentDateThreshold);

        if (getSkipStatuses().length > 0) {
            params.put("skipStatuses", getSkipStatuses());
        }

        List<Map<String, Object>> maps;
        SqlSession session = SqlConfig.getSqlSessionManager().openSession(true);

        try {
            maps = session.selectList("Message.getMessagesToPrune", params);
        } finally {
            session.close();
        }

        PruneIds pruneIds = new PruneIds();

        for (Map<String, Object> map : maps) {
            long receivedDate = ((Calendar) map.get("mm_received_date")).getTimeInMillis();
            long id = (Long) map.get("id");

            if (messageDateThreshold != null && receivedDate < messageDateThreshold.getTimeInMillis()) {
                pruneIds.messageIds.add(id);
            }

            pruneIds.contentMessageIds.add(id);
        }

        return pruneIds;
    }

    private PruneIds archive(String channelId, Calendar messageDateThreshold, Calendar contentDateThreshold, String archiveFolder) throws DataPrunerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("localChannelId", ChannelController.getInstance().getLocalChannelId(channelId));
        params.put("skipIncomplete", isSkipIncomplete());
        params.put("dateThreshold", (contentDateThreshold == null) ? messageDateThreshold : contentDateThreshold);

        if (getSkipStatuses().length > 0) {
            params.put("skipStatuses", getSkipStatuses());
        }

        DataPrunerMessageList messageList = new DataPrunerMessageList(channelId, pageSize, params, messageDateThreshold);
        String tempChannelFolder = archiveFolder + "/." + channelId;
        String finalChannelFolder = archiveFolder + "/" + channelId;

        try {
            MessageWriterOptions messageWriterOptions = SerializationUtils.clone(archiverOptions);
            messageWriterOptions.setBaseFolder(System.getProperty("user.dir"));

            if (messageWriterOptions.getArchiveFormat() == null) {
                messageWriterOptions.setRootFolder(tempChannelFolder);
            } else {
                messageWriterOptions.setRootFolder(archiveFolder);
                messageWriterOptions.setArchiveFileName(channelId);
            }

            logger.debug("Running archiver, channel: " + channelId + ", root folder: " + messageWriterOptions.getRootFolder() + ", archive format: " + messageWriterOptions.getArchiveFormat() + ", archive filename: " + messageWriterOptions.getArchiveFileName() + ", file pattern: " + messageWriterOptions.getFilePattern());
            status.setArchiving(true);
            MessageWriter archiver = MessageWriterFactory.getInstance().getMessageWriter(messageWriterOptions, ConfigurationController.getInstance().getEncryptor());
            messageExporter.exportMessages(messageList, archiver);
            archiver.close();

            if (messageWriterOptions.getArchiveFormat() == null && new File(tempChannelFolder).isDirectory()) {
                try {
                    FileUtils.moveDirectory(new File(tempChannelFolder), new File(finalChannelFolder));
                } catch (IOException e) {
                    logger.error("Failed to move " + tempChannelFolder + " to " + finalChannelFolder, e);
                }
            }

            PruneIds ids = new PruneIds();
            ids.messageIds = messageList.getMessageIds();
            ids.contentMessageIds = messageList.getContentMessageIds();

            return ids;
        } catch (Throwable t) {
            FileUtils.deleteQuietly(new File(tempChannelFolder));
            FileUtils.deleteQuietly(new File(finalChannelFolder));
            throw new DataPrunerException(t);
        } finally {
            status.setArchiving(false);
        }
    }

    private long pruneChannelByIds(long localChannelId, List<Long> messageIds, boolean contentOnly) throws DataPrunerException, InterruptedException {
        if (messageIds.size() == 0) {
            logger.debug("Skipping pruner since no messages were found to prune");
            return 0;
        }

        List<Long> invertedMessageIdList = getInvertedList(messageIds);
        List<long[]> includeRanges = null;
        List<long[]> excludeRanges = null;
        Strategy strategy = this.strategy;

        /*
         * If a query strategy hasn't been defined, automatically choose one. If
         * the # of archived
         * messages is less than the # unarchived, then we want to prune using
         * DELETE ... WHERE IN
         * ([archived message ids]). Otherwise, we want to delete by excluding
         * the unarchived
         * messages: DELETE ... WHERE NOT IN ([unarchived message ids]) AND id
         * BETWEEN min AND max.
         */
        if (strategy == null) {
            if (messageIds.size() > LIST_LIMIT && invertedMessageIdList.size() > LIST_LIMIT) {
                includeRanges = getRanges(messageIds);
                excludeRanges = getRanges(invertedMessageIdList);

                strategy = (includeRanges.size() < excludeRanges.size()) ? Strategy.INCLUDE_RANGES : Strategy.EXCLUDE_RANGES;
            } else {
                strategy = (messageIds.size() < invertedMessageIdList.size()) ? Strategy.INCLUDE_LIST : Strategy.EXCLUDE_LIST;
            }
        }

        ThreadUtils.checkInterruptedStatus();
        Integer limit = getBlockSize();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("localChannelId", localChannelId);

        switch (strategy) {
            case INCLUDE_LIST:
                params.put("includeMessageList", StringUtils.join(messageIds, ','));
                break;

            case EXCLUDE_LIST:
                params.put("minMessageId", messageIds.get(0));
                params.put("maxMessageId", messageIds.get(messageIds.size() - 1));

                if (invertedMessageIdList.size() > 0) {
                    params.put("excludeMessageList", StringUtils.join(invertedMessageIdList, ','));
                }
                break;

            case INCLUDE_RANGES:
                if (includeRanges == null) {
                    includeRanges = getRanges(messageIds);
                }

                params.put("includeMessageRanges", includeRanges);
                break;

            case EXCLUDE_RANGES:
                if (excludeRanges == null) {
                    excludeRanges = getRanges(invertedMessageIdList);
                }

                List<long[]> ranges = excludeRanges;

                params.put("minMessageId", messageIds.get(0));
                params.put("maxMessageId", messageIds.get(messageIds.size() - 1));

                if (!ranges.isEmpty()) {
                    params.put("excludeMessageRanges", ranges);
                }
                break;
        }

        ThreadUtils.checkInterruptedStatus();
        logger.debug("Pruning " + messageIds.size() + " message " + (contentOnly ? "content " : "") + "records in local channel id " + localChannelId + " with strategy: " + strategy);
        ThreadUtils.checkInterruptedStatus();
        int numPruned;
        long startTime = System.currentTimeMillis();

        if (contentOnly) {
            numPruned = runDelete("Message.pruneMessageContent", params, limit);
        } else {
            if (DatabaseUtil.statementExists("Message.pruneAttachments")) {
                runDelete("Message.pruneAttachments", params, limit);
            }

            if (DatabaseUtil.statementExists("Message.pruneCustomMetaData")) {
                runDelete("Message.pruneCustomMetaData", params, limit);
            }

            runDelete("Message.pruneMessageContent", params, limit);

            if (DatabaseUtil.statementExists("Message.pruneConnectorMessages")) {
                runDelete("Message.pruneConnectorMessages", params, limit);
            }

            if (DatabaseUtil.statementExists("Message.pruneMessagesByIds")) {
                numPruned = runDelete("Message.pruneMessagesByIds", params, limit);
            } else {
                numPruned = runDelete("Message.pruneMessages", params, limit);
            }
        }

        logger.debug("Pruning ended in " + (System.currentTimeMillis() - startTime) + "ms");
        return numPruned;
    }

    private int runDelete(String query, Map<String, Object> params, Integer limit) throws InterruptedException {
        SqlSession session = SqlConfig.getSqlSessionManager().openSession(true);
        ThreadUtils.checkInterruptedStatus();

        if (DatabaseUtil.statementExists("initDataPruner", session)) {
            session.update("initDataPruner");
        }

        try {
            status.setPruning(true);

            if (limit == null) {
                int count = session.delete(query, params);
                ThreadUtils.checkInterruptedStatus();
                return count;
            }

            int deletedRows;
            int total = 0;

            do {
                deletedRows = session.delete(query, params);
                ThreadUtils.checkInterruptedStatus();
                total += deletedRows;
            } while (deletedRows >= limit && deletedRows > 0);

            return total;
        } finally {
            session.close();
            status.setPruning(false);
        }
    }

    private List<long[]> getRanges(List<Long> sortedValues) {
        List<long[]> ranges = new ArrayList<long[]>();
        int size = sortedValues.size();

        if (size == 0) {
            return ranges;
        }

        long start = sortedValues.get(0);
        long end = start;

        for (int i = 1; i < size; i++) {
            long value = sortedValues.get(i);

            if (value == (end + 1)) {
                end = value;
            } else {
                ranges.add(new long[] { start, end });
                start = value;
                end = start;
            }
        }

        ranges.add(new long[] { start, end });
        return ranges;
    }

    private List<Long> getInvertedList(List<Long> sortedValues) {
        List<Long> invertedList = new ArrayList<Long>();
        int numValues = sortedValues.size();

        if (numValues == 0) {
            return invertedList;
        }

        long lastValue = sortedValues.get(0);
        long currentValue;

        for (int i = 1; i < numValues; i++) {
            currentValue = sortedValues.get(i);

            /*
             * See if there is a gap at the current position in the list, if
             * there is, then add all
             * of the gap values in the inverted list
             */
            if (currentValue > (lastValue + 1)) {
                for (long j = lastValue + 1; j < currentValue; j++) {
                    invertedList.add(j);
                }
            }

            lastValue = currentValue;
        }

        return invertedList;
    }

    private String getTimeElapsed() {
        long ms = System.currentTimeMillis() - status.getTaskStartTime().getTimeInMillis();
        long mins = ms / 60000;
        long secs = (ms % 60000) / 1000;

        return mins + " minute" + (mins == 1 ? "" : "s") + ", " + secs + " second" + (secs == 1 ? "" : "s");
    }

    private class PruneIds {
        public List<Long> messageIds = new ArrayList<Long>();
        public List<Long> contentMessageIds = new ArrayList<Long>();
    }

    private class PruneResult {
        public long numMessagesArchived;
        public long numMessagesPruned;
        public long numContentPruned;
    }

    private class PrunerTask {
        private String channelId;
        private String channelName;
        private Calendar messageDateThreshold;
        private Calendar contentDateThreshold;

        public PrunerTask(String channelId, String channelName, Calendar messageDateThreshold, Calendar contentDateThreshold) {
            this.channelId = channelId;
            this.channelName = channelName;
            this.messageDateThreshold = messageDateThreshold;
            this.contentDateThreshold = contentDateThreshold;
        }

        public String getChannelId() {
            return channelId;
        }

        public String getChannelName() {
            return channelName;
        }

        public Calendar getMessageDateThreshold() {
            return messageDateThreshold;
        }

        public Calendar getContentDateThreshold() {
            return contentDateThreshold;
        }
    }
}
