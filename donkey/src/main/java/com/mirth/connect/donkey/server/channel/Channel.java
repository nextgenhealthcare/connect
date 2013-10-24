/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.io.IOException;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.MetaDataColumnType;
import com.mirth.connect.donkey.model.channel.ResponseConnectorProperties;
import com.mirth.connect.donkey.model.event.DeployedStateEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.XmlSerializerException;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.model.message.attachment.AttachmentException;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.HaltException;
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
import com.mirth.connect.donkey.server.event.DeployedStateEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.donkey.server.event.EventDispatcher;
import com.mirth.connect.donkey.server.queue.ConnectorMessageQueue;
import com.mirth.connect.donkey.server.queue.ConnectorMessageQueueDataSource;
import com.mirth.connect.donkey.util.Base64Util;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.donkey.util.ThreadUtils;

public class Channel implements Startable, Stoppable, Runnable {
    private String channelId;
    private long localChannelId;
    private String name;
    private String serverId;
    private int revision;
    private Calendar deployDate;

    private boolean enabled = false;
    private DeployedState initialState;
    private DeployedState currentState = DeployedState.STOPPED;

    private StorageSettings storageSettings = new StorageSettings();
    private DonkeyDaoFactory daoFactory;
    private EventDispatcher eventDispatcher = Donkey.getInstance().getEventDispatcher();
    private Serializer serializer = Donkey.getInstance().getSerializer();

    private AttachmentHandler attachmentHandler;
    private List<MetaDataColumn> metaDataColumns = new ArrayList<MetaDataColumn>();
    private SourceConnector sourceConnector;
    private ConnectorMessageQueue sourceQueue = new ConnectorMessageQueue();
    private FilterTransformerExecutor sourceFilterTransformerExecutor;
    private PreProcessor preProcessor;
    private PostProcessor postProcessor;
    private List<DestinationChain> destinationChains = new ArrayList<DestinationChain>();
    private ResponseSelector responseSelector;

    // A single threaded executor that controls the channel (deploy, start, stop, etc...)
    private ExecutorService controlExecutor = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    // A cached thread pool executor that executes recovery tasks and destination chain tasks
    private ExecutorService channelExecutor;
    private Thread queueThread;
    private Set<Thread> dispatchThreads = new HashSet<Thread>();
    private boolean shuttingDown = false;
    private Set<Future<?>> controlTasks = new LinkedHashSet<Future<?>>();

    private boolean stopSourceQueue = false;
    private Semaphore processLock;
    private ChannelLock lock = ChannelLock.UNLOCKED;

    private MessageController messageController = MessageController.getInstance();

    private Logger logger = Logger.getLogger(getClass());

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public long getLocalChannelId() {
        return localChannelId;
    }

    public void setLocalChannelId(long localChannelId) {
        this.localChannelId = localChannelId;
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

    public DeployedState getInitialState() {
        return initialState;
    }

    public void setInitialState(DeployedState initialState) {
        this.initialState = initialState;
    }

    public DeployedState getCurrentState() {
        return currentState;
    }

    public void updateCurrentState(DeployedState currentState) {
        this.currentState = currentState;
        eventDispatcher.dispatchEvent(new DeployedStateEvent(channelId, name, null, null, DeployedStateEventType.getTypeFromDeployedState(currentState)));
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

    protected EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    protected Serializer getSerializer() {
        return serializer;
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
        return currentState != DeployedState.STOPPED && currentState != DeployedState.STOPPING;
    }

    /**
     * Tell whether or not the channel is configured correctly and is able to be deployed
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
     * Get a specific DestinationConnector by metadata id. A convenience method that searches the
     * destination chains for the destination connector with the given metadata id.
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
        sourceQueue.invalidate(true, false);

        for (DestinationChain chain : destinationChains) {
            for (Integer metaDataId : chain.getMetaDataIds()) {
                chain.getDestinationConnectors().get(metaDataId).getQueue().invalidate(true, false);
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

                eventDispatcher.dispatchEvent(new DeployedStateEvent(channelId, name, null, null, DeployedStateEventType.DEPLOYED, connectorStatistics));
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
                eventDispatcher.dispatchEvent(new DeployedStateEvent(channelId, name, null, null, DeployedStateEventType.UNDEPLOYED));
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
        start(null);
    }

    /**
     * Start the channel and all of the channel's connectors.
     */
    public void start(Set<Integer> connectorsToStart) throws StartException {
        Future<?> task = null;

        try {
            synchronized (controlExecutor) {
                if (lock == ChannelLock.UNLOCKED || lock == ChannelLock.DEPLOY || lock == ChannelLock.DEBUG) {
                    task = controlExecutor.submit(new StartTask(connectorsToStart));
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

    public void halt() throws HaltException {
        Future<?> task = null;
        Throwable firstCause = null;

        try {
            synchronized (controlExecutor) {
                Object[] tasks = controlTasks.toArray();

                // Cancel tasks in reverse order
                for (int i = tasks.length - 1; i >= 0; i--) {
                    ((Future<?>) tasks[i]).cancel(true);
                }

                // These executors must be shutdown here in order to terminate a stop task.
                // They are also shutdown in the halt task itself in case a start task started them after this point.
                channelExecutor.shutdownNow();

                if (queueThread != null) {
                    queueThread.interrupt();
                }

                // Interrupt any dispatch threads that are currently processing
                synchronized (dispatchThreads) {
                    shuttingDown = true;
                    for (Thread thread : dispatchThreads) {
                        thread.interrupt();
                    }
                }

                List<Integer> deployedMetaDataIds = new ArrayList<Integer>();
                deployedMetaDataIds.add(0);

                for (DestinationChain chain : destinationChains) {
                    for (Integer metaDataId : chain.getMetaDataIds()) {
                        deployedMetaDataIds.add(metaDataId);
                    }
                }

                for (Integer metaDataId : deployedMetaDataIds) {
                    try {
                        haltConnector(metaDataId);
                    } catch (Throwable t) {
                        if (firstCause == null) {
                            firstCause = t;
                        }
                    }
                }

                task = controlExecutor.submit(new HaltTask());
                controlTasks.add(task);
            }

            task.get();

            if (firstCause != null) {
                throw firstCause;
            }
        } catch (Throwable t) {
            Throwable cause = t.getCause();
            if (cause instanceof HaltException) {
                throw (HaltException) cause;
            }

            throw new HaltException("Failed to halt channel.", t);
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

        ThreadUtils.checkInterruptedStatus();
        try {
            sourceConnector.stop();
        } catch (Throwable t) {
            logger.error("Error stopping Source connector for channel " + name + " (" + channelId + ").", t);
            if (firstCause == null) {
                firstCause = t;
            }
        }

        ThreadUtils.checkInterruptedStatus();
        final int timeout = 10;

        while (true) {
            synchronized (dispatchThreads) {
                if (dispatchThreads.size() == 0) {
                    shuttingDown = true;
                    /*
                     * Once the thread count reaches zero, we want to make sure that any calls to
                     * finishDispatch complete (which should release the channel's process lock and
                     * allow us to acquire it here).
                     */
                    obtainProcessLock();
                    releaseProcessLock();
                    break;
                }
            }
            Thread.sleep(timeout);
        }

        // If an exception occurs, then still proceed by stopping the rest of the connectors
        for (Integer metaDataId : metaDataIds) {
            ThreadUtils.checkInterruptedStatus();

            try {
                if (metaDataId > 0) {
                    getDestinationConnector(metaDataId).stop();
                }
            } catch (Throwable t) {
                logger.error("Error stopping destination connector \"" + getDestinationConnector(metaDataId).getDestinationName() + "\" for channel " + name + " (" + channelId + ").", t);
                if (firstCause == null) {
                    firstCause = t;
                }
            }
        }

        if (queueThread != null) {
            queueThread.join();
        }

        channelExecutor.shutdown();

        if (firstCause != null) {
            /*
             * Only set the state to STOPPED here if the exception (or its cause) is not an
             * InterruptedException (indicating that the channel was halted).
             */
            if (!(firstCause instanceof InterruptedException) && (firstCause.getCause() == null || !(firstCause.getCause() instanceof InterruptedException))) {
                updateCurrentState(DeployedState.STOPPED);
            }
            throw new StopException("Failed to stop channel " + name + " (" + channelId + "): One or more connectors failed to stop.", firstCause);
        }
    }

    private void halt(List<Integer> metaDataIds) throws HaltException, InterruptedException {
        stopSourceQueue = true;

        channelExecutor.shutdownNow();

        if (queueThread != null) {
            queueThread.interrupt();
        }

        // Interrupt any dispatch threads that are currently processing
        synchronized (dispatchThreads) {
            shuttingDown = true;
            for (Thread thread : dispatchThreads) {
                thread.interrupt();
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

        // In case interrupting everything didn't work, wait until all dispatch, chain, and recovery threads have finished
        final int timeout = 10;

        while (true) {
            synchronized (dispatchThreads) {
                if (dispatchThreads.size() == 0) {
                    shuttingDown = true;
                    /*
                     * Once the thread count reaches zero, we want to make sure that any calls to
                     * finishDispatch complete (which should release the channel's process lock and
                     * allow us to acquire it here).
                     */
                    obtainProcessLock();
                    releaseProcessLock();
                    break;
                }
            }
            Thread.sleep(timeout);
        }

        while (!channelExecutor.awaitTermination(timeout, TimeUnit.MILLISECONDS))
            ;

        if (firstCause != null) {
            updateCurrentState(DeployedState.STOPPED);
            throw new HaltException("Failed to stop channel " + name + " (" + channelId + "): One or more connectors failed to stop.", firstCause);
        }
    }

    public void startConnector(Integer metaDataId) throws StartException {
        Future<?> task = null;

        try {
            synchronized (controlExecutor) {
                if (lock == ChannelLock.UNLOCKED || lock == ChannelLock.DEBUG) {
                    task = controlExecutor.submit(metaDataId == 0 ? new ResumeTask() : new StartDestinationTask(metaDataId));
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

            throw new StartException("Failed to start connector.", t);
        } finally {
            synchronized (controlExecutor) {
                if (task != null) {
                    controlTasks.remove(task);
                }
            }
        }
    }

    public void stopConnector(Integer metaDataId) throws StopException {
        Future<?> task = null;

        try {
            synchronized (controlExecutor) {
                if (lock == ChannelLock.UNLOCKED || lock == ChannelLock.DEBUG) {
                    task = controlExecutor.submit(metaDataId == 0 ? new PauseTask() : new StopDestinationTask(metaDataId));
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

            throw new StopException("Failed to stop connector.", t);
        } finally {
            synchronized (controlExecutor) {
                if (task != null) {
                    controlTasks.remove(task);
                }
            }
        }
    }

    private void haltConnector(Integer metaDataId) throws HaltException {
        try {
            if (metaDataId == 0) {
                sourceConnector.halt();
            } else {
                getDestinationConnector(metaDataId).halt();
            }
        } catch (HaltException e) {
            if (metaDataId == 0) {
                logger.error("Error halting Source connector for channel " + name + " (" + channelId + ").", e);
            } else {
                logger.error("Error halting destination connector \"" + getDestinationConnector(metaDataId).getDestinationName() + "\" for channel " + name + " (" + channelId + ").", e);
            }
            throw e;
        }
    }

    protected DispatchResult dispatchRawMessage(RawMessage rawMessage) throws ChannelException {
        if (currentState == DeployedState.STOPPING || currentState == DeployedState.STOPPED) {
            throw new ChannelException(true);
        }

        Thread currentThread = Thread.currentThread();
        boolean lockAcquired = false;
        Long persistedMessageId = null;

        try {
            synchronized (dispatchThreads) {
                if (!shuttingDown) {
                    dispatchThreads.add(currentThread);
                } else {
                    throw new ChannelException(true);
                }
            }

            DonkeyDao dao = null;
            Message processedMessage = null;
            Response response = null;
            boolean removeContent = false;
            boolean removeAttachments = false;
            DispatchResult dispatchResult = null;

            try {
                obtainProcessLock();
                lockAcquired = true;

                /*
                 * TRANSACTION: Create Raw Message
                 * - create a source connector message from the raw message and set the status as
                 * RECEIVED
                 * - store attachments
                 */
                dao = daoFactory.getDao();
                ConnectorMessage sourceMessage = createAndStoreSourceMessage(dao, rawMessage);
                ThreadUtils.checkInterruptedStatus();

                if (sourceConnector.isRespondAfterProcessing()) {
                    dao.commit(storageSettings.isRawDurable());
                    persistedMessageId = sourceMessage.getMessageId();
                    dao.close();

                    markDeletedQueuedMessages(rawMessage, persistedMessageId);

                    processedMessage = process(sourceMessage, false);

                    boolean messageCompleted = messageController.isMessageCompleted(processedMessage);
                    if (messageCompleted) {
                        removeContent = (storageSettings.isRemoveContentOnCompletion());
                        removeAttachments = (storageSettings.isRemoveAttachmentsOnCompletion());
                    }
                } else {
                    // Block other threads from adding to the source queue until both the current commit and queue addition finishes
                    synchronized (sourceQueue) {
                        dao.commit(storageSettings.isRawDurable());
                        persistedMessageId = sourceMessage.getMessageId();
                        dao.close();
                        queue(sourceMessage);
                    }

                    markDeletedQueuedMessages(rawMessage, persistedMessageId);
                }

                if (responseSelector.canRespond()) {
                    response = responseSelector.getResponse(sourceMessage, processedMessage);
                }
            } catch (RuntimeException e) {
                // TODO determine behavior if this occurs.
                throw new ChannelException(true, e);
            } finally {
                if (lockAcquired && (!sourceConnector.isRespondAfterProcessing() || persistedMessageId == null || Thread.currentThread().isInterrupted())) {
                    // Release the process lock if an exception was thrown before a message was persisted
                    // or if the thread was interrupted because no additional processing will be done.
                    releaseProcessLock();
                    lockAcquired = false;
                }

                if (dao != null && !dao.isClosed()) {
                    dao.close();
                }

                // Create the DispatchResult at the very end because lockAcquired might have changed
                if (persistedMessageId != null) {
                    dispatchResult = new DispatchResult(persistedMessageId, processedMessage, response, sourceConnector.isRespondAfterProcessing(), removeContent, removeAttachments, lockAcquired);
                }
            }

            return dispatchResult;
        } catch (InterruptedException e) {
            // This exception should only ever be thrown during a halt.
            // It is impossible to know whether or not the message was persisted because the task will continue to run
            // even though we are no longer waiting for it. Furthermore it is possible the message was actually sent.

            // The best we can do is cancel the task and throw a channel exception. 
            // If the message was not queued on the source connector, recovery should take care of it.
            // If the message was queued, the source of the message will be notified that the message was not persisted to be safe.
            // This could lead to a potential duplicate message being received/sent, but it is one of the consequences of using halt.

            throw new ChannelException(true, e);
        } catch (Throwable t) {
            Throwable cause = t.getCause();
            ChannelException channelException = null;

            if (cause instanceof InterruptedException) {
                channelException = new ChannelException(true, cause);
            } else if (cause instanceof ChannelException) {
                logger.error("Runtime error in channel.", cause);
                channelException = (ChannelException) cause;
            } else {
                logger.error("Error processing message.", t);
                channelException = new ChannelException(false, t);
            }

            if (persistedMessageId == null) {
                throw channelException;
            }

            return new DispatchResult(persistedMessageId, null, null, false, false, false, lockAcquired, channelException);
        } finally {
            synchronized (dispatchThreads) {
                dispatchThreads.remove(currentThread);
            }
        }
    }

    private void markDeletedQueuedMessages(RawMessage rawMessage, Long persistedMessageId) throws InterruptedException {
        /*
         * If the current message has overwritten a previous one, we mark this message as deleted in
         * all destination queues. This is done so that if a queue thread is currently processing a
         * message, it will release the message after the current attempt, instead of keeping the
         * message in memory and trying again. To ensure that all queues are no longer trying to
         * process this message, we wait until the message is no longer checked out.
         */
        if (rawMessage.isOverwrite() && rawMessage.getOriginalMessageId() != null) {
            // Mark the message as deleted in all queues first
            for (Integer metaDataId : getMetaDataIds()) {
                if (!metaDataId.equals(0)) {
                    getDestinationConnector(metaDataId).getQueue().markAsDeleted(persistedMessageId);
                }
            }

            // Wait until the message is not checked out in all queues
            for (Integer metaDataId : getMetaDataIds()) {
                if (!metaDataId.equals(0)) {
                    while (getDestinationConnector(metaDataId).getQueue().isCheckedOut(persistedMessageId)) {
                        Thread.sleep(100);
                    }
                }
            }
        }
    }

    private ConnectorMessage createAndStoreSourceMessage(DonkeyDao dao, RawMessage rawMessage) throws ChannelException, InterruptedException {
        ThreadUtils.checkInterruptedStatus();
        Long messageId;
        Calendar receivedDate;

        if (rawMessage.isOverwrite() && rawMessage.getOriginalMessageId() != null) {
            messageId = rawMessage.getOriginalMessageId();
            Set<Integer> metaDataIds = new HashSet<Integer>();

            if (rawMessage.getDestinationMetaDataIds() != null) {
                metaDataIds.addAll(rawMessage.getDestinationMetaDataIds());
            } else {
                metaDataIds.addAll(getMetaDataIds());
            }

            metaDataIds.add(0);
            if (!rawMessage.isImported()) {
                dao.deleteMessageStatistics(channelId, messageId, metaDataIds);
            }
            dao.deleteMessageAttachments(channelId, messageId);
            dao.deleteConnectorMessages(channelId, messageId, metaDataIds);
            dao.resetMessage(channelId, messageId);
            receivedDate = Calendar.getInstance();
        } else {
            messageId = dao.getNextMessageId(channelId);
            receivedDate = Calendar.getInstance();

            Message message = new Message();
            message.setMessageId(messageId);
            message.setChannelId(channelId);
            message.setServerId(serverId);
            message.setReceivedDate(receivedDate);
            message.setOriginalId(rawMessage.getOriginalMessageId());

            dao.insertMessage(message);
        }

        ConnectorMessage sourceMessage = new ConnectorMessage(channelId, messageId, 0, serverId, receivedDate, Status.RECEIVED);
        sourceMessage.setConnectorName(sourceConnector.getSourceName());
        sourceMessage.setChainId(0);
        sourceMessage.setOrderId(0);

        sourceMessage.setRaw(new MessageContent(channelId, messageId, 0, ContentType.RAW, null, sourceConnector.getInboundDataType().getType(), false));

        if (rawMessage.getChannelMap() != null) {
            sourceMessage.setChannelMap(rawMessage.getChannelMap());
        }

        if (attachmentHandler != null) {
            ThreadUtils.checkInterruptedStatus();

            try {
                if (rawMessage.isBinary()) {
                    attachmentHandler.initialize(rawMessage.getRawBytes(), this);
                } else {
                    attachmentHandler.initialize(rawMessage.getRawData(), this);
                }

                // Free up the memory of the raw message since it is no longer being used
                rawMessage.clearMessage();

                Attachment attachment;
                while ((attachment = attachmentHandler.nextAttachment()) != null) {
                    ThreadUtils.checkInterruptedStatus();

                    if (storageSettings.isStoreAttachments()) {
                        dao.insertMessageAttachment(channelId, messageId, attachment);
                    }
                }

                String replacedMessage = attachmentHandler.shutdown();

                sourceMessage.getRaw().setContent(replacedMessage);
            } catch (AttachmentException e) {
                eventDispatcher.dispatchEvent(new ErrorEvent(channelId, null, ErrorEventType.ATTACHMENT_HANDLER, null, null, "Error processing attachments for channel " + channelId + ".", e));
                logger.error("Error processing attachments for channel " + channelId + ".", e);
                throw new ChannelException(false, e);
            }
        } else {
            if (rawMessage.isBinary()) {
                ThreadUtils.checkInterruptedStatus();

                try {
                    byte[] rawBytes = Base64Util.encodeBase64(rawMessage.getRawBytes());
                    rawMessage.clearMessage();
                    sourceMessage.getRaw().setContent(org.apache.commons.codec.binary.StringUtils.newStringUsAscii(rawBytes));
                } catch (IOException e) {
                    logger.error("Error processing binary data for channel " + channelId + ".", e);
                    throw new ChannelException(false, e);
                }

            } else {
                sourceMessage.getRaw().setContent(rawMessage.getRawData());
                rawMessage.clearMessage();
            }
        }

        ThreadUtils.checkInterruptedStatus();

        /*
         * If the raw message should be durable, then the channel map from the RawMessage needs to
         * be persisted even if map storage is disabled. The only map that can be utilized at this
         * point is the channel map, therefore we can simply tell the Dao to store all maps.
         */
        dao.insertConnectorMessage(sourceMessage, storageSettings.isStoreMaps() || storageSettings.isRawDurable(), true);

        if (storageSettings.isStoreRaw()) {
            ThreadUtils.checkInterruptedStatus();
            dao.insertMessageContent(sourceMessage.getRaw());
        }

        return sourceMessage;
    }

    public void obtainProcessLock() throws InterruptedException {
        processLock.acquire();
    }

    public void releaseProcessLock() {
        processLock.release();
    }

    /**
     * Queues a source message for processing
     */
    protected void queue(ConnectorMessage sourceMessage) {
        sourceQueue.add(sourceMessage);
    }

    /**
     * Process a source message and return the final processed composite message once all
     * destinations have completed and the post-processor has executed
     * 
     * @param sourceMessage
     *            A source connector message
     * @return The final processed composite message containing the source message and all
     *         destination messages
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
         * - create connector messages for each destination chain with RECEIVED status and maps
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
                if (e instanceof XmlSerializerException) {
                    eventDispatcher.dispatchEvent(new ErrorEvent(channelId, 0, ErrorEventType.SERIALIZER, sourceConnector.getSourceName(), null, e.getMessage(), e));
                }

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
                if (metaDataIds != null) {
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
                    dao.insertConnectorMessage(message, storageSettings.isStoreMaps(), true);

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

            if (!enabledChains.isEmpty()) {
                // Execute each destination chain (but the last one) and store the tasks in a list
                List<Future<List<ConnectorMessage>>> destinationChainTasks = new ArrayList<Future<List<ConnectorMessage>>>();

                for (int i = 0; i <= enabledChains.size() - 2; i++) {
                    try {
                        destinationChainTasks.add(channelExecutor.submit(enabledChains.get(i)));
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
         * Check for null here in case DestinationChain.call() returned null, which indicates that
         * the chain did not process and should be skipped. This would only happen in very rare
         * circumstances, possibly if a message is sent to the chain and the destination connector
         * that the message belongs to has been removed or disabled.
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
        if (cause.getMessage() != null && cause.getMessage().contains("Java heap space")) {
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
        return channelExecutor.submit(new RecoveryTask(this)).get();
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
            try {
                process(sourceMessage, true);
            } catch (RuntimeException e) {
                logger.error("An error occurred in channel " + name + " (" + channelId + ") while processing message ID " + sourceMessage.getMessageId() + " from the source queue", e);
            }

            sourceMessage = sourceQueue.poll();
        }
    }

    public void finishMessage(Message finalMessage, boolean markAsProcessed) throws InterruptedException {
        ThreadUtils.checkInterruptedStatus();
        Response response = null;
        boolean storePostProcessorError = false;
        ConnectorMessage sourceConnectorMessage = finalMessage.getConnectorMessages().get(0);

        try {
            response = postProcessor.doPostProcess(finalMessage);
        } catch (DonkeyException e) {
            sourceConnectorMessage.setPostProcessorError(e.getFormattedError());
            storePostProcessorError = true;
        }

        // Place all destination and custom responses into the source response map
        sourceConnectorMessage.getResponseMap().putAll(finalMessage.getMergedConnectorMessage().getResponseMap());

        if (response != null) {
            sourceConnectorMessage.getResponseMap().put(ResponseConnectorProperties.RESPONSE_POST_PROCESSOR, response);
        }

        /*
         * TRANSACTION: Post Process and Complete
         * - store the merged response map as the source connector's response map
         * - mark the message as processed
         */
        ThreadUtils.checkInterruptedStatus();
        DonkeyDao dao = null;

        try {
            if (storePostProcessorError) {
                dao = daoFactory.getDao();
                dao.updateErrors(sourceConnectorMessage);
            }

            if (markAsProcessed) {
                if (dao == null) {
                    dao = daoFactory.getDao();
                }

                if (storageSettings.isStoreMergedResponseMap()) {
                    ThreadUtils.checkInterruptedStatus();
                    dao.updateResponseMap(sourceConnectorMessage);
                }

                dao.markAsProcessed(channelId, finalMessage.getMessageId());
                finalMessage.setProcessed(true);

                boolean messageCompleted = messageController.isMessageCompleted(finalMessage);
                if (messageCompleted) {
                    if (storageSettings.isRemoveContentOnCompletion()) {
                        dao.deleteMessageContent(channelId, finalMessage.getMessageId());
                    }

                    if (storageSettings.isRemoveAttachmentsOnCompletion()) {
                        dao.deleteMessageAttachments(channelId, finalMessage.getMessageId());
                    }
                }
            }

            if (dao != null) {
                dao.commit(storageSettings.isDurable());
            }
        } finally {
            if (dao != null) {
                dao.close();
            }
        }
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
        message.setProcessed(true);

        dao.insertMessage(message);

        for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
            connectorMessage.setMessageId(messageId);
            connectorMessage.setChannelId(channelId);
            connectorMessage.setServerId(serverId);

            Status status = connectorMessage.getStatus();
            // If the message was exported and does not have a final status, set the status to error so it does not get picked up by the channel's queues or recovery
            if (status != Status.FILTERED && status != Status.TRANSFORMED && status != Status.SENT && status != Status.ERROR) {
                connectorMessage.setStatus(Status.ERROR);
            }

            int metaDataId = connectorMessage.getMetaDataId();

            dao.insertConnectorMessage(connectorMessage, true, false);

            if (!connectorMessage.getMetaDataMap().isEmpty()) {
                dao.insertMetaData(connectorMessage, metaDataColumns);
            }

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
            if (!sourceConnector.isRespondAfterProcessing() && (!storageSettings.isEnabled() || !storageSettings.isStoreRaw() || (!storageSettings.isStoreMaps() && !storageSettings.isRawDurable()))) {
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
                sourceQueue.setDataSource(new ConnectorMessageQueueDataSource(channelId, serverId, 0, Status.RECEIVED, false, daoFactory));

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
                        destinationConnector.getQueue().setDataSource(new ConnectorMessageQueueDataSource(getChannelId(), getServerId(), destinationConnector.getMetaDataId(), Status.QUEUED, destinationConnector.isQueueRotate(), daoFactory));

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

        private Set<Integer> connectorsToStart;

        public StartTask(Set<Integer> connectorsToStart) {
            this.connectorsToStart = connectorsToStart;
        }

        @Override
        public Void call() throws Exception {
            if (currentState != DeployedState.STARTED) {
                // Prevent the channel for being started while messages are being deleted.
                synchronized (Channel.this) {
                    updateCurrentState(DeployedState.STARTING);

                    /*
                     * We can't guarantee the state of the semaphore when the channel was stopped /
                     * halted, so we just create a new one instead.
                     */
                    processLock = new Semaphore(1, true);
                    dispatchThreads.clear();
                    shuttingDown = false;
                    stopSourceQueue = false;

                    // Remove any items in the queue's buffer because they may be outdated and refresh the queue size.
                    sourceQueue.invalidate(true, true);

                    // enable all destination connectors in each chain
                    for (DestinationChain chain : destinationChains) {
                        chain.getEnabledMetaDataIds().clear();
                        chain.getEnabledMetaDataIds().addAll(chain.getMetaDataIds());
                    }
                    List<Integer> startedMetaDataIds = new ArrayList<Integer>();

                    try {
                        channelExecutor = Executors.newCachedThreadPool();

                        // start the destination connectors
                        for (DestinationChain chain : destinationChains) {
                            for (Integer metaDataId : chain.getMetaDataIds()) {
                                DestinationConnector destinationConnector = chain.getDestinationConnectors().get(metaDataId);

                                if (destinationConnector.getCurrentState() == DeployedState.STOPPED && (connectorsToStart == null || connectorsToStart.contains(metaDataId))) {
                                    startedMetaDataIds.add(metaDataId);
                                    destinationConnector.start();
                                }
                            }
                        }

                        ThreadUtils.checkInterruptedStatus();
                        try {
                            processUnfinishedMessages();
                        } catch (InterruptedException e) {
                            logger.error("Startup recovery interrupted for channel " + name + "(" + channelId + ")", e);
                            Thread.currentThread().interrupt();
                        } catch (Exception e) {
                            Throwable cause;
                            if (e instanceof ExecutionException) {
                                cause = e.getCause();
                            } else {
                                cause = e;
                            }

                            logger.error("Startup recovery failed for channel " + name + "(" + channelId + "): " + cause.getMessage(), cause);
                        }

                        ThreadUtils.checkInterruptedStatus();
                        // start up the worker thread that will process queued messages
                        if (!sourceConnector.isRespondAfterProcessing()) {
                            queueThread = new Thread(Channel.this);
                            queueThread.start();
                        }

                        if (connectorsToStart == null || connectorsToStart.contains(0)) {
                            ThreadUtils.checkInterruptedStatus();
                            // start up the source connector
                            if (sourceConnector.getCurrentState() == DeployedState.STOPPED) {
                                startedMetaDataIds.add(0);
                                sourceConnector.start();
                            }

                            updateCurrentState(DeployedState.STARTED);
                        } else {
                            updateCurrentState(DeployedState.PAUSED);
                        }
                    } catch (Throwable t) {
                        // If an exception occurred, then attempt to rollback by stopping all the connectors that were started
                        try {
                            stop(startedMetaDataIds);
                            updateCurrentState(DeployedState.STOPPED);
                        } catch (Throwable t2) {
                            /*
                             * Only set the state to STOPPED here if the exception (or its cause) is
                             * not an InterruptedException (indicating that the channel was halted).
                             */
                            if (!(t2 instanceof InterruptedException) && (t2.getCause() == null || !(t2.getCause() instanceof InterruptedException))) {
                                updateCurrentState(DeployedState.STOPPED);
                            }
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
            if (currentState != DeployedState.STOPPED) {
                updateCurrentState(DeployedState.STOPPING);
                List<Integer> deployedMetaDataIds = new ArrayList<Integer>();
                deployedMetaDataIds.add(0);

                for (DestinationChain chain : destinationChains) {
                    for (Integer metaDataId : chain.getMetaDataIds()) {
                        deployedMetaDataIds.add(metaDataId);
                    }
                }

                stop(deployedMetaDataIds);
                updateCurrentState(DeployedState.STOPPED);
            } else {
                logger.warn("Failed to stop channel " + name + " (" + channelId + "): The channel is already stopped.");
            }

            return null;
        }
    }

    private class HaltTask implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            if (currentState != DeployedState.STOPPED) {
                updateCurrentState(DeployedState.STOPPING);
                List<Integer> deployedMetaDataIds = new ArrayList<Integer>();
                deployedMetaDataIds.add(0);

                for (DestinationChain chain : destinationChains) {
                    for (Integer metaDataId : chain.getMetaDataIds()) {
                        deployedMetaDataIds.add(metaDataId);
                    }
                }

                halt(deployedMetaDataIds);
                updateCurrentState(DeployedState.STOPPED);
            } else {
                logger.warn("Failed to stop channel " + name + " (" + channelId + "): The channel is already stopped.");
            }

            return null;
        }
    }

    private class PauseTask implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            if (currentState == DeployedState.STARTED) {
                try {
                    updateCurrentState(DeployedState.PAUSING);
                    sourceConnector.stop();
                    updateCurrentState(DeployedState.PAUSED);
                } catch (Throwable t) {
                    throw new PauseException("Failed to pause channel " + name + " (" + channelId + ").", t);
                }
            } else {
                //TODO what to do here?
                if (currentState == DeployedState.PAUSED) {
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
            if (currentState == DeployedState.PAUSED) {
                try {
                    updateCurrentState(DeployedState.STARTING);
                    sourceConnector.start();
                    updateCurrentState(DeployedState.STARTED);
                } catch (Throwable t) {
                    try {
                        sourceConnector.stop();
                        updateCurrentState(DeployedState.PAUSED);
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

    private class StartDestinationTask implements Callable<Void> {

        private Integer metaDataId;

        public StartDestinationTask(Integer metaDataId) {
            this.metaDataId = metaDataId;
        }

        @Override
        public Void call() throws Exception {
            DestinationConnector destinationConnector = getDestinationConnector(metaDataId);

            if (currentState == DeployedState.STARTED || currentState == DeployedState.PAUSED) {
                if (destinationConnector.getCurrentState() != DeployedState.STARTED) {
                    try {
                        destinationConnector.start();
                    } catch (Throwable t) {
                        throw new StartException("Failed to stop connector " + destinationConnector.getDestinationName() + " for channel " + name + " (" + channelId + "). ", t);
                    }
                }
            } else {
                logger.error("Failed to start connector " + destinationConnector.getDestinationName() + " for channel " + name + " (" + channelId + "): The channel is not started or paused.");
            }

            return null;
        }

    }

    private class StopDestinationTask implements Callable<Void> {

        private Integer metaDataId;

        public StopDestinationTask(Integer metaDataId) {
            this.metaDataId = metaDataId;
        }

        @Override
        public Void call() throws Exception {
            DestinationConnector destinationConnector = getDestinationConnector(metaDataId);

            if (currentState == DeployedState.STARTED || currentState == DeployedState.PAUSED) {
                if (destinationConnector.getCurrentState() != DeployedState.STOPPED) {
                    // Destination connectors can only be stopped individually if the queue is enabled.
                    if (destinationConnector.isQueueEnabled()) {
                        try {
                            // Force messages to be queued after this point even if attempt first is on.
                            destinationConnector.setForceQueue(true);
                            destinationConnector.stop();
                        } catch (Throwable t) {
                            throw new StopException("Failed to stop connector " + destinationConnector.getDestinationName() + " for channel " + name + " (" + channelId + "). ", t);
                        }
                    } else {
                        logger.error("Failed to stop connector " + destinationConnector.getDestinationName() + " for channel " + name + " (" + channelId + "): Destination connectors must have queueing enabled to be stopped individually.");
                    }
                }
            } else {
                logger.error("Failed to stop connector " + destinationConnector.getDestinationName() + " for channel " + name + " (" + channelId + "): The channel is not started or paused.");
            }

            return null;
        }

    }
}
