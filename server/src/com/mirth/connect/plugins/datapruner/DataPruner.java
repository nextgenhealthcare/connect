/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datapruner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.util.ThreadUtils;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelProperties;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.ServerEvent.Level;
import com.mirth.connect.model.ServerEvent.Outcome;
import com.mirth.connect.plugins.datapruner.ArchiverMessageList;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.MessageExporter;
import com.mirth.connect.util.MessageExporter.MessageExportException;
import com.mirth.connect.util.messagewriter.MessageWriter;
import com.mirth.connect.util.messagewriter.MessageWriterException;
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
    private ExecutorService executor;
    private MessageExporter messageExporter = new MessageExporter();
    private DataPrunerStatus status = new DataPrunerStatus();
    private DataPrunerStatus lastStatus;
    private Logger logger = Logger.getLogger(this.getClass());

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
        executor = Executors.newSingleThreadExecutor();
        executor.submit(this);
        
        return true;
    }

    public synchronized void stop() throws InterruptedException {
        if (running.get()) {
            logger.debug("Halting Data Pruner");
            executor.shutdownNow();
            
            while (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.debug("Waiting for Data Pruner to terminate");
            }
            
            logger.debug("Data Pruner halted successfully");
        }
    }
    
    private Queue<PrunerTask> buildTaskQueue() throws Exception {
        List<Channel> channels = com.mirth.connect.server.controllers.ChannelController.getInstance().getChannels(null);

        Queue<PrunerTask> queue = new LinkedList<PrunerTask>();
        
        for (Channel channel : channels) {
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
        
        return queue;
    }

    @Override
    public void run() {
        try {
            logger.debug("Executing pruner, started at " + new SimpleDateFormat("MM/dd/yyyy hh:mm aa").format(status.getStartTime().getTime()));
            
            if (pruneEvents) {
                pruneEvents();
            }
            
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
                    
                    int[] result = new int[] { 0, 0, 0 };
                    MessageWriter archiver = null;
    
                    if (archiveEnabled) {
                        archiver = MessageWriterFactory.getInstance().getMessageWriter(archiverOptions, ConfigurationController.getInstance().getEncryptor(), task.getChannelId());
                    }
                    
                    result = pruneChannel(task.getChannelId(), archiver, task.getMessageDateThreshold(), task.getContentDateThreshold());
                    
                    status.getProcessedChannelIds().add(task.getChannelId());
                    
                    Map<String, String> attributes = new HashMap<String, String>();
                    attributes.put("Channel ID", task.getChannelId());
                    attributes.put("Channel Name", task.getChannelName());
                    
                    if (archiveEnabled) {
                        attributes.put("Messages Archived", Integer.toString(result[0]));
                    }
                    
                    attributes.put("Messages Pruned", Integer.toString(result[1]));
                    attributes.put("Content Rows Pruned", Integer.toString(result[2]));
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

                int numEventsPruned = session.delete("Message.prunerDeleteEvents", params);

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

    public int[] pruneChannel(String channelId, MessageWriter archiver, Calendar messageDateThreshold, Calendar contentDateThreshold) throws InterruptedException, DataPrunerException {
        logger.debug("Executing pruner for channel: " + channelId);

        if (messageDateThreshold == null && contentDateThreshold == null) {
            return new int[] { 0, 0, 0 };
        }

        // the content date threshold is only used/needed if it is later than the message date threshold
        if (messageDateThreshold != null && contentDateThreshold != null && contentDateThreshold.getTimeInMillis() <= messageDateThreshold.getTimeInMillis()) {
            contentDateThreshold = null;
        }

        int retries = retryCount;

        while (true) {
            ThreadUtils.checkInterruptedStatus();

            try {
                if (archiver == null) {
                    return pruneWithoutArchiver(channelId, messageDateThreshold, contentDateThreshold);
                } else {
                    return pruneWithArchiver(archiver, channelId, messageDateThreshold, contentDateThreshold);
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

    private int[] pruneWithoutArchiver(String channelId, Calendar messageDateThreshold, Calendar contentDateThreshold) throws InterruptedException {
        int numMessagesPruned = 0;
        int numContentPruned = 0;

        Integer limit = getBlockSize();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("localChannelId", ChannelController.getInstance().getLocalChannelId(channelId));
        params.put("dateThreshold", contentDateThreshold);
        params.put("skipIncomplete", isSkipIncomplete());

        if (getSkipStatuses().length > 0) {
            params.put("skipStatuses", getSkipStatuses());
        }

        if (limit != null) {
            params.put("limit", limit);
        }

        long startTime = System.currentTimeMillis();

        if (contentDateThreshold != null) {
            logger.debug("Pruning content");
            numContentPruned += runDelete("Message.prunerDeleteMessageContent", params, limit, true);
        }

        if (messageDateThreshold != null) {
            logger.debug("Pruning messages");
            params.put("dateThreshold", messageDateThreshold);

            if (contentDateThreshold == null) {
                numContentPruned += runDelete("Message.prunerDeleteMessageContent", params, limit, true);
            }

            /*
             * These manual "cascade" delete queries are only needed for databases that don't
             * support cascade deletion with the Message.prunerDeleteMessages query. When
             * manually cascading, we do not allow the thread to be interrupted while deleting,
             * since it could result in partially deleted messages.
             */
            if (DatabaseUtil.statementExists("Message.prunerCascadeDeleteConnectorMessages")) {
                numContentPruned += runDelete("Message.prunerCascadeDeleteMessageContent", params, limit, false);
                runDelete("Message.prunerCascadeDeleteCustomMetadata", params, limit, false);
                runDelete("Message.prunerCascadeDeleteAttachments", params, limit, false);
                runDelete("Message.prunerCascadeDeleteConnectorMessages", params, limit, false);
                numMessagesPruned += runDelete("Message.prunerDeleteMessages", params, limit, false);

                ThreadUtils.checkInterruptedStatus();
            } else {
                numMessagesPruned += runDelete("Message.prunerDeleteMessages", params, limit, true);
            }
        }

        logger.debug("Pruning completed in " + (System.currentTimeMillis() - startTime) + "ms");
        return new int[] { 0, numMessagesPruned, numContentPruned };
    }

    private int[] pruneWithArchiver(MessageWriter archiver, String channelId, Calendar messageDateThreshold, Calendar contentDateThreshold) throws DataPrunerException, InterruptedException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("localChannelId", ChannelController.getInstance().getLocalChannelId(channelId));
        params.put("skipIncomplete", isSkipIncomplete());
        params.put("dateThreshold", (contentDateThreshold == null) ? messageDateThreshold : contentDateThreshold);

        if (getSkipStatuses().length > 0) {
            params.put("skipStatuses", getSkipStatuses());
        }

        ArchiverMessageList messageList = new ArchiverMessageList(channelId, pageSize, params);

        if (archiver instanceof PassthruMessageWriter) {
            messageList.setIncludeContent(false);
        }

        List<Long> archivedMessageIds;

        try {
            logger.debug("Running archiver for channel: " + channelId);
            status.setArchiving(true);
            archivedMessageIds = messageExporter.exportMessages(messageList, archiver).getProcessedIds();
        } catch (MessageExportException e) {
            throw new DataPrunerException(e);
        } finally {
            status.setArchiving(false);
        }

        if (archivedMessageIds.size() == 0) {
            logger.debug("Not pruning since no messages were archived");
            return new int[] { 0, 0, 0 };
        }

        Integer limit = getBlockSize();

        params = new HashMap<String, Object>();
        params.put("localChannelId", ChannelController.getInstance().getLocalChannelId(channelId));

        if (limit != null) {
            params.put("limit", limit);
        }

        List<Long> unarchivedMessageIds = getInvertedList(archivedMessageIds);
        List<long[]> includeRanges = null;
        List<long[]> excludeRanges = null;
        Strategy strategy = this.strategy;

        /*
         * If a query strategy hasn't been defined, automatically choose one. If the # of archived
         * messages is less than the # unarchived, then we want to prune using DELETE ... WHERE IN
         * ([archived message ids]). Otherwise, we want to delete by excluding the unarchived
         * messages: DELETE ... WHERE NOT IN ([unarchived message ids]) AND id BETWEEN min AND max.
         */
        if (strategy == null) {
            if (archivedMessageIds.size() > LIST_LIMIT && unarchivedMessageIds.size() > LIST_LIMIT) {
                includeRanges = getRanges(archivedMessageIds);
                excludeRanges = getRanges(unarchivedMessageIds);

                strategy = (includeRanges.size() < excludeRanges.size()) ? Strategy.INCLUDE_RANGES : Strategy.EXCLUDE_RANGES;
            } else {
                strategy = (archivedMessageIds.size() < unarchivedMessageIds.size()) ? Strategy.INCLUDE_LIST : Strategy.EXCLUDE_LIST;
            }
        }

        ThreadUtils.checkInterruptedStatus();

        switch (strategy) {
            case INCLUDE_LIST:
                params.put("includeMessageList", StringUtils.join(archivedMessageIds, ','));
                break;

            case EXCLUDE_LIST:
                params.put("minMessageId", archivedMessageIds.get(0));
                params.put("maxMessageId", archivedMessageIds.get(archivedMessageIds.size() - 1));

                if (unarchivedMessageIds.size() > 0) {
                    params.put("excludeMessageList", StringUtils.join(unarchivedMessageIds, ','));
                }
                break;

            case INCLUDE_RANGES:
                if (includeRanges == null) {
                    includeRanges = getRanges(archivedMessageIds);
                }

                params.put("includeMessageRanges", includeRanges);
                break;

            case EXCLUDE_RANGES:
                if (excludeRanges == null) {
                    excludeRanges = getRanges(unarchivedMessageIds);
                }

                List<long[]> ranges = excludeRanges;

                params.put("minMessageId", archivedMessageIds.get(0));
                params.put("maxMessageId", archivedMessageIds.get(archivedMessageIds.size() - 1));

                if (!ranges.isEmpty()) {
                    params.put("excludeMessageRanges", ranges);
                }
                break;
        }

        ThreadUtils.checkInterruptedStatus();
        logger.debug("Pruning " + archivedMessageIds.size() + " messages in channel " + channelId + " with strategy: " + strategy);
        int numContentPruned = 0;
        int numMessagesPruned = 0;
        SqlSession session = SqlConfig.getSqlSessionManager().openSession();
        ThreadUtils.checkInterruptedStatus();
        long startTime = System.currentTimeMillis();

        try {
            /*
             * Delete content for all messages that were archived if a content date threshold was
             * given.
             */
            if (contentDateThreshold != null) {
                numContentPruned += runDelete("Message.prunerDeleteMessageContent", params, limit, true);
            }

            if (messageDateThreshold != null) {
                /*
                 * If content was pruned separately, then we need to add an additional check for the
                 * message date threshold. If content was not pruned separately, then we are safe to
                 * delete all of the messages that were archived.
                 */
                if (contentDateThreshold != null) {
                    params.put("dateThreshold", messageDateThreshold);
                }

                /*
                 * These manual "cascade" delete queries are only needed for databases that don't
                 * support cascade deletion with the Message.prunerDeleteMessages query. When
                 * manually cascading, we do not allow the thread to be interrupted while deleting,
                 * since it could result in partially deleted messages.
                 */
                if (DatabaseUtil.statementExists("Message.prunerCascadeDeleteConnectorMessages")) {
                    numContentPruned += runDelete("Message.prunerCascadeDeleteMessageContent", params, limit, false);
                    runDelete("Message.prunerCascadeDeleteCustomMetadata", params, limit, false);
                    runDelete("Message.prunerCascadeDeleteAttachments", params, limit, false);
                    runDelete("Message.prunerCascadeDeleteConnectorMessages", params, limit, false);
                    numMessagesPruned += runDelete("Message.prunerDeleteMessages", params, limit, false);

                    ThreadUtils.checkInterruptedStatus();
                } else {
                    numMessagesPruned += runDelete("Message.prunerDeleteMessages", params, limit, true);
                }
            }

            session.commit();
            logger.debug("Pruning completed in " + (System.currentTimeMillis() - startTime) + "ms");
            return new int[] { archivedMessageIds.size(), numMessagesPruned, numContentPruned };
        } finally {
            session.close();
        }
    }

    protected int runDelete(String query, Map<String, Object> params, Integer limit, boolean interruptible) throws InterruptedException {
        SqlSession session = SqlConfig.getSqlSessionManager().openSession();

        if (interruptible) {
            ThreadUtils.checkInterruptedStatus();
        }

        try {
            status.setPruning(true);
            
            if (limit == null) {
                int count = session.delete(query, params);

                if (interruptible) {
                    ThreadUtils.checkInterruptedStatus();
                }

                session.commit();

                if (interruptible) {
                    ThreadUtils.checkInterruptedStatus();
                }

                return count;
            }

            int deletedRows;
            int total = 0;

            do {
                deletedRows = session.delete(query, params);

                if (interruptible) {
                    ThreadUtils.checkInterruptedStatus();
                }

                session.commit();

                if (interruptible) {
                    ThreadUtils.checkInterruptedStatus();
                }

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
             * See if there is a gap at the current position in the list, if there is, then add all
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

    private class PassthruMessageWriter implements MessageWriter {
        @Override
        public boolean write(Message message) throws MessageWriterException {
            return true;
        }

        @Override
        public void close() throws MessageWriterException {}
    }
}
