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
import java.util.Iterator;
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

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
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
import com.mirth.connect.server.util.ListRangeIterator;
import com.mirth.connect.server.util.ListRangeIterator.ListRangeItem;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.MessageExporter.MessageExportException;
import com.mirth.connect.util.messagewriter.MessageWriter;
import com.mirth.connect.util.messagewriter.MessageWriterFactory;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class DataPruner implements Runnable {
    private final static int ARCHIVE_BATCH_SIZE = 1000;
    private final static int ID_RETRIEVE_LIMIT = 100000;

    private int numExported;
    private int retryCount;
    private boolean skipIncomplete;
    private Status[] skipStatuses;
    private Integer blockSize;
    private boolean archiveEnabled;
    private MessageWriterOptions archiverOptions;
    private boolean pruneEvents;
    private Integer maxEventAge;
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private DonkeyDaoFactory daoFactory;
    private AtomicBoolean running = new AtomicBoolean(false);
    private Thread pruneThread;
    private DataPrunerStatus status = new DataPrunerStatus();
    private DataPrunerStatus lastStatus;
    private Logger logger = Logger.getLogger(getClass());

    public DataPruner() {
        this.retryCount = 3;
        this.skipIncomplete = true;
        this.skipStatuses = new Status[] { Status.ERROR, Status.QUEUED };
    }

    public int getNumExported() {
        return numExported;
    }

    public void setNumExported(int numExported) {
        this.numExported = numExported;
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

    private DonkeyDaoFactory getDaoFactory() {
        /*
         * The DaoFactory can't be retrieved in the constructor because it will not have been
         * instantiated yet at that point.
         */
        if (daoFactory == null) {
            daoFactory = Donkey.getInstance().getDaoFactory();
        }

        return daoFactory;
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
                            queue.add(new PrunerTask(channel.getId(), channel.getName(), messageDateThreshold, contentDateThreshold, channel.getProperties().isArchiveEnabled()));
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

                    PruneResult result = pruneChannel(task.getChannelId(), task.getChannelName(), task.getMessageDateThreshold(), task.getContentDateThreshold(), archiveFolder, task.isArchiveEnabled());

                    status.getProcessedChannelIds().add(task.getChannelId());

                    Map<String, String> attributes = new HashMap<String, String>();
                    attributes.put("Channel ID", task.getChannelId());
                    attributes.put("Channel Name", task.getChannelName());

                    if (archiveEnabled && task.isArchiveEnabled()) {
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
                    Throwable t = e;
                    if (e instanceof DataPrunerException) {
                        t = e.getCause();
                    }
                    logger.error("Failed to prune messages for channel " + task.getChannelName() + " (" + task.getChannelId() + ").", t);
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

    public PruneResult pruneChannel(String channelId, String channelName, Calendar messageDateThreshold, Calendar contentDateThreshold, String archiveFolder, boolean channelArchiveEnabled) throws InterruptedException, DataPrunerException {
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
                long maxMessageId;

                DonkeyDao dao = getDaoFactory().getDao();
                try {
                    maxMessageId = dao.getMaxMessageId(channelId);
                } finally {
                    dao.close();
                }

                Map<String, Object> params = new HashMap<String, Object>();
                params.put("localChannelId", localChannelId);
                params.put("maxMessageId", maxMessageId);
                params.put("skipIncomplete", isSkipIncomplete());
                params.put("dateThreshold", (contentDateThreshold == null) ? messageDateThreshold : contentDateThreshold);

                if (getSkipStatuses().length > 0) {
                    params.put("skipStatuses", getSkipStatuses());
                }

                PruneResult result = new PruneResult();
                PruneIds messageIds = new PruneIds();
                PruneIds contentMessageIds = new PruneIds();

                if (!archiveEnabled || !channelArchiveEnabled) {
                    getIdsToPrune(params, messageDateThreshold, messageIds, contentMessageIds);
                } else {
                    archiveAndGetIdsToPrune(params, channelId, messageDateThreshold, archiveFolder, messageIds, contentMessageIds);
                }

                while (contentMessageIds.hasNext()) {
                    result.numContentPruned += pruneChannelByIds(localChannelId, contentMessageIds, true);
                }

                while (messageIds.hasNext()) {
                    result.numMessagesPruned += pruneChannelByIds(localChannelId, messageIds, false);
                }

                return result;
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                if (retries > 0) {
                    logger.error("Failed to prune messages for channel " + channelName + " (" + channelId + "). Attempts remaining: " + retries + ".", e);
                    retries--;
                } else {
                    throw new DataPrunerException("Failed to prune messages", e);
                }
            }
        }
    }

    private void getIdsToPrune(Map<String, Object> params, Calendar messageDateThreshold, PruneIds messageIds, PruneIds contentMessageIds) throws DataPrunerException, InterruptedException {
        params.put("limit", ID_RETRIEVE_LIMIT);
        params.put("archive", false);

        long minMessageId = 0;

        List<Map<String, Object>> maps;
        do {
            SqlSession session = SqlConfig.getSqlSessionManager().openSession(true);

            try {
                params.put("minMessageId", minMessageId);
                maps = session.selectList("Message.getMessagesToPrune", params);
            } finally {
                session.close();
            }

            for (Map<String, Object> map : maps) {
                long receivedDate = ((Calendar) map.get("mm_received_date")).getTimeInMillis();
                long id = (Long) map.get("id");

                if (messageDateThreshold != null && receivedDate < messageDateThreshold.getTimeInMillis()) {
                    messageIds.add(id);
                }

                contentMessageIds.add(id);
                minMessageId = id + 1;
            }
        } while (maps != null && maps.size() == ID_RETRIEVE_LIMIT);
    }

    private void archiveAndGetIdsToPrune(Map<String, Object> params, String channelId, Calendar messageDateThreshold, String archiveFolder, PruneIds messageIds, PruneIds contentMessageIds) throws DataPrunerException, InterruptedException {
        params.put("limit", ARCHIVE_BATCH_SIZE);
        params.put("archive", true);

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

            numExported = 0;
            long minMessageId = 0;
            List<Message> messageList = null;
            do {
                ThreadUtils.checkInterruptedStatus();
                try {
                    params.put("minMessageId", minMessageId);

                    messageList = getMessagesForArchive(channelId, params, messageDateThreshold, messageIds, contentMessageIds);

                    for (Message message : messageList) {
                        ThreadUtils.checkInterruptedStatus();

                        try {
                            if (archiver.write(message)) {
                                numExported++;
                            }

                        } catch (Exception e) {
                            Throwable cause = ExceptionUtils.getRootCause(e);
                            throw new MessageExportException("Failed to export message: " + cause.getMessage(), cause);
                        }

                        minMessageId = message.getMessageId() + 1;
                    }
                } catch (Exception e) {
                    if (e instanceof MessageExportException) {
                        throw (MessageExportException) e;
                    }

                    throw new MessageExportException(e);
                }
            } while (messageList != null && messageList.size() == ARCHIVE_BATCH_SIZE);

            archiver.close();

            if (messageWriterOptions.getArchiveFormat() == null && new File(tempChannelFolder).isDirectory()) {
                try {
                    FileUtils.moveDirectory(new File(tempChannelFolder), new File(finalChannelFolder));
                } catch (IOException e) {
                    logger.error("Failed to move " + tempChannelFolder + " to " + finalChannelFolder, e);
                }
            }
        } catch (Throwable t) {
            FileUtils.deleteQuietly(new File(tempChannelFolder));
            FileUtils.deleteQuietly(new File(finalChannelFolder));
            throw new DataPrunerException(t);
        } finally {
            status.setArchiving(false);
        }
    }

    private List<Message> getMessagesForArchive(String channelId, Map<String, Object> params, Calendar messageDateThreshold, PruneIds messageIds, PruneIds contentMessageIds) {
        List<Map<String, Object>> maps;
        SqlSession session = SqlConfig.getSqlSessionManager().openSession();

        try {
            maps = session.selectList("Message.getMessagesToPrune", params);
        } finally {
            session.close();
        }

        List<Message> messages = new ArrayList<Message>();

        DonkeyDao dao = getDaoFactory().getDao();

        try {
            for (Map<String, Object> map : maps) {
                Long messageId = (Long) map.get("id");
                long connectorReceivedDateMillis = ((Calendar) map.get("mm_received_date")).getTimeInMillis();

                Map<Integer, ConnectorMessage> connectorMessages = null;
                connectorMessages = dao.getConnectorMessages(channelId, messageId);

                Message message = new Message();
                message.setMessageId(messageId);
                message.setChannelId(channelId);
                message.setReceivedDate((Calendar) map.get("received_date"));
                message.setProcessed((Boolean) map.get("processed"));
                message.setServerId((String) map.get("server_id"));
                message.setOriginalId((Long) map.get("original_id"));
                message.setImportId((Long) map.get("import_id"));
                message.getConnectorMessages().putAll(connectorMessages);

                messages.add(message);

                contentMessageIds.add(messageId);

                if (messageDateThreshold != null && connectorReceivedDateMillis < messageDateThreshold.getTimeInMillis()) {
                    messageIds.add(messageId);
                }
            }
            return messages;
        } finally {
            dao.close();
        }
    }

    private long pruneChannelByIds(long localChannelId, PruneIds ids, boolean contentOnly) throws DataPrunerException, InterruptedException {
        if (!ids.hasNext()) {
            logger.debug("Skipping pruner since no messages were found to prune");
            return 0;
        }

        int numPruned = 0;

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("localChannelId", localChannelId);

        ListRangeIterator listRangeIterator = new ListRangeIterator(ids, ListRangeIterator.DEFAULT_LIST_LIMIT, true, blockSize);

        while (listRangeIterator.hasNext()) {
            ListRangeItem item = listRangeIterator.next();
            List<Long> list = item.getList();
            Long startRange = item.getStartRange();
            Long endRange = item.getEndRange();

            if (list != null || (startRange != null && endRange != null)) {
                if (list != null) {
                    logger.debug("Pruning with include list: " + list.get(0) + " " + list.get(list.size() - 1));
                    params.remove("minMessageId");
                    params.remove("maxMessageId");
                    params.put("includeMessageList", StringUtils.join(list, ","));
                } else {
                    logger.debug("Pruning with ranges: " + startRange + " - " + endRange);
                    params.remove("includeMessageList");
                    params.put("minMessageId", startRange);
                    params.put("maxMessageId", endRange);
                }

                numPruned = runDeleteQueries(params, contentOnly);
            }
        }

        return numPruned;
    }

    private int runDeleteQueries(Map<String, Object> params, boolean contentOnly) throws InterruptedException {
        int numPruned;

        if (contentOnly) {
            numPruned = runDelete("Message.pruneMessageContent", params);
        } else {
            if (DatabaseUtil.statementExists("Message.pruneAttachments")) {
                runDelete("Message.pruneAttachments", params);
            }

            if (DatabaseUtil.statementExists("Message.pruneCustomMetaData")) {
                runDelete("Message.pruneCustomMetaData", params);
            }

            runDelete("Message.pruneMessageContent", params);

            if (DatabaseUtil.statementExists("Message.pruneConnectorMessages")) {
                runDelete("Message.pruneConnectorMessages", params);
            }

            numPruned = runDelete("Message.pruneMessages", params);
        }
        return numPruned;
    }

    private int runDelete(String query, Map<String, Object> params) throws InterruptedException {
        ThreadUtils.checkInterruptedStatus();
        SqlSession session = SqlConfig.getSqlSessionManager().openSession(true);

        try {
            if (DatabaseUtil.statementExists("initDataPruner", session)) {
                session.update("initDataPruner");
            }

            status.setPruning(true);

            int count = session.delete(query, params);
            ThreadUtils.checkInterruptedStatus();
            return count;
        } finally {
            session.close();
            status.setPruning(false);
        }
    }

    private String getTimeElapsed() {
        long ms = System.currentTimeMillis() - status.getTaskStartTime().getTimeInMillis();
        long mins = ms / 60000;
        long secs = (ms % 60000) / 1000;

        return mins + " minute" + (mins == 1 ? "" : "s") + ", " + secs + " second" + (secs == 1 ? "" : "s");
    }

    private class PruneIds implements Iterator<Long> {
        private int currentIdIndex = 0;
        private int currentRangeIndex = 0;
        private long lastId = 0;

        private List<Long> ids = new ArrayList<Long>();
        private List<Long> ranges = new ArrayList<Long>();

        public void add(Long messageId) {
            int lastIdIndex = ids.size() - 1;
            int lastRangeIndex = ranges.size() - 1;

            if (!ids.isEmpty() && messageId == ids.get(lastIdIndex) + 1) {
                ids.remove(lastIdIndex);

                ranges.add(messageId - 1);
                ranges.add(messageId);
            } else if (!ranges.isEmpty() && messageId == ranges.get(lastRangeIndex) + 1) {
                ranges.set(lastRangeIndex, messageId);
            } else {
                ids.add(messageId);
            }
        }

        @Override
        public boolean hasNext() {
            return currentIdIndex < ids.size() || currentRangeIndex < ranges.size();
        }

        @Override
        public Long next() {
            if (!ranges.isEmpty() && currentRangeIndex < ranges.size()) {
                if (lastId >= ranges.get(currentRangeIndex) && lastId < ranges.get(currentRangeIndex + 1)) {
                    if (++lastId == ranges.get(currentRangeIndex + 1)) {
                        currentRangeIndex += 2;
                    }

                    return lastId;
                }

                if (currentIdIndex < ids.size() && ids.get(currentIdIndex) < ranges.get(currentRangeIndex)) {
                    return ids.get(currentIdIndex++);
                } else {
                    lastId = ranges.get(currentRangeIndex);
                    return lastId;
                }
            } else {
                return ids.get(currentIdIndex++);
            }
        }

        @Override
        public void remove() {}
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
        private boolean archiveEnabled;

        public PrunerTask(String channelId, String channelName, Calendar messageDateThreshold, Calendar contentDateThreshold, boolean archiveEnabled) {
            this.channelId = channelId;
            this.channelName = channelName;
            this.messageDateThreshold = messageDateThreshold;
            this.contentDateThreshold = contentDateThreshold;
            this.archiveEnabled = archiveEnabled;
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

        public boolean isArchiveEnabled() {
            return archiveEnabled;
        }
    }
}
