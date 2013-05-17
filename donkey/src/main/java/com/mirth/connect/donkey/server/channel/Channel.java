/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.channel.ChannelState;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.MetaDataColumnType;
import com.mirth.connect.donkey.model.channel.ResponseConnectorProperties;
import com.mirth.connect.donkey.model.event.ChannelEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.PauseException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.Startable;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.Stoppable;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.components.PostProcessor;
import com.mirth.connect.donkey.server.channel.components.PreProcessor;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.controllers.MessageController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.event.ChannelEvent;
import com.mirth.connect.donkey.server.event.DeployEvent;
import com.mirth.connect.donkey.server.event.EventDispatcher;
import com.mirth.connect.donkey.server.queue.ConnectorMessageQueue;
import com.mirth.connect.donkey.server.queue.ConnectorMessageQueueDataSource;
import com.mirth.connect.donkey.util.ThreadUtils;

public class Channel implements Startable, Stoppable, Runnable {
    private String channelId;
    private String name;
    private String serverId;
    private String version;
    private int revision;
    private Calendar deployDate;

    private boolean enabled = false;
    private ChannelState initialState;
    private ChannelState currentState = ChannelState.STOPPED;

    private StorageSettings storageSettings = new StorageSettings();
    private DonkeyDaoFactory daoFactory;
    private EventDispatcher eventDispatcher = Donkey.getInstance().getEventDispatcher();

    private AttachmentHandler attachmentHandler;
    private List<MetaDataColumn> metaDataColumns = new ArrayList<MetaDataColumn>();
    private SourceConnector sourceConnector;
    private ConnectorMessageQueue sourceQueue = new ConnectorMessageQueue();
    private FilterTransformerExecutor sourceFilterTransformerExecutor;
    private PreProcessor preProcessor;
    private PostProcessor postProcessor;
    private List<DestinationChain> destinationChains = new ArrayList<DestinationChain>();
    private ResponseSelector responseSelector;

    private ExecutorService controlExecutor = Executors.newSingleThreadExecutor();
    private ExecutorService channelExecutor;
    private ExecutorService queueExecutor;
    private ExecutorService destinationChainExecutor;
    private ExecutorService recoveryExecutor;
    private Set<Future<DispatchResult>> channelTasks = new HashSet<Future<DispatchResult>>();
    private Set<Future<?>> controlTasks = new LinkedHashSet<Future<?>>();

    private boolean stopSourceQueue = false;
    private final AtomicBoolean processLock = new AtomicBoolean(true);
    private ChannelLock lock = ChannelLock.UNLOCKED;

    private Logger logger = Logger.getLogger(getClass());

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public Calendar getDeployDate() {
        return deployDate;
    }

    public void setDeployDate(Calendar deployedDate) {
        this.deployDate = deployedDate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ChannelState getInitialState() {
        return initialState;
    }

    public void setInitialState(ChannelState initialState) {
        this.initialState = initialState;
    }

    public ChannelState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(ChannelState currentState) {
        this.currentState = currentState;
    }

    public void updateCurrentState(ChannelState state) {
        setCurrentState(state);
        Donkey.getInstance().getEventDispatcher().dispatchEvent(new ChannelEvent(channelId, ChannelEventType.getTypeFromChannelState(state)));
    }

    public StorageSettings getStorageSettings() {
        return storageSettings;
    }

    public void setStorageSettings(StorageSettings storageSettings) {
        this.storageSettings = storageSettings;
    }

    public DonkeyDaoFactory getDaoFactory() {
        return daoFactory;
    }

    public void setDaoFactory(DonkeyDaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public AttachmentHandler getAttachmentHandler() {
        return attachmentHandler;
    }

    public void setAttachmentHandler(AttachmentHandler attachmentHandler) {
        this.attachmentHandler = attachmentHandler;
    }

    public List<MetaDataColumn> getMetaDataColumns() {
        return metaDataColumns;
    }

    public void setMetaDataColumns(List<MetaDataColumn> metaDataColumns) {
        this.metaDataColumns = metaDataColumns;
    }

    public SourceConnector getSourceConnector() {
        return sourceConnector;
    }

    public void setSourceConnector(SourceConnector sourceConnector) {
        this.sourceConnector = sourceConnector;
    }

    /**
     * Get the queue that holds messages waiting to be processed
     */
    public ConnectorMessageQueue getSourceQueue() {
        return sourceQueue;
    }

    public void setSourceQueue(ConnectorMessageQueue sourceQueue) {
        this.sourceQueue = sourceQueue;
    }

    public FilterTransformerExecutor getSourceFilterTransformer() {
        return sourceFilterTransformerExecutor;
    }

    public void setSourceFilterTransformer(FilterTransformerExecutor sourceFilterTransformer) {
        this.sourceFilterTransformerExecutor = sourceFilterTransformer;
    }

    public PreProcessor getPreProcessor() {
        return preProcessor;
    }

    public void setPreProcessor(PreProcessor preProcessor) {
        this.preProcessor = preProcessor;
    }

    public PostProcessor getPostProcessor() {
        return postProcessor;
    }

    public void setPostProcessor(PostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    public void addDestinationChain(DestinationChain chain) {
        destinationChains.add(chain);
        chain.setChainId(destinationChains.size());
    }

    public List<DestinationChain> getDestinationChains() {
        return destinationChains;
    }

    public ResponseSelector getResponseSelector() {
        return responseSelector;
    }

    public void setResponseSelector(ResponseSelector responseSelector) {
        this.responseSelector = responseSelector;
    }

    public void lock(ChannelLock lock) {
        this.lock = lock;
    }

    public void unlock() {
        this.lock = ChannelLock.UNLOCKED;
    }

    public boolean isActive() {
        return currentState != ChannelState.STOPPED && currentState != ChannelState.STOPPING;
    }

    /**
     * Tell whether or not the channel is configured correctly and is able to be
     * deployed
     */
    public boolean isConfigurationValid() {
        if (channelId == null || daoFactory == null || sourceConnector == null || sourceFilterTransformerExecutor == null) {
            return false;
        }

        for (DestinationChain chain : destinationChains) {
            Map<Integer, FilterTransformerExecutor> filterTransformerExecutors = chain.getFilterTransformerExecutors();

            for (Integer metaDataId : chain.getMetaDataIds()) {
                if (filterTransformerExecutors.get(metaDataId) == null) {
                    return false;
                }
            }
        }

        return true;
    }

    public int getDestinationCount() {
        int numDestinations = 0;

        for (DestinationChain chain : destinationChains) {
            numDestinations += chain.getDestinationConnectors().size();
        }

        return numDestinations;
    }

    /**
     * Get a specific DestinationConnector by metadata id. A convenience method
     * that searches the destination chains for the destination connector with
     * the given metadata id.
     */
    public DestinationConnector getDestinationConnector(int metaDataId) {
        for (DestinationChain chain : destinationChains) {
            DestinationConnector destinationConnector = chain.getDestinationConnectors().get(metaDataId);

            if (destinationConnector != null) {
                return destinationConnector;
            }
        }

        return null;
    }

    public List<Integer> getMetaDataIds() {
        List<Integer> metaDataIds = new ArrayList<Integer>();
        metaDataIds.add(getSourceConnector().getMetaDataId());

        for (DestinationChain chain : destinationChains) {
            metaDataIds.addAll(chain.getMetaDataIds());
        }

        return metaDataIds;
    }

    public void invalidateQueues() {
        sourceQueue.invalidate(true);

        for (DestinationChain chain : destinationChains) {
            for (Integer metaDataId : chain.getMetaDataIds()) {
                chain.getDestinationConnectors().get(metaDataId).getQueue().invalidate(true);
            }
        }
    }

    public void deploy() throws DeployException {
        if (!isConfigurationValid()) {
            throw new DeployException("Failed to deploy channel. The channel configuration is incomplete.");
        }

        Future<?> task = null;

        try {
            synchronized (controlExecutor) {
                if (lock == ChannelLock.DEPLOY || lock == ChannelLock.DEBUG) {
                    task = controlExecutor.submit(new DeployTask());
                    controlTasks.add(task);
                }
            }

            if (task != null) {
                task.get();

                Statistics channelStatistics = ChannelController.getInstance().getStatistics();
                Map<Integer, Map<Status, Long>> connectorStatistics = new HashMap<Integer, Map<Status, Long>>();
                Map<Status, Long> statisticMap = new HashMap<Status, Long>(channelStatistics.getConnectorStats(channelId, 0));
                statisticMap.put(Status.QUEUED, (long) sourceQueue.size());

                connectorStatistics.put(0, statisticMap);
                for (DestinationChain chain : destinationChains) {
                    for (Integer metaDataId : chain.getMetaDataIds()) {
                        statisticMap = new HashMap<Status, Long>(channelStatistics.getConnectorStats(channelId, metaDataId));
                        statisticMap.put(Status.QUEUED, (long) chain.getDestinationConnectors().get(metaDataId).getQueue().size());

                        connectorStatistics.put(metaDataId, statisticMap);
                    }
                }

                eventDispatcher.dispatchEvent(new DeployEvent(channelId, connectorStatistics, ChannelEventType.DEPLOY));
            }
        } catch (Throwable t) {
            Throwable cause = t.getCause();
            if (cause instanceof DeployException) {
                throw (DeployException) cause;
            }

            throw new DeployException("Failed to deploy channel.", t);
        } finally {
            synchronized (controlExecutor) {
                if (task != null) {
                    controlTasks.remove(task);
                }
            }
        }
    }

    public void undeploy() throws UndeployException {
        Future<?> task = null;

        try {
            synchronized (controlExecutor) {
                if (lock == ChannelLock.UNDEPLOY || lock == ChannelLock.DEBUG) {
                    task = controlExecutor.submit(new UndeployTask());
                    controlTasks.add(task);
                }
            }

            if (task != null) {
                task.get();
                eventDispatcher.dispatchEvent(new ChannelEvent(channelId, ChannelEventType.UNDEPLOY));
            }
        } catch (Throwable t) {
            Throwable cause = t.getCause();
            if (cause instanceof UndeployException) {
                throw (UndeployException) cause;
            }

            throw new UndeployException("Failed to undeploy channel.", t);
        } finally {
            synchronized (controlExecutor) {
                if (task != null) {
                    controlTasks.remove(task);
                }
            }
        }
    }

    private void undeployConnector(Integer metaDataId) throws UndeployException {
        try {
            if (metaDataId == 0) {
                if (sourceConnector != null) {
                    sourceConnector.onUndeploy();
                }
            } else {
                DestinationConnector destinationConnector = getDestinationConnector(metaDataId);

                if (destinationConnector != null) {
                    destinationConnector.onUndeploy();
                }
            }
        } catch (UndeployException e) {
            if (metaDataId == 0) {
                logger.error("Error undeploying Source connector for channel " + name + " (" + channelId + ").", e);
            } else {
                logger.error("Error undeploying destination connector \"" + getDestinationConnector(metaDataId).getDestinationName() + "\" for channel " + name + " (" + channelId + ").", e);
            }
            throw e;
        }
    }

    @Override
    public void start() throws StartException {
        start(true);
    }

    /**
     * Start the channel and all of the channel's connectors.
     */
    public void start(boolean startSourceConnector) throws StartException {
        Future<?> task = null;

        try {
            synchronized (controlExecutor) {
                if (lock == ChannelLock.UNLOCKED || lock == ChannelLock.DEPLOY || lock == ChannelLock.DEBUG) {
                    task = controlExecutor.submit(new StartTask(startSourceConnector));
                    controlTasks.add(task);
                }
            }

            if (task != null) {
                task.get();
            }
        } catch (Throwable t) {
            Throwable cause = t.getCause();
            if (cause instanceof StartException) {
                throw (StartException) cause;
            }

            throw new StartException("Failed to start channel.", t);
        } finally {
            synchronized (controlExecutor) {
                if (task != null) {
                    controlTasks.remove(task);
                }
            }
        }
    }

    @Override
    public void stop() throws StopException {
        Future<?> task = null;

        try {
            synchronized (controlExecutor) {
                if (lock == ChannelLock.UNLOCKED || lock == ChannelLock.UNDEPLOY || lock == ChannelLock.DEBUG) {
                    task = controlExecutor.submit(new StopTask());
                    controlTasks.add(task);
                }
            }

            if (task != null) {
                task.get();
            }
        } catch (Throwable t) {
            Throwable cause = t.getCause();
            if (cause instanceof StopException) {
                throw (StopException) cause;
            }

            throw new StopException("Failed to stop channel.", t);
        } finally {
            synchronized (controlExecutor) {
                if (task != null) {
                    controlTasks.remove(task);
                }
            }
        }
    }

    public void halt() throws StopException {
        Future<?> task = null;

        try {
            synchronized (controlExecutor) {
                Object[] tasks = controlTasks.toArray();

                // Cancel tasks in reverse order
                for (int i = tasks.length - 1; i >= 0; i--) {
                    ((Future<?>) tasks[i]).cancel(true);
                }

                if (recoveryExecutor != null) {
                    recoveryExecutor.shutdownNow();
                }

                // These executors must be shutdown here in order to terminate a stop task.
                // They are also shutdown in the halt task itself in case a start task started them after this point.
                destinationChainExecutor.shutdownNow();
                channelExecutor.shutdownNow();
                queueExecutor.shutdownNow();

                task = controlExecutor.submit(new HaltTask());
                controlTasks.add(task);
            }

            task.get();
        } catch (Throwable t) {
            Throwable cause = t.getCause();
            if (cause instanceof StopException) {
                throw (StopException) cause;
            }

            throw new StopException("Failed to halt channel.", t);
        } finally {
            synchronized (controlExecutor) {
                if (task != null) {
                    controlTasks.remove(task);
                }
            }
        }
    }

    public void pause() throws PauseException {
        Future<?> task = null;

        try {
            synchronized (controlExecutor) {
                if (lock == ChannelLock.UNLOCKED || lock == ChannelLock.DEBUG) {
                    task = controlExecutor.submit(new PauseTask());
                    controlTasks.add(task);
                }
            }

            if (task != null) {
                task.get();
            }
        } catch (Throwable t) {
            Throwable cause = t.getCause();
            if (cause instanceof PauseException) {
                throw (PauseException) cause;
            }

            throw new PauseException("Failed to pause channel.", t);
        } finally {
            synchronized (controlExecutor) {
                if (task != null) {
                    controlTasks.remove(task);
                }
            }
        }
    }

    public void resume() throws StartException {
        Future<?> task = null;

        try {
            synchronized (controlExecutor) {
                if (lock == ChannelLock.UNLOCKED || lock == ChannelLock.DEBUG) {
                    task = controlExecutor.submit(new ResumeTask());
                    controlTasks.add(task);
                }
            }

            if (task != null) {
                task.get();
            }
        } catch (Throwable t) {
            Throwable cause = t.getCause();
            if (cause instanceof StartException) {
                throw (StartException) cause;
            }

            throw new StartException("Failed to resume channel.", t);
        } finally {
            synchronized (controlExecutor) {
                if (task != null) {
                    controlTasks.remove(task);
                }
            }
        }
    }

    private void stop(List<Integer> metaDataIds) throws StopException, InterruptedException {
        stopSourceQueue = true;
        Throwable firstCause = null;

        // If an exception occurs, then still proceed by stopping the rest of the connectors
        for (Integer metaDataId : metaDataIds) {
            ThreadUtils.checkInterruptedStatus();

            try {
                stopConnector(metaDataId);
            } catch (Throwable t) {
                if (firstCause == null) {
                    firstCause = t;
                }
            }
        }

        ThreadUtils.checkInterruptedStatus();

        channelExecutor.shutdown();
        queueExecutor.shutdown();

        final int timeout = 10;

        while (!channelExecutor.awaitTermination(timeout, TimeUnit.SECONDS))
            ;
        while (!queueExecutor.awaitTermination(timeout, TimeUnit.SECONDS))
            ;

        /*
         * Once the channel executor finishes, we want to obtain the process lock to ensure that any
         * calls to finishDispatch have already completed.
         */
        obtainProcessLock();
        releaseProcessLock();

        destinationChainExecutor.shutdown();

        if (firstCause != null) {
            updateCurrentState(ChannelState.STOPPED);
            throw new StopException("Failed to stop channel " + name + " (" + channelId + "): One or more connectors failed to stop.", firstCause);
        }
    }

    private void halt(List<Integer> metaDataIds) throws StopException, InterruptedException {
        stopSourceQueue = true;

        destinationChainExecutor.shutdownNow();
        channelExecutor.shutdownNow();
        queueExecutor.shutdownNow();

        // Cancel any tasks that were submitting and not yet finished
        synchronized (channelTasks) {
            for (Future<DispatchResult> channelTask : channelTasks) {
                channelTask.cancel(true);
            }
        }

        Throwable firstCause = null;

        // If an exception occurs, then still proceed by stopping the rest of the connectors
        for (Integer metaDataId : metaDataIds) {
            try {
                haltConnector(metaDataId);
            } catch (Throwable t) {
                if (firstCause == null) {
                    firstCause = t;
                }
            }
        }

        if (firstCause != null) {
            updateCurrentState(ChannelState.STOPPED);
            throw new StopException("Failed to stop channel " + name + " (" + channelId + "): One or more connectors failed to stop.", firstCause);
        }
    }

    private void stopConnector(Integer metaDataId) throws StopException {
        try {
            if (metaDataId == 0) {
                sourceConnector.stop();
            } else {
                getDestinationConnector(metaDataId).stop();
            }
        } catch (StopException e) {
            if (metaDataId == 0) {
                logger.error("Error stopping Source connector for channel " + name + " (" + channelId + ").", e);
            } else {
                logger.error("Error stopping destination connector \"" + getDestinationConnector(metaDataId).getDestinationName() + "\" for channel " + name + " (" + channelId + ").", e);
            }
            throw e;
        }
    }

    private void haltConnector(Integer metaDataId) throws StopException {
        try {
            if (metaDataId == 0) {
                sourceConnector.halt();
            } else {
                getDestinationConnector(metaDataId).halt();
            }
        } catch (StopException e) {
            if (metaDataId == 0) {
                logger.error("Error stopping Source connector for channel " + name + " (" + channelId + ").", e);
            } else {
                logger.error("Error stopping destination connector \"" + getDestinationConnector(metaDataId).getDestinationName() + "\" for channel " + name + " (" + channelId + ").", e);
            }
            throw e;
        }
    }

    protected DispatchResult dispatchRawMessage(RawMessage rawMessage) throws ChannelException {
        if (currentState == ChannelState.STOPPING || currentState == ChannelState.STOPPED) {
            throw new ChannelException(true);
        }

        MessageTask task = new MessageTask(rawMessage, this);

        Future<DispatchResult> future = null;
        try {

            synchronized (channelTasks) {
                future = channelExecutor.submit(task);
                channelTasks.add(future);
            }
            DispatchResult messageResponse = future.get();

            return messageResponse;
        } catch (RejectedExecutionException e) {
            throw new ChannelException(true, e);
        } catch (InterruptedException e) {
            // This exception should only ever be thrown during a halt.
            // It is impossible to know whether or not the message was persisted because the task will continue to run
            // even though we are no longer waiting for it. Furthermore it is possible the message was actually sent.

            // The best we can do is cancel the task and throw a channel exception. 
            // If the message was not queued on the source connector, recovery should take care of it.
            // If the message was queued, the source of the message will be notified that the message was not persisted to be safe.
            // This could lead to a potential duplicate message being received/sent, but it is one of the consequences of using halt.
            future.cancel(true);

            throw new ChannelException(true, e);
        } catch (Throwable e) {
            Throwable cause = e.getCause();
            ChannelException channelException = null;

            if (cause instanceof InterruptedException) {
                channelException = new ChannelException(true, cause);
            } else if (cause instanceof ChannelException) {
                logger.error("Runtime error in channel.", cause);
                channelException = (ChannelException) cause;
            } else {
                logger.error("Error processing message.", e);
                channelException = new ChannelException(false, e);
            }

            if (task.getPersistedMessageId() == null) {
                throw channelException;
            }

            return new DispatchResult(task.getPersistedMessageId(), null, null, false, false, false, task.isLockAcquired(), channelException);
        } finally {
            if (future != null) {
                synchronized (channelTasks) {
                    channelTasks.remove(future);
                }
            }
        }
    }

    public void obtainProcessLock() throws InterruptedException {
        synchronized (processLock) {
            while (processLock.get()) {
                processLock.wait();
            }

            processLock.set(true);
        }
    }

    public void releaseProcessLock() {
        synchronized (processLock) {
            processLock.set(false);
            processLock.notifyAll();
        }
    }

    /**
     * Queues a source message for processing
     */
    protected void queue(ConnectorMessage sourceMessage) {
        sourceQueue.add(sourceMessage);
    }

    /**
     * Process a source message and return the final processed composite message
     * once all destinations have completed and the post-processor has executed
     * 
     * @param sourceMessage
     *            A source connector message
     * @return The final processed composite message containing the source
     *         message and all destination messages
     * @throws InterruptedException
     */
    protected Message process(ConnectorMessage sourceMessage, boolean markAsProcessed) throws InterruptedException {
        ThreadUtils.checkInterruptedStatus();
        long messageId = sourceMessage.getMessageId();

        if (sourceMessage.getMetaDataId() != 0 || sourceMessage.getStatus() != Status.RECEIVED) {
            throw new RuntimeException("Received a source message with an invalid state");
        }

        // create a final merged message that will contain the merged maps from each destination chain's processed message
        Message finalMessage = new Message();
        finalMessage.setMessageId(messageId);
        finalMessage.setServerId(serverId);
        finalMessage.setChannelId(channelId);
        finalMessage.setReceivedDate(sourceMessage.getReceivedDate());
        finalMessage.getConnectorMessages().put(0, sourceMessage);

        // run the raw message through the pre-processor script
        String processedRawContent = null;

        ThreadUtils.checkInterruptedStatus();

        try {
            processedRawContent = preProcessor.doPreProcess(sourceMessage);
        } catch (DonkeyException e) {
            sourceMessage.setStatus(Status.ERROR);
            sourceMessage.setProcessingError(e.getFormattedError());
        }

        /*
         * TRANSACTION: Process Source
         * - store processed raw content
         * - update the source status
         * - store transformed content
         * - store encoded content
         * - update source maps
         * - create connector messages for each destination chain with RECEIVED
         * status and maps
         */
        ThreadUtils.checkInterruptedStatus();
        DonkeyDao dao = daoFactory.getDao();

        try {
            if (sourceMessage.getStatus() == Status.ERROR) {
                dao.updateStatus(sourceMessage, Status.RECEIVED);

                if (StringUtils.isNotBlank(sourceMessage.getProcessingError())) {
                    dao.updateErrors(sourceMessage);
                }

                ThreadUtils.checkInterruptedStatus();
                dao.commit(storageSettings.isDurable());
                dao.close();
                finishMessage(finalMessage, markAsProcessed);
                return finalMessage;
            }

            if (processedRawContent != null) {
                // store the processed raw content
                sourceMessage.setProcessedRaw(new MessageContent(channelId, messageId, 0, ContentType.PROCESSED_RAW, processedRawContent, sourceConnector.getInboundDataType().getType(), false));
            }

            // send the message to the source filter/transformer and then update it's status
            try {
                sourceFilterTransformerExecutor.processConnectorMessage(sourceMessage);
            } catch (DonkeyException e) {
                sourceMessage.setStatus(Status.ERROR);
                sourceMessage.setProcessingError(e.getFormattedError());
            }

            dao.updateStatus(sourceMessage, Status.RECEIVED);

            // Set the source connector's custom column map
            sourceConnector.getMetaDataReplacer().setMetaDataMap(sourceMessage, metaDataColumns);

            // Store the custom columns
            if (!sourceMessage.getMetaDataMap().isEmpty() && storageSettings.isStoreCustomMetaData()) {
                ThreadUtils.checkInterruptedStatus();
                dao.insertMetaData(sourceMessage, metaDataColumns);
            }

            // if the message was filtered or an error occurred, then finish
            if (sourceMessage.getStatus() != Status.TRANSFORMED) {
                if (storageSettings.isStoreProcessedRaw() && sourceMessage.getProcessedRaw() != null) {
                    ThreadUtils.checkInterruptedStatus();
                    dao.insertMessageContent(sourceMessage.getProcessedRaw());
                }

                if (storageSettings.isStoreTransformed() && sourceMessage.getTransformed() != null) {
                    dao.insertMessageContent(sourceMessage.getTransformed());
                }

                if (StringUtils.isNotBlank(sourceMessage.getProcessingError())) {
                    dao.updateErrors(sourceMessage);
                }

                ThreadUtils.checkInterruptedStatus();
                dao.commit();
                dao.close();

                finishMessage(finalMessage, markAsProcessed);
                return finalMessage;
            }

            // store the raw, transformed and encoded content
            boolean insertedContent = false;
            ThreadUtils.checkInterruptedStatus();

            if (storageSettings.isStoreProcessedRaw() && sourceMessage.getProcessedRaw() != null) {
                dao.batchInsertMessageContent(sourceMessage.getProcessedRaw());
                insertedContent = true;
            }

            if (storageSettings.isStoreTransformed() && sourceMessage.getTransformed() != null) {
                dao.batchInsertMessageContent(sourceMessage.getTransformed());
                insertedContent = true;
            }

            if (storageSettings.isStoreSourceEncoded() && sourceMessage.getEncoded() != null) {
                dao.batchInsertMessageContent(sourceMessage.getEncoded());
                insertedContent = true;
            }

            if (insertedContent) {
                dao.executeBatchInsertMessageContent(channelId);
            }

            if (storageSettings.isStoreMaps()) {
                ThreadUtils.checkInterruptedStatus();

                // update the message maps generated by the filter/transformer
                dao.updateMaps(sourceMessage);
            }

            // create a message for each destination chain
            Map<Integer, ConnectorMessage> destinationMessages = new HashMap<Integer, ConnectorMessage>();
            MessageContent sourceEncoded = sourceMessage.getEncoded();

            // get the list of destination meta data ids to send to
            List<Integer> metaDataIds = null;

            if (sourceMessage.getChannelMap().containsKey(Constants.DESTINATION_META_DATA_IDS_KEY)) {
                metaDataIds = (List<Integer>) sourceMessage.getChannelMap().get(Constants.DESTINATION_META_DATA_IDS_KEY);
            }

            for (DestinationChain chain : destinationChains) {
                // The order of the enabledMetaDataId list needs to be based on the chain order.
                // We do not use ListUtils here because there is no official guarantee of order.
                if (metaDataIds != null && metaDataIds.size() > 0) {
                    List<Integer> enabledMetaDataIds = new ArrayList<Integer>();
                    for (Integer id : chain.getMetaDataIds()) {
                        if (metaDataIds.contains(id)) {
                            enabledMetaDataIds.add(id);
                        }
                    }
                    chain.setEnabledMetaDataIds(enabledMetaDataIds);
                }

                // if any destinations in this chain are enabled, create messages for them
                if (!chain.getEnabledMetaDataIds().isEmpty()) {
                    ThreadUtils.checkInterruptedStatus();
                    Integer metaDataId = chain.getEnabledMetaDataIds().get(0);

                    DestinationConnector destinationConnector = chain.getDestinationConnectors().get(metaDataId);

                    // create the raw content from the source's encoded content
                    MessageContent raw = new MessageContent(channelId, messageId, metaDataId, ContentType.RAW, sourceEncoded.getContent(), destinationConnector.getInboundDataType().getType(), sourceEncoded.isEncrypted());

                    // create the message and set the raw content
                    ConnectorMessage message = new ConnectorMessage(channelId, messageId, metaDataId, sourceMessage.getServerId(), Calendar.getInstance(), Status.RECEIVED);
                    message.setConnectorName(destinationConnector.getDestinationName());
                    message.setChainId(chain.getChainId());
                    message.setOrderId(destinationConnector.getOrderId());

                    message.setChannelMap(new HashMap<String, Object>(sourceMessage.getChannelMap()));
                    message.setResponseMap(new HashMap<String, Object>(sourceMessage.getResponseMap()));
                    message.setRaw(raw);

                    // store the new message, but we don't need to store the content because we will reference the source's encoded content
                    dao.insertConnectorMessage(message, storageSettings.isStoreMaps());

                    destinationMessages.put(metaDataId, message);
                }
            }

            ThreadUtils.checkInterruptedStatus();
            dao.commit();
            dao.close();

            /*
             * Construct a list of only the enabled destination chains. This is done because we
             * don't know beforehand which destination chain will be the "last" one.
             */
            List<DestinationChain> enabledChains = new ArrayList<DestinationChain>();
            for (DestinationChain chain : destinationChains) {
                if (!chain.getEnabledMetaDataIds().isEmpty()) {
                    chain.setMessage(destinationMessages.get(chain.getEnabledMetaDataIds().get(0)));
                    enabledChains.add(chain);
                }
            }

            // Execute each destination chain (but the last one) and store the tasks in a list
            List<Future<List<ConnectorMessage>>> destinationChainTasks = new ArrayList<Future<List<ConnectorMessage>>>();

            for (int i = 0; i <= enabledChains.size() - 2; i++) {
                try {
                    destinationChainTasks.add(destinationChainExecutor.submit(enabledChains.get(i)));
                } catch (RejectedExecutionException e) {
                    Thread.currentThread().interrupt();
                    throw new InterruptedException();
                }
            }

            List<ConnectorMessage> connectorMessages = null;

            // Always call the last chain directly rather than submitting it as a Future
            try {
                connectorMessages = enabledChains.get(enabledChains.size() - 1).call();
            } catch (Throwable t) {
                handleDestinationChainThrowable(t);
            }

            addConnectorMessages(finalMessage, sourceMessage, connectorMessages);

            // Get the result message from each destination chain's task and merge the map data into the final merged message. If an exception occurs, return immediately without sending the message to the post-processor.
            for (Future<List<ConnectorMessage>> task : destinationChainTasks) {
                connectorMessages = null;

                try {
                    connectorMessages = task.get();
                } catch (Exception e) {
                    handleDestinationChainThrowable(e);
                }

                addConnectorMessages(finalMessage, sourceMessage, connectorMessages);
            }

            // re-enable all destination connectors in each chain
            if (metaDataIds != null) {
                for (DestinationChain chain : destinationChains) {
                    chain.getEnabledMetaDataIds().clear();
                    chain.getEnabledMetaDataIds().addAll(chain.getMetaDataIds());
                }
            }

            finishMessage(finalMessage, markAsProcessed);
            return finalMessage;
        } finally {
            if (!dao.isClosed()) {
                dao.close();
            }
        }
    }

    private void addConnectorMessages(Message finalMessage, ConnectorMessage sourceMessage, List<ConnectorMessage> connectorMessages) {
        /*
         * Check for null here in case DestinationChain.call() returned null, which
         * indicates that the chain did not process and should be skipped. This would only
         * happen in very rare circumstances, possibly if a message is sent to the chain and
         * the destination connector that the message belongs to has been removed or
         * disabled.
         */
        if (connectorMessages != null) {
            for (ConnectorMessage connectorMessage : connectorMessages) {
                finalMessage.getConnectorMessages().put(connectorMessage.getMetaDataId(), connectorMessage);
                sourceMessage.getResponseMap().putAll(connectorMessage.getResponseMap());
            }
        }
    }

    private void handleDestinationChainThrowable(Throwable t) throws OutOfMemoryError, InterruptedException {
        Throwable cause;
        if (t instanceof ExecutionException) {
            cause = t.getCause();
        } else {
            cause = t;
        }

        // TODO: make sure we are catching out of memory errors correctly here
        if (cause.getMessage().contains("Java heap space")) {
            logger.error(cause.getMessage(), cause);
            throw new OutOfMemoryError();
        }

        if (cause instanceof CancellationException) {
            Thread.currentThread().interrupt();
            throw new InterruptedException();
        } else if (cause instanceof InterruptedException) {
            Thread.currentThread().interrupt();
            throw (InterruptedException) cause;
        }

        throw new RuntimeException(cause);
    }

    /**
     * Process all unfinished messages found in storage
     */
    protected List<Message> processUnfinishedMessages() throws Exception {
        recoveryExecutor = Executors.newSingleThreadExecutor();

        try {
            return recoveryExecutor.submit(new RecoveryTask(this)).get();
        } finally {
            recoveryExecutor.shutdown();
        }
    }

    @Override
    public void run() {
        try {
            do {
                processSourceQueue(Constants.SOURCE_QUEUE_POLL_TIMEOUT_MILLIS);
            } while (isActive());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void processSourceQueue(int timeout) throws InterruptedException {
        ThreadUtils.checkInterruptedStatus();
        ConnectorMessage sourceMessage = sourceQueue.poll(timeout, TimeUnit.MILLISECONDS);

        while (sourceMessage != null && !stopSourceQueue) {
            process(sourceMessage, true);
            sourceMessage = sourceQueue.poll();
        }
    }

    public void finishMessage(Message finalMessage, boolean markAsProcessed) throws InterruptedException {
        ThreadUtils.checkInterruptedStatus();
        Response response = null;
        boolean storePostProcessorError = false;

        try {
            response = postProcessor.doPostProcess(finalMessage);
        } catch (Exception e) {
            logger.error("Error executing postprocessor for channel " + finalMessage.getChannelId() + ".", e);
            finalMessage.getConnectorMessages().get(0).setPostProcessorError(e.getMessage());
            storePostProcessorError = true;
        }

        if (response != null) {
            finalMessage.getConnectorMessages().get(0).getResponseMap().put(ResponseConnectorProperties.RESPONSE_POST_PROCESSOR, response);
        }

        /*
         * TRANSACTION: Post Process and Complete
         * - store the merged response map as the source connector's response
         * map
         * - mark the message as processed
         */
        ThreadUtils.checkInterruptedStatus();
        DonkeyDao dao = daoFactory.getDao();

        if (storePostProcessorError) {
            dao.updateErrors(finalMessage.getConnectorMessages().get(0));
        }

        if (markAsProcessed) {
            if (storageSettings.isStoreMergedResponseMap()) {
                ThreadUtils.checkInterruptedStatus();
                dao.updateResponseMap(finalMessage.getConnectorMessages().get(0));
            }

            dao.markAsProcessed(channelId, finalMessage.getMessageId());
            finalMessage.setProcessed(true);

            boolean messageCompleted = MessageController.getInstance().isMessageCompleted(finalMessage);
            if (messageCompleted) {
                if (storageSettings.isRemoveContentOnCompletion()) {
                    dao.deleteMessageContent(channelId, finalMessage.getMessageId());
                }

                if (storageSettings.isRemoveAttachmentsOnCompletion()) {
                    dao.deleteMessageAttachments(channelId, finalMessage.getMessageId());
                }
            }
        }

        dao.commit(storageSettings.isDurable());
        dao.close();
    }

    public void importMessage(Message message) throws DonkeyException {
        DonkeyDao dao = null;

        try {
            dao = daoFactory.getDao();
            importMessage(message, dao);
            dao.commit();
        } finally {
            dao.close();
        }
    }

    public void importMessage(Message message, DonkeyDao dao) throws DonkeyException {
        if (message.getImportId() == null) {
            message.setImportId(message.getMessageId());
        }

        if (message.getImportChannelId() == null && !message.getChannelId().equals(channelId)) {
            message.setImportChannelId(message.getChannelId());
        }

        long messageId = dao.getNextMessageId(channelId);
        message.setMessageId(messageId);
        message.setChannelId(channelId);
        message.setServerId(serverId);

        dao.insertMessage(message);

        for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
            connectorMessage.setMessageId(messageId);
            connectorMessage.setChannelId(channelId);
            connectorMessage.setServerId(serverId);

            int metaDataId = connectorMessage.getMetaDataId();

            dao.insertConnectorMessage(connectorMessage, true);

            // TODO insert custom metadata

            for (ContentType contentType : ContentType.getMessageTypes()) {
                MessageContent messageContent = connectorMessage.getMessageContent(contentType);

                if (messageContent != null) {
                    messageContent.setMessageId(messageId);
                    messageContent.setChannelId(channelId);
                }
            }

            if (storageSettings.isStoreRaw() && connectorMessage.getRaw() != null) {
                dao.insertMessageContent(connectorMessage.getRaw());
            }

            if (storageSettings.isStoreProcessedRaw() && connectorMessage.getProcessedRaw() != null) {
                dao.insertMessageContent(connectorMessage.getProcessedRaw());
            }

            if (storageSettings.isStoreTransformed() && connectorMessage.getTransformed() != null) {
                dao.insertMessageContent(connectorMessage.getTransformed());
            }

            if (storageSettings.isStoreSourceEncoded() && metaDataId == 0 && connectorMessage.getEncoded() != null) {
                dao.insertMessageContent(connectorMessage.getEncoded());
            }

            if (storageSettings.isStoreDestinationEncoded() && metaDataId > 0 && connectorMessage.getEncoded() != null) {
                dao.insertMessageContent(connectorMessage.getEncoded());
            }

            if (storageSettings.isStoreSent() && connectorMessage.getSent() != null) {
                dao.insertMessageContent(connectorMessage.getSent());
            }

            if (storageSettings.isStoreResponse() && connectorMessage.getResponse() != null) {
                dao.insertMessageContent(connectorMessage.getResponse());
            }

            if (storageSettings.isStoreResponseTransformed() && connectorMessage.getResponseTransformed() != null) {
                dao.insertMessageContent(connectorMessage.getResponseTransformed());
            }

            if (storageSettings.isStoreProcessedResponse() && connectorMessage.getProcessedResponse() != null) {
                dao.insertMessageContent(connectorMessage.getProcessedResponse());
            }

            // TODO insert attachments?
        }
    }

    private class DeployTask implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            ChannelController.getInstance().initChannelStorage(channelId);

            /*
             * Before deploying, make sure the connector is deployable. Verify that if queueing is
             * enabled, the current storage settings support it.
             */
            if (!sourceConnector.isRespondAfterProcessing() && (!storageSettings.isEnabled() || !storageSettings.isStoreRaw() || !storageSettings.isStoreMaps())) {
                throw new DeployException("Failed to deploy channel " + name + " (" + channelId + "): the source connector has queueing enabled, but the current storage settings do not support queueing on the source connector.");
            }

            for (DestinationChain chain : destinationChains) {
                for (Integer metaDataId : chain.getMetaDataIds()) {
                    DestinationConnector destinationConnector = chain.getDestinationConnectors().get(metaDataId);

                    if (destinationConnector.isQueueEnabled() && (!storageSettings.isEnabled() || !storageSettings.isStoreSourceEncoded() || !storageSettings.isStoreSent() || !storageSettings.isStoreMaps())) {
                        throw new DeployException("Failed to deploy channel " + name + " (" + channelId + "): one or more destination connectors have queueing enabled, but the current storage settings do not support queueing on destination connectors.");
                    }
                }
            }

            try {
                updateMetaDataColumns();
            } catch (SQLException e) {
                throw new DeployException("Failed to deploy channel " + name + " (" + channelId + "): Unable to update custom metadata columns.");
            }

            List<Integer> deployedMetaDataIds = new ArrayList<Integer>();

            // Call the connector onDeploy() methods so they can run their onDeploy logic
            try {
                if (responseSelector == null) {
                    responseSelector = new ResponseSelector(sourceConnector.getInboundDataType());
                }

                // set the source queue data source
                sourceQueue.setDataSource(new ConnectorMessageQueueDataSource(channelId, 0, Status.RECEIVED, false, daoFactory));

                // manually refresh the source queue size from it's data source
                sourceQueue.updateSize();

                deployedMetaDataIds.add(0);
                sourceConnector.onDeploy();

                for (DestinationChain chain : destinationChains) {
                    chain.setDaoFactory(daoFactory);
                    chain.setStorageSettings(storageSettings);

                    for (Integer metaDataId : chain.getMetaDataIds()) {
                        DestinationConnector destinationConnector = chain.getDestinationConnectors().get(metaDataId);
                        destinationConnector.setDaoFactory(daoFactory);
                        destinationConnector.setStorageSettings(storageSettings);

                        // set the queue data source
                        destinationConnector.getQueue().setDataSource(new ConnectorMessageQueueDataSource(getChannelId(), destinationConnector.getMetaDataId(), Status.QUEUED, destinationConnector.isQueueRotate(), daoFactory));

                        // refresh the queue size from it's data source
                        destinationConnector.getQueue().updateSize();

                        deployedMetaDataIds.add(metaDataId);
                        destinationConnector.onDeploy();
                    }
                }
            } catch (Throwable t) {
                // If an exception occurred, then attempt to rollback by undeploying all the connectors that were deployed
                for (Integer metaDataId : deployedMetaDataIds) {
                    try {
                        undeployConnector(metaDataId);
                    } catch (UndeployException e2) {
                    }
                }

                throw new DeployException(t);
            }

            responseSelector.setNumDestinations(getDestinationCount());

            return null;
        }

        private void updateMetaDataColumns() throws SQLException {
            DonkeyDao dao = daoFactory.getDao();

            try {
                Map<String, MetaDataColumnType> existingColumnsMap = new HashMap<String, MetaDataColumnType>();
                List<String> columnsToRemove = new ArrayList<String>();
                List<MetaDataColumn> existingColumns = dao.getMetaDataColumns(channelId);

                for (MetaDataColumn existingColumn : existingColumns) {
                    existingColumnsMap.put(existingColumn.getName(), existingColumn.getType());
                    columnsToRemove.add(existingColumn.getName());
                }

                for (MetaDataColumn column : metaDataColumns) {
                    String columnName = column.getName();

                    if (existingColumnsMap.containsKey(columnName)) {
                        // The column name already exists in the table
                        if (existingColumnsMap.get(columnName) != column.getType()) {
                            // The column name is in the table, but the column type has changed
                            dao.removeMetaDataColumn(channelId, columnName);
                            dao.addMetaDataColumn(channelId, column);
                        }
                    } else {
                        // The column name does not exist in the table
                        dao.addMetaDataColumn(channelId, column);
                    }

                    columnsToRemove.remove(columnName);
                }

                for (String columnToRemove : columnsToRemove) {
                    dao.removeMetaDataColumn(channelId, columnToRemove);
                }

                dao.commit();
            } finally {
                dao.close();
            }
        }
    }

    private class UndeployTask implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            // Call the connector onUndeploy() methods so they can run their onUndeploy logic
            Throwable firstCause = null;

            List<Integer> deployedMetaDataIds = new ArrayList<Integer>();
            deployedMetaDataIds.add(0);

            for (DestinationChain chain : destinationChains) {
                for (Integer metaDataId : chain.getMetaDataIds()) {
                    deployedMetaDataIds.add(metaDataId);
                }
            }

            // If an exception occurs, then still proceed by undeploying the rest of the connectors
            for (Integer metaDataId : deployedMetaDataIds) {
                try {
                    undeployConnector(metaDataId);
                } catch (Throwable t) {
                    if (firstCause == null) {
                        firstCause = t;
                    }
                }
            }

            if (firstCause != null) {
                throw new UndeployException("Failed to undeploy channel " + name + " (" + channelId + "): One or more connectors failed to undeploy.", firstCause);
            }

            return null;
        }
    }

    private class StartTask implements Callable<Void> {

        private boolean startSourceConnector;

        public StartTask(boolean startSourceConnector) {
            this.startSourceConnector = startSourceConnector;
        }

        @Override
        public Void call() throws Exception {
            if (currentState != ChannelState.STARTED) {
                // Prevent the channel for being started while messages are being deleted.
                synchronized (Channel.this) {
                    updateCurrentState(ChannelState.STARTING);
                    processLock.set(false);
                    channelTasks.clear();
                    stopSourceQueue = false;

                    // Remove any items in the queue's buffer because they may be outdated and refresh the queue size.
                    sourceQueue.invalidate(true);

                    // enable all destination connectors in each chain
                    for (DestinationChain chain : destinationChains) {
                        chain.getEnabledMetaDataIds().clear();
                        chain.getEnabledMetaDataIds().addAll(chain.getMetaDataIds());
                    }
                    List<Integer> startedMetaDataIds = new ArrayList<Integer>();

                    try {
                        destinationChainExecutor = Executors.newCachedThreadPool();
                        queueExecutor = Executors.newSingleThreadExecutor();
                        channelExecutor = Executors.newSingleThreadExecutor();

                        // start the destination connectors
                        for (DestinationChain chain : destinationChains) {
                            for (Integer metaDataId : chain.getMetaDataIds()) {
                                DestinationConnector destinationConnector = chain.getDestinationConnectors().get(metaDataId);

                                if (!destinationConnector.isRunning()) {
                                    startedMetaDataIds.add(metaDataId);
                                    destinationConnector.start();
                                }
                            }
                        }

                        ThreadUtils.checkInterruptedStatus();
                        try {
                            processUnfinishedMessages();
                        } catch (InterruptedException e) {
                            logger.error("Startup recovery interrupted");
                            Thread.currentThread().interrupt();
                        } catch (Exception e) {
                            logger.error("Startup recovery failed");
                        }

                        ThreadUtils.checkInterruptedStatus();
                        // start up the worker thread that will process queued messages
                        if (!sourceConnector.isRespondAfterProcessing()) {
                            queueExecutor.execute(Channel.this);
                        }

                        if (startSourceConnector) {
                            ThreadUtils.checkInterruptedStatus();
                            // start up the source connector
                            if (sourceConnector.getCurrentState() == ChannelState.STOPPED) {
                                startedMetaDataIds.add(0);
                                sourceConnector.start();
                            }

                            updateCurrentState(ChannelState.STARTED);
                        } else {
                            updateCurrentState(ChannelState.PAUSED);
                        }
                    } catch (Throwable t) {
                        // If an exception occurred, then attempt to rollback by stopping all the connectors that were started
                        try {
                            stop(startedMetaDataIds);
                            updateCurrentState(ChannelState.STOPPED);
                        } catch (Throwable t2) {
                            updateCurrentState(ChannelState.STOPPED);
                        }

                        throw new StartException(t);
                    }
                }
            } else {
                logger.warn("Failed to start channel " + name + " (" + channelId + "): The channel is already running.");
            }

            return null;
        }
    }

    private class StopTask implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            if (currentState != ChannelState.STOPPED) {
                updateCurrentState(ChannelState.STOPPING);
                List<Integer> deployedMetaDataIds = new ArrayList<Integer>();
                deployedMetaDataIds.add(0);

                for (DestinationChain chain : destinationChains) {
                    for (Integer metaDataId : chain.getMetaDataIds()) {
                        deployedMetaDataIds.add(metaDataId);
                    }
                }

                stop(deployedMetaDataIds);
                updateCurrentState(ChannelState.STOPPED);
            } else {
                logger.warn("Failed to stop channel " + name + " (" + channelId + "): The channel is already stopped.");
            }

            return null;
        }
    }

    private class HaltTask implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            if (currentState != ChannelState.STOPPED) {
                updateCurrentState(ChannelState.STOPPING);
                List<Integer> deployedMetaDataIds = new ArrayList<Integer>();
                deployedMetaDataIds.add(0);

                for (DestinationChain chain : destinationChains) {
                    for (Integer metaDataId : chain.getMetaDataIds()) {
                        deployedMetaDataIds.add(metaDataId);
                    }
                }

                halt(deployedMetaDataIds);
                updateCurrentState(ChannelState.STOPPED);
            } else {
                logger.warn("Failed to stop channel " + name + " (" + channelId + "): The channel is already stopped.");
            }

            return null;
        }
    }

    private class PauseTask implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            if (currentState == ChannelState.STARTED) {
                try {
                    updateCurrentState(ChannelState.PAUSING);
                    sourceConnector.stop();
                    updateCurrentState(ChannelState.PAUSED);
                } catch (Throwable t) {
                    throw new PauseException("Failed to pause channel " + name + " (" + channelId + ").", t);
                }
            } else {
                //TODO what to do here?
                if (currentState == ChannelState.PAUSED) {
                    logger.warn("Failed to pause channel " + name + " (" + channelId + "): The channel is already paused.");
                } else {
                    logger.warn("Failed to pause channel " + name + " (" + channelId + "): The channel is currently " + currentState.toString().toLowerCase() + " and cannot be paused.");
                }

            }

            return null;
        }

    }

    private class ResumeTask implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            if (currentState == ChannelState.PAUSED) {
                try {
                    updateCurrentState(ChannelState.STARTING);
                    sourceConnector.start();
                    updateCurrentState(ChannelState.STARTED);
                } catch (Throwable t) {
                    try {
                        sourceConnector.stop();
                        updateCurrentState(ChannelState.PAUSED);
                    } catch (Throwable e2) {
                    }

                    throw new StartException("Failed to resume channel " + name + " (" + channelId + ").", t);
                }
            } else {
                logger.warn("Failed to resume channel " + name + " (" + channelId + "): The source connector is not currently paused.");
            }

            return null;
        }

    }
}
