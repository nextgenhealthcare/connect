/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.channel.SourceConnectorProperties;
import com.mirth.connect.donkey.model.channel.SourceConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.event.Event;
import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.SerializationType;
import com.mirth.connect.donkey.model.message.XmlSerializer;
import com.mirth.connect.donkey.model.message.XmlSerializerException;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.DonkeyConfiguration;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.donkey.server.channel.DestinationChain;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.FilterTransformerExecutor;
import com.mirth.connect.donkey.server.channel.MetaDataReplacer;
import com.mirth.connect.donkey.server.channel.ResponseSelector;
import com.mirth.connect.donkey.server.channel.ResponseTransformerExecutor;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.donkey.server.channel.StorageSettings;
import com.mirth.connect.donkey.server.channel.components.PostProcessor;
import com.mirth.connect.donkey.server.channel.components.PreProcessor;
import com.mirth.connect.donkey.server.data.buffered.BufferedDaoFactory;
import com.mirth.connect.donkey.server.data.passthru.DelayedStatisticsUpdater;
import com.mirth.connect.donkey.server.data.passthru.PassthruDaoFactory;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.donkey.server.event.EventDispatcher;
import com.mirth.connect.donkey.server.message.DataType;
import com.mirth.connect.donkey.server.message.ResponseValidator;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptorFactory;
import com.mirth.connect.donkey.server.message.batch.BatchMessageException;
import com.mirth.connect.donkey.server.message.batch.BatchMessageReader;
import com.mirth.connect.donkey.server.message.batch.ResponseHandler;
import com.mirth.connect.donkey.server.message.batch.SimpleResponseHandler;
import com.mirth.connect.donkey.server.queue.ConnectorMessageQueue;
import com.mirth.connect.model.ChannelProperties;
import com.mirth.connect.model.CodeTemplate.ContextType;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.DashboardStatus.StatusType;
import com.mirth.connect.model.Filter;
import com.mirth.connect.model.MessageStorageMode;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.model.attachments.AttachmentHandlerType;
import com.mirth.connect.model.datatype.BatchProperties;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.plugins.ChannelPlugin;
import com.mirth.connect.plugins.DataTypeServerPlugin;
import com.mirth.connect.server.ExtensionLoader;
import com.mirth.connect.server.attachments.JavaScriptAttachmentHandler;
import com.mirth.connect.server.attachments.MirthAttachmentHandler;
import com.mirth.connect.server.attachments.PassthruAttachmentHandler;
import com.mirth.connect.server.builders.JavaScriptBuilder;
import com.mirth.connect.server.channel.ChannelFuture;
import com.mirth.connect.server.channel.ChannelTask;
import com.mirth.connect.server.channel.ChannelTaskHandler;
import com.mirth.connect.server.channel.LoggingTaskHandler;
import com.mirth.connect.server.channel.MirthMetaDataReplacer;
import com.mirth.connect.server.message.DataTypeFactory;
import com.mirth.connect.server.message.DefaultResponseValidator;
import com.mirth.connect.server.mybatis.MessageSearchResult;
import com.mirth.connect.server.transformers.JavaScriptFilterTransformer;
import com.mirth.connect.server.transformers.JavaScriptPostprocessor;
import com.mirth.connect.server.transformers.JavaScriptPreprocessor;
import com.mirth.connect.server.transformers.JavaScriptResponseTransformer;
import com.mirth.connect.server.util.GlobalChannelVariableStoreFactory;
import com.mirth.connect.server.util.GlobalVariableStore;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;

public class DonkeyEngineController implements EngineController {
    private static EngineController instance = null;

    public static EngineController getInstance() {
        synchronized (DonkeyEngineController.class) {
            if (instance == null) {
                instance = ExtensionLoader.getInstance().getControllerInstance(EngineController.class);

                if (instance == null) {
                    instance = new DonkeyEngineController();
                }
            }

            return instance;
        }
    }

    private Donkey donkey = Donkey.getInstance();
    private Logger logger = Logger.getLogger(this.getClass());
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private com.mirth.connect.donkey.server.controllers.ChannelController donkeyChannelController = com.mirth.connect.donkey.server.controllers.ChannelController.getInstance();
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
    private int queueBufferSize = Constants.DEFAULT_QUEUE_BUFFER_SIZE;
    private Map<String, ExecutorService> engineExecutors = new ConcurrentHashMap<String, ExecutorService>();
    private Set<Channel> deployingChannels = Collections.synchronizedSet(new HashSet<Channel>());
    private Set<Channel> undeployingChannels = Collections.synchronizedSet(new HashSet<Channel>());

    private enum StatusTask {
        START, STOP, PAUSE, RESUME
    };

    protected DonkeyEngineController() {}

    @Override
    public void startEngine() throws StartException, StopException, ControllerException, InterruptedException {
        logger.debug("starting donkey engine");

        Integer queueBufferSize = configurationController.getServerSettings().getQueueBufferSize();
        if (queueBufferSize != null) {
            this.queueBufferSize = queueBufferSize;
        }

        final Encryptor encryptor = configurationController.getEncryptor();

        com.mirth.connect.donkey.server.Encryptor donkeyEncryptor = new com.mirth.connect.donkey.server.Encryptor() {
            @Override
            public String encrypt(String text) {
                return encryptor.encrypt(text);
            }

            @Override
            public String decrypt(String text) {
                return encryptor.decrypt(text);
            }
        };

        EventDispatcher eventDispatcher = new EventDispatcher() {

            @Override
            public void dispatchEvent(Event event) {
                eventController.dispatchEvent(event);
            }
        };

        donkey.startEngine(new DonkeyConfiguration(configurationController.getApplicationDataDir(), configurationController.getDatabaseSettings().getProperties(), donkeyEncryptor, eventDispatcher, configurationController.getServerId()));
    }

    @Override
    public void stopEngine() throws StopException, InterruptedException {
        undeployChannels(getDeployedIds(), ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, null);
        donkey.stopEngine();
    }

    @Override
    public boolean isRunning() {
        return donkey.isRunning();
    }

    @Override
    public void startupDeploy() {
        deployChannels(channelController.getChannelIds(), ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, null);
    }

    @Override
    public void deployChannels(Set<String> channelIds, ServerEventContext context, ChannelTaskHandler handler) {
        List<ChannelTask> deployTasks = new ArrayList<ChannelTask>();
        List<ChannelTask> undeployTasks = new ArrayList<ChannelTask>();

        for (String channelId : channelIds) {
            if (isDeployed(channelId)) {
                undeployTasks.add(new UndeployTask(channelId, context));
            }

            deployTasks.add(new DeployTask(channelId, null, null, context));
        }

        if (CollectionUtils.isNotEmpty(undeployTasks)) {
            waitForTasks(submitTasks(undeployTasks, handler));
            executeChannelPluginOnUndeploy(context);
            executeGlobalUndeployScript();
        }

        if (CollectionUtils.isNotEmpty(deployTasks)) {
            executeGlobalDeployScript();
            executeChannelPluginOnDeploy(context);
            waitForTasks(submitTasks(deployTasks, handler));
        }
    }

    @Override
    public void undeployChannels(Set<String> channelIds, ServerEventContext context, ChannelTaskHandler handler) {
        List<ChannelTask> undeployTasks = new ArrayList<ChannelTask>();

        for (String channelId : channelIds) {
            undeployTasks.add(new UndeployTask(channelId, context));
        }

        if (CollectionUtils.isNotEmpty(undeployTasks)) {
            waitForTasks(submitTasks(undeployTasks, handler));
            executeChannelPluginOnUndeploy(context);
            executeGlobalUndeployScript();
        }
    }

    @Override
    public void redeployAllChannels(ServerEventContext context, ChannelTaskHandler handler) {
        undeployChannels(getDeployedIds(), context, handler);
        clearGlobalMap();
        deployChannels(channelController.getChannelIds(), context, handler);
    }

    @Override
    public void startChannels(Set<String> channelIds, ChannelTaskHandler handler) {
        waitForTasks(submitTasks(buildChannelStatusTasks(channelIds, StatusTask.START), handler));
    }

    @Override
    public void stopChannels(Set<String> channelIds, ChannelTaskHandler handler) {
        waitForTasks(submitTasks(buildChannelStatusTasks(channelIds, StatusTask.STOP), handler));
    }

    @Override
    public void pauseChannels(Set<String> channelIds, ChannelTaskHandler handler) {
        waitForTasks(submitTasks(buildChannelStatusTasks(channelIds, StatusTask.PAUSE), handler));
    }

    @Override
    public void resumeChannels(Set<String> channelIds, ChannelTaskHandler handler) {
        waitForTasks(submitTasks(buildChannelStatusTasks(channelIds, StatusTask.RESUME), handler));
    }

    @Override
    public void startConnector(Map<String, List<Integer>> connectorInfo, ChannelTaskHandler handler) {
        waitForTasks(submitTasks(buildConnectorStatusTasks(connectorInfo, StatusTask.START), handler));
    }

    @Override
    public void stopConnector(Map<String, List<Integer>> connectorInfo, ChannelTaskHandler handler) {
        waitForTasks(submitTasks(buildConnectorStatusTasks(connectorInfo, StatusTask.STOP), handler));
    }

    @Override
    public void haltChannels(Set<String> channelIds, ChannelTaskHandler handler) {
        waitForTasks(submitHaltTasks(channelIds, handler));
    }

    @Override
    public void removeChannels(Set<String> channelIds, ServerEventContext context, ChannelTaskHandler handler) {
        List<ChannelTask> tasks = new ArrayList<ChannelTask>();

        for (com.mirth.connect.model.Channel channelModel : channelController.getChannels(channelIds)) {
            tasks.add(new UndeployTask(channelModel.getId(), context));
            tasks.add(new RemoveTask(channelModel, context));
        }

        if (CollectionUtils.isNotEmpty(tasks)) {
            waitForTasks(submitTasks(tasks, handler));
            executeChannelPluginOnUndeploy(context);
        }
    }

    @Override
    public void removeMessages(String channelId, Map<Long, MessageSearchResult> results, ChannelTaskHandler handler) {
        List<ChannelTask> tasks = new ArrayList<ChannelTask>();

        tasks.add(new RemoveMessagesTask(channelId, results));

        waitForTasks(submitTasks(tasks, handler));
    };

    @Override
    public void removeAllMessages(Set<String> channelIds, boolean force, boolean clearStatistics, ChannelTaskHandler handler) {
        List<ChannelTask> tasks = new ArrayList<ChannelTask>();

        for (String channelId : channelIds) {
            tasks.add(new RemoveAllMessagesTask(channelId, force, clearStatistics));
        }

        waitForTasks(submitTasks(tasks, handler));
    }

    @Override
    public DashboardStatus getChannelStatus(String channelId) {
        Channel channel = donkey.getDeployedChannels().get(channelId);
        if (channel != null) {
            return getDashboardStatuses(Collections.singleton(channel)).get(0);
        }
        return null;
    }

    @Override
    public List<DashboardStatus> getChannelStatusList() {
        return getChannelStatusList(null);
    }

    @Override
    public List<DashboardStatus> getChannelStatusList(Set<String> channelIds) {
        Map<String, Channel> channels = null;

        if (channelIds != null) {
            channels = new HashMap<String, Channel>();

            for (Channel channel : donkey.getDeployedChannels().values()) {
                if (channelIds.contains(channel.getChannelId())) {
                    channels.put(channel.getChannelId(), channel);
                }
            }
        } else {
            channels = new HashMap<String, Channel>(donkey.getDeployedChannels());
        }

        synchronized (deployingChannels) {
            for (Channel channel : deployingChannels) {
                if (!channels.containsKey(channel.getChannelId())) {
                    channels.put(channel.getChannelId(), channel);
                }
            }
        }

        synchronized (undeployingChannels) {
            for (Channel channel : undeployingChannels) {
                if (!channels.containsKey(channel.getChannelId())) {
                    channels.put(channel.getChannelId(), channel);
                }
            }
        }

        return getDashboardStatuses(channels.values());
    }

    private List<DashboardStatus> getDashboardStatuses(Collection<Channel> channels) {
        List<DashboardStatus> statuses = new ArrayList<DashboardStatus>();

        Map<String, Integer> channelRevisions = null;
        try {
            channelRevisions = channelController.getChannelRevisions();
        } catch (ControllerException e) {
            logger.error("Error retrieving channel revisions", e);
        }

        for (Channel channel : channels) {
            String channelId = channel.getChannelId();
            com.mirth.connect.model.Channel channelModel = channelController.getDeployedChannelById(channelId);

            // Make sure the channel is actually still deployed
            if (channelModel != null) {
                Statistics stats = channelController.getStatistics();
                Statistics lifetimeStats = channelController.getTotalStatistics();

                DashboardStatus status = new DashboardStatus();
                status.setStatusType(StatusType.CHANNEL);
                status.setChannelId(channelId);
                status.setName(channel.getName());
                status.setState(channel.getCurrentState());
                status.setDeployedDate(channel.getDeployDate());

                int channelRevision = 0;
                // Just in case the channel no longer exists
                if (channelRevisions != null && channelRevisions.containsKey(channelId)) {
                    channelRevision = channelRevisions.get(channelId);
                    status.setDeployedRevisionDelta(channelRevision - channelModel.getRevision());
                }

                status.setStatistics(stats.getConnectorStats(channelId, null));
                status.setLifetimeStatistics(lifetimeStats.getConnectorStats(channelId, null));
                status.setTags(channelModel.getProperties().getTags());

                DashboardStatus sourceStatus = new DashboardStatus();
                sourceStatus.setStatusType(StatusType.SOURCE_CONNECTOR);
                sourceStatus.setChannelId(channelId);
                sourceStatus.setMetaDataId(0);
                sourceStatus.setName("Source");
                sourceStatus.setState(channel.getSourceConnector().getCurrentState());
                sourceStatus.setStatistics(stats.getConnectorStats(channelId, 0));
                sourceStatus.setLifetimeStatistics(lifetimeStats.getConnectorStats(channelId, 0));
                sourceStatus.setQueueEnabled(!channel.getSourceConnector().isRespondAfterProcessing());
                sourceStatus.setQueued(new Long(channel.getSourceQueue().size()));

                status.setQueued(sourceStatus.getQueued());

                status.getChildStatuses().add(sourceStatus);

                for (DestinationChain chain : channel.getDestinationChains()) {
                    for (Entry<Integer, DestinationConnector> connectorEntry : chain.getDestinationConnectors().entrySet()) {
                        Integer metaDataId = connectorEntry.getKey();
                        DestinationConnector connector = connectorEntry.getValue();

                        DashboardStatus destinationStatus = new DashboardStatus();
                        destinationStatus.setStatusType(StatusType.DESTINATION_CONNECTOR);
                        destinationStatus.setChannelId(channelId);
                        destinationStatus.setMetaDataId(metaDataId);
                        destinationStatus.setName(connector.getDestinationName());
                        destinationStatus.setState(connector.getCurrentState());
                        destinationStatus.setStatistics(stats.getConnectorStats(channelId, metaDataId));
                        destinationStatus.setLifetimeStatistics(lifetimeStats.getConnectorStats(channelId, metaDataId));
                        destinationStatus.setQueueEnabled(connector.isQueueEnabled());
                        destinationStatus.setQueued(new Long(connector.getQueue().size()));

                        status.setQueued(status.getQueued() + destinationStatus.getQueued());

                        status.getChildStatuses().add(destinationStatus);
                    }
                }

                statuses.add(status);
            }
        }

        Collections.sort(statuses, new Comparator<DashboardStatus>() {

            public int compare(DashboardStatus o1, DashboardStatus o2) {
                Calendar c1 = o1.getDeployedDate();
                Calendar c2 = o2.getDeployedDate();

                return ObjectUtils.compare(c1, c2);
            }

        });

        return statuses;
    }

    @Override
    public Set<String> getDeployedIds() {
        return donkey.getDeployedChannelIds();
    }

    @Override
    public boolean isDeployed(String channelId) {
        return donkey.getDeployedChannels().containsKey(channelId);
    }

    @Override
    public Channel getDeployedChannel(String channelId) {
        return donkey.getDeployedChannels().get(channelId);
    }

    @Override
    public DispatchResult dispatchRawMessage(String channelId, RawMessage rawMessage, boolean force, boolean canBatch) throws ChannelException, BatchMessageException {
        if (!isDeployed(channelId)) {
            ChannelException e = new ChannelException(true);
            logger.error("Could not find channel to route to: " + channelId, e);
            throw e;
        }

        SourceConnector sourceConnector = donkey.getDeployedChannels().get(channelId).getSourceConnector();

        if (canBatch && sourceConnector.isProcessBatch()) {
            if (rawMessage.isBinary()) {
                throw new BatchMessageException("Batch processing is not supported for binary data.");
            } else {
                BatchRawMessage batchRawMessage = new BatchRawMessage(new BatchMessageReader(rawMessage.getRawData()), rawMessage.getSourceMap());

                ResponseHandler responseHandler = new SimpleResponseHandler();
                sourceConnector.dispatchBatchMessage(batchRawMessage, responseHandler);

                return responseHandler.getResultForResponse();
            }
        } else {
            DispatchResult dispatchResult = null;

            try {
                dispatchResult = sourceConnector.dispatchRawMessage(rawMessage, force);
                dispatchResult.setAttemptedResponse(true);
            } finally {
                sourceConnector.finishDispatch(dispatchResult);
            }

            return dispatchResult;
        }
    }

    protected Channel createChannelFromModel(com.mirth.connect.model.Channel channelModel) throws Exception {
        String channelId = channelModel.getId();
        ChannelProperties channelProperties = channelModel.getProperties();
        StorageSettings storageSettings = getStorageSettings(channelProperties.getMessageStorageMode(), channelProperties);

        Channel channel = new Channel();

        Map<String, Integer> destinationIdMap = new LinkedHashMap<String, Integer>();

        channel.setChannelId(channelId);
        channel.setLocalChannelId(donkeyChannelController.getLocalChannelId(channelId));
        channel.setServerId(ConfigurationController.getInstance().getServerId());
        channel.setName(channelModel.getName());
        channel.setEnabled(channelModel.isEnabled());
        channel.setRevision(channelModel.getRevision());
        channel.setInitialState(channelProperties.getInitialState());
        channel.setStorageSettings(storageSettings);
        channel.setMetaDataColumns(channelProperties.getMetaDataColumns());
        channel.setAttachmentHandler(createAttachmentHandler(channelId, channelProperties.getAttachmentProperties()));
        channel.setPreProcessor(createPreProcessor(channelId, channelModel.getPreprocessingScript(), destinationIdMap));
        channel.setPostProcessor(createPostProcessor(channelId, channelModel.getPostprocessingScript()));
        channel.setSourceConnector(createSourceConnector(channel, channelModel.getSourceConnector(), storageSettings, destinationIdMap));
        channel.setResponseSelector(new ResponseSelector(channel.getSourceConnector().getInboundDataType()));
        channel.setSourceFilterTransformer(createFilterTransformerExecutor(channelId, channelModel.getSourceConnector(), destinationIdMap));

        ConnectorMessageQueue sourceQueue = new ConnectorMessageQueue();
        sourceQueue.setBufferCapacity(queueBufferSize);
        channel.setSourceQueue(sourceQueue);

        if (channelModel.getSourceConnector().getProperties() instanceof SourceConnectorPropertiesInterface) {
            SourceConnectorProperties sourceConnectorProperties = ((SourceConnectorPropertiesInterface) channelModel.getSourceConnector().getProperties()).getSourceConnectorProperties();
            channel.getResponseSelector().setRespondFromName(sourceConnectorProperties.getResponseVariable());
        }

        if (storageSettings.isEnabled()) {
            BufferedDaoFactory bufferedDaoFactory = new BufferedDaoFactory(donkey.getDaoFactory());
            bufferedDaoFactory.setEncryptData(channelProperties.isEncryptData());

            channel.setDaoFactory(bufferedDaoFactory);
        } else {
            channel.setDaoFactory(new PassthruDaoFactory(new DelayedStatisticsUpdater(donkey.getDaoFactory())));
        }

        DestinationChain chain = createDestinationChain(channel);

        for (com.mirth.connect.model.Connector connectorModel : channelModel.getDestinationConnectors()) {
            if (connectorModel.isEnabled()) {
                // read 'waitForPrevious' property and add new chains as needed
                // if there are currently no chains, add a new one regardless of 'waitForPrevious'
                if (!connectorModel.isWaitForPrevious() || channel.getDestinationChains().size() == 0) {
                    chain = createDestinationChain(channel);
                    channel.addDestinationChain(chain);
                }

                Integer metaDataId = connectorModel.getMetaDataId();
                destinationIdMap.put(connectorModel.getName(), metaDataId);

                if (metaDataId == null) {
                    metaDataId = channelModel.getNextMetaDataId();
                    channelModel.setNextMetaDataId(metaDataId + 1);
                    connectorModel.setMetaDataId(metaDataId);
                }

                chain.addDestination(connectorModel.getMetaDataId(), createFilterTransformerExecutor(channelId, connectorModel, destinationIdMap), createDestinationConnector(channel, connectorModel, storageSettings, destinationIdMap));
            }
        }

        return channel;
    }

    public static StorageSettings getStorageSettings(MessageStorageMode messageStorageMode, ChannelProperties channelProperties) {
        StorageSettings storageSettings = new StorageSettings();
        storageSettings.setRemoveContentOnCompletion(channelProperties.isRemoveContentOnCompletion());
        storageSettings.setRemoveAttachmentsOnCompletion(channelProperties.isRemoveAttachmentsOnCompletion());
        storageSettings.setStoreAttachments(channelProperties.isStoreAttachments());

        // we assume that all storage settings are enabled by default
        switch (messageStorageMode) {
            case PRODUCTION:
                storageSettings.setStoreProcessedRaw(false);
                storageSettings.setStoreTransformed(false);
                storageSettings.setStoreResponseTransformed(false);
                storageSettings.setStoreProcessedResponse(false);
                break;

            case RAW:
                storageSettings.setMessageRecoveryEnabled(false);
                storageSettings.setDurable(false);
                storageSettings.setStoreMaps(false);
                storageSettings.setStoreResponseMap(false);
                storageSettings.setStoreMergedResponseMap(false);
                storageSettings.setStoreProcessedRaw(false);
                storageSettings.setStoreTransformed(false);
                storageSettings.setStoreSourceEncoded(false);
                storageSettings.setStoreDestinationEncoded(false);
                storageSettings.setStoreSent(false);
                storageSettings.setStoreResponseTransformed(false);
                storageSettings.setStoreProcessedResponse(false);
                storageSettings.setStoreResponse(false);
                storageSettings.setStoreSentResponse(false);
                break;

            case METADATA:
                storageSettings.setMessageRecoveryEnabled(false);
                storageSettings.setDurable(false);
                storageSettings.setRawDurable(false);
                storageSettings.setStoreMaps(false);
                storageSettings.setStoreResponseMap(false);
                storageSettings.setStoreMergedResponseMap(false);
                storageSettings.setStoreRaw(false);
                storageSettings.setStoreProcessedRaw(false);
                storageSettings.setStoreTransformed(false);
                storageSettings.setStoreSourceEncoded(false);
                storageSettings.setStoreDestinationEncoded(false);
                storageSettings.setStoreSent(false);
                storageSettings.setStoreResponseTransformed(false);
                storageSettings.setStoreProcessedResponse(false);
                storageSettings.setStoreResponse(false);
                storageSettings.setStoreSentResponse(false);
                break;

            case DISABLED:
                storageSettings.setEnabled(false);
                storageSettings.setMessageRecoveryEnabled(false);
                storageSettings.setDurable(false);
                storageSettings.setRawDurable(false);
                storageSettings.setStoreCustomMetaData(false);
                storageSettings.setStoreMaps(false);
                storageSettings.setStoreResponseMap(false);
                storageSettings.setStoreMergedResponseMap(false);
                storageSettings.setStoreRaw(false);
                storageSettings.setStoreProcessedRaw(false);
                storageSettings.setStoreTransformed(false);
                storageSettings.setStoreSourceEncoded(false);
                storageSettings.setStoreDestinationEncoded(false);
                storageSettings.setStoreSent(false);
                storageSettings.setStoreResponseTransformed(false);
                storageSettings.setStoreProcessedResponse(false);
                storageSettings.setStoreResponse(false);
                storageSettings.setStoreSentResponse(false);
                break;
        }

        return storageSettings;
    }

    private AttachmentHandler createAttachmentHandler(String channelId, AttachmentHandlerProperties attachmentHandlerProperties) throws Exception {
        AttachmentHandler attachmentHandler = null;

        if (AttachmentHandlerType.fromString(attachmentHandlerProperties.getType()) != AttachmentHandlerType.NONE) {
            Class<?> attachmentHandlerClass = Class.forName(attachmentHandlerProperties.getClassName());

            if (MirthAttachmentHandler.class.isAssignableFrom(attachmentHandlerClass)) {
                attachmentHandler = (MirthAttachmentHandler) attachmentHandlerClass.newInstance();
                attachmentHandler.setProperties(attachmentHandlerProperties);

                if (attachmentHandler instanceof JavaScriptAttachmentHandler) {
                    String scriptId = ScriptController.getScriptId(ScriptController.ATTACHMENT_SCRIPT_KEY, channelId);
                    String attachmentScript = attachmentHandlerProperties.getProperties().get("javascript.script");

                    if (attachmentScript != null) {
                        try {
                            Set<String> scriptOptions = new HashSet<String>();
                            scriptOptions.add("useAttachmentList");
                            JavaScriptUtil.compileAndAddScript(scriptId, attachmentScript, ContextType.CHANNEL_CONTEXT, scriptOptions);
                        } catch (Exception e) {
                            logger.error("Error compiling attachment handler script " + scriptId + ".", e);
                        }
                    }
                }
            } else {
                throw new Exception(attachmentHandlerProperties.getClassName() + " does not extend " + MirthAttachmentHandler.class.getName());
            }
        } else {
            attachmentHandler = new PassthruAttachmentHandler();
        }

        return attachmentHandler;
    }

    private PreProcessor createPreProcessor(String channelId, String preProcessingScript, Map<String, Integer> destinationIdMap) {
        String scriptId = ScriptController.getScriptId(ScriptController.PREPROCESSOR_SCRIPT_KEY, channelId);

        try {
            JavaScriptUtil.compileAndAddScript(scriptId, preProcessingScript, ContextType.CHANNEL_CONTEXT);
        } catch (Exception e) {
            logger.error("Error compiling preprocessor script " + scriptId + ".", e);
        }

        return new JavaScriptPreprocessor(destinationIdMap);
    }

    private PostProcessor createPostProcessor(String channelId, String postProcessingScript) {
        String scriptId = ScriptController.getScriptId(ScriptController.POSTPROCESSOR_SCRIPT_KEY, channelId);

        try {
            JavaScriptUtil.compileAndAddScript(scriptId, postProcessingScript, ContextType.CHANNEL_CONTEXT);
        } catch (Exception e) {
            logger.error("Error compiling postprocessor script " + scriptId + ".", e);
        }

        return new JavaScriptPostprocessor();
    }

    private SourceConnector createSourceConnector(Channel channel, com.mirth.connect.model.Connector connectorModel, StorageSettings storageSettings, Map<String, Integer> destinationIdMap) throws Exception {
        ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
        ConnectorProperties connectorProperties = connectorModel.getProperties();
        ConnectorMetaData connectorMetaData = extensionController.getConnectorMetaData().get(connectorProperties.getName());
        SourceConnector sourceConnector = (SourceConnector) Class.forName(connectorMetaData.getServerClassName()).newInstance();

        setCommonConnectorProperties(channel.getChannelId(), sourceConnector, connectorModel, destinationIdMap);

        sourceConnector.setMetaDataReplacer(createMetaDataReplacer(connectorModel));
        sourceConnector.setChannel(channel);

        if (connectorProperties instanceof SourceConnectorPropertiesInterface) {
            SourceConnectorProperties sourceConnectorProperties = ((SourceConnectorPropertiesInterface) connectorProperties).getSourceConnectorProperties();
            sourceConnector.setRespondAfterProcessing(sourceConnectorProperties.isRespondAfterProcessing());

            DataTypeServerPlugin dataTypePlugin = ExtensionController.getInstance().getDataTypePlugins().get(connectorModel.getTransformer().getInboundDataType());
            DataTypeProperties dataTypeProperties = connectorModel.getTransformer().getInboundProperties();
            SerializerProperties serializerProperties = dataTypeProperties.getSerializerProperties();
            BatchProperties batchProperties = serializerProperties.getBatchProperties();

            if (batchProperties != null && sourceConnectorProperties.isProcessBatch()) {
                BatchAdaptorFactory batchAdaptorFactory = dataTypePlugin.getBatchAdaptorFactory(sourceConnector, serializerProperties);
                batchAdaptorFactory.setUseFirstReponse(sourceConnectorProperties.isFirstResponse());
                sourceConnector.setBatchAdaptorFactory(batchAdaptorFactory);
            }
        } else {
            sourceConnector.setRespondAfterProcessing(true);
        }

        return sourceConnector;
    }

    private FilterTransformerExecutor createFilterTransformerExecutor(String channelId, com.mirth.connect.model.Connector connectorModel, Map<String, Integer> destinationIdMap) throws Exception {
        boolean runFilterTransformer = false;
        String template = null;
        Transformer transformer = connectorModel.getTransformer();
        Filter filter = connectorModel.getFilter();

        DataType inboundDataType = DataTypeFactory.getDataType(transformer.getInboundDataType(), transformer.getInboundProperties());
        DataType outboundDataType = DataTypeFactory.getDataType(transformer.getOutboundDataType(), transformer.getOutboundProperties());

        // Check the conditions for skipping transformation
        // 1. Script is not empty
        // 2. Data Types are different
        // 3. The data type has properties settings that require a transformation
        // 4. The outbound template is not empty        

        if (!filter.getRules().isEmpty() || !transformer.getSteps().isEmpty() || !transformer.getInboundDataType().equals(transformer.getOutboundDataType())) {
            runFilterTransformer = true;
        }

        // Ask the inbound serializer if it needs to be transformed with serialization
        if (!runFilterTransformer) {
            runFilterTransformer = inboundDataType.getSerializer().isSerializationRequired(true);
        }

        // Ask the outbound serializier if it needs to be transformed with serialization
        if (!runFilterTransformer) {
            runFilterTransformer = outboundDataType.getSerializer().isSerializationRequired(false);
        }

        // Serialize the outbound template if needed
        if (StringUtils.isNotBlank(transformer.getOutboundTemplate())) {
            DataTypeServerPlugin outboundServerPlugin = ExtensionController.getInstance().getDataTypePlugins().get(transformer.getOutboundDataType());
            XmlSerializer serializer = outboundServerPlugin.getSerializer(transformer.getOutboundProperties().getSerializerProperties());

            if (outboundServerPlugin.isBinary() || outboundServerPlugin.getSerializationType() == SerializationType.RAW) {
                template = transformer.getOutboundTemplate();
            } else {
                try {
                    template = serializer.toXML(transformer.getOutboundTemplate());
                } catch (XmlSerializerException e) {
                    throw new XmlSerializerException("Error serializing transformer outbound template for connector \"" + connectorModel.getName() + "\": " + e.getMessage(), e.getCause(), e.getFormattedError());
                }
            }

            runFilterTransformer = true;
        }

        FilterTransformerExecutor filterTransformerExecutor = new FilterTransformerExecutor(inboundDataType, outboundDataType);

        if (runFilterTransformer) {
            String script = JavaScriptBuilder.generateFilterTransformerScript(filter, transformer);
            filterTransformerExecutor.setFilterTransformer(new JavaScriptFilterTransformer(channelId, connectorModel.getName(), script, template, destinationIdMap));
        }

        return filterTransformerExecutor;
    }

    private ResponseTransformerExecutor createResponseTransformerExecutor(String channelId, com.mirth.connect.model.Connector connectorModel, Map<String, Integer> destinationIdMap) throws Exception {
        boolean runResponseTransformer = false;
        String template = null;
        Transformer transformer = connectorModel.getResponseTransformer();

        DataType inboundDataType = DataTypeFactory.getDataType(transformer.getInboundDataType(), transformer.getInboundProperties());
        DataType outboundDataType = DataTypeFactory.getDataType(transformer.getOutboundDataType(), transformer.getOutboundProperties());

        // Check the conditions for skipping transformation
        // 1. Script is not empty
        // 2. Data Types are different
        // 3. The data type has properties settings that require a transformation
        // 4. The outbound template is not empty        

        if (!transformer.getSteps().isEmpty() || !transformer.getInboundDataType().equals(transformer.getOutboundDataType())) {
            runResponseTransformer = true;
        }

        // Ask the inbound serializer if it needs to be transformed with serialization
        if (!runResponseTransformer) {
            runResponseTransformer = inboundDataType.getSerializer().isSerializationRequired(true);
        }

        // Ask the outbound serializier if it needs to be transformed with serialization
        if (!runResponseTransformer) {
            runResponseTransformer = outboundDataType.getSerializer().isSerializationRequired(false);
        }

        // Serialize the outbound template if needed
        if (StringUtils.isNotBlank(transformer.getOutboundTemplate())) {
            DataTypeServerPlugin outboundServerPlugin = ExtensionController.getInstance().getDataTypePlugins().get(transformer.getOutboundDataType());
            XmlSerializer serializer = outboundServerPlugin.getSerializer(transformer.getOutboundProperties().getSerializerProperties());

            if (outboundServerPlugin.isBinary() || outboundServerPlugin.getSerializationType() == SerializationType.RAW) {
                template = transformer.getOutboundTemplate();
            } else {
                try {
                    template = serializer.toXML(transformer.getOutboundTemplate());
                } catch (XmlSerializerException e) {
                    throw new XmlSerializerException("Error serializing response transformer outbound template for connector \"" + connectorModel.getName() + "\": " + e.getMessage(), e.getCause(), e.getFormattedError());
                }
            }

            runResponseTransformer = true;
        }

        ResponseTransformerExecutor responseTransformerExecutor = new ResponseTransformerExecutor(inboundDataType, outboundDataType);

        if (runResponseTransformer) {
            String script = JavaScriptBuilder.generateResponseTransformerScript(transformer);
            responseTransformerExecutor.setResponseTransformer(new JavaScriptResponseTransformer(channelId, connectorModel.getName(), script, template, destinationIdMap));
        }

        return responseTransformerExecutor;
    }

    private DestinationChain createDestinationChain(Channel channel) {
        DestinationChain chain = new DestinationChain();
        chain.setChannelId(channel.getChannelId());
        chain.setMetaDataReplacer(channel.getSourceConnector().getMetaDataReplacer());
        chain.setMetaDataColumns(channel.getMetaDataColumns());

        return chain;
    }

    private DestinationConnector createDestinationConnector(Channel channel, com.mirth.connect.model.Connector connectorModel, StorageSettings storageSettings, Map<String, Integer> destinationIdMap) throws Exception {
        ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
        ConnectorProperties connectorProperties = connectorModel.getProperties();
        ConnectorMetaData connectorMetaData = extensionController.getConnectorMetaData().get(connectorProperties.getName());
        String className = connectorMetaData.getServerClassName();
        DestinationConnector destinationConnector = (DestinationConnector) Class.forName(className).newInstance();

        setCommonConnectorProperties(channel.getChannelId(), destinationConnector, connectorModel, destinationIdMap);
        destinationConnector.setChannel(channel);

        destinationConnector.setDestinationName(connectorModel.getName());

        // Create the response validator
        DataTypeServerPlugin dataTypePlugin = ExtensionController.getInstance().getDataTypePlugins().get(connectorModel.getResponseTransformer().getInboundDataType());
        DataTypeProperties dataTypeProperties = connectorModel.getResponseTransformer().getInboundProperties();
        SerializerProperties serializerProperties = dataTypeProperties.getSerializerProperties();
        ResponseValidator responseValidator = dataTypePlugin.getResponseValidator(serializerProperties.getSerializationProperties(), dataTypeProperties.getResponseValidationProperties());
        if (responseValidator == null) {
            responseValidator = new DefaultResponseValidator();
        }
        destinationConnector.setResponseValidator(responseValidator);
        destinationConnector.setResponseTransformerExecutor(createResponseTransformerExecutor(channel.getChannelId(), connectorModel, destinationIdMap));

        ConnectorMessageQueue queue = new ConnectorMessageQueue();
        queue.setBufferCapacity(queueBufferSize);
        queue.setRotate(destinationConnector.isQueueRotate());
        destinationConnector.setQueue(queue);

        return destinationConnector;
    }

    private void setCommonConnectorProperties(String channelId, Connector connector, com.mirth.connect.model.Connector connectorModel, Map<String, Integer> destinationIdMap) {
        connector.setChannelId(channelId);
        connector.setMetaDataId(connectorModel.getMetaDataId());
        connector.setConnectorProperties(connectorModel.getProperties());
        connector.setDestinationIdMap(destinationIdMap);

        Transformer transformerModel = connectorModel.getTransformer();

        connector.setInboundDataType(DataTypeFactory.getDataType(transformerModel.getInboundDataType(), transformerModel.getInboundProperties()));
        connector.setOutboundDataType(DataTypeFactory.getDataType(transformerModel.getOutboundDataType(), transformerModel.getOutboundProperties()));
    }

    private MetaDataReplacer createMetaDataReplacer(com.mirth.connect.model.Connector connectorModel) {
        // TODO: Extract this from the Connector model based on the inbound data type
        return new MirthMetaDataReplacer();
    }

    private void clearGlobalChannelMap(com.mirth.connect.model.Channel channelModel) {
        if (channelModel.getProperties().isClearGlobalChannelMap()) {
            logger.debug("clearing global channel map for channel: " + channelModel.getId());
            GlobalChannelVariableStoreFactory.getInstance().get(channelModel.getId()).clear();
            GlobalChannelVariableStoreFactory.getInstance().get(channelModel.getId()).clearSync();
        }
    }

    private void clearGlobalMap() {
        try {
            if (configurationController.getServerSettings().getClearGlobalMap() == null || configurationController.getServerSettings().getClearGlobalMap()) {
                logger.debug("clearing global map");
                GlobalVariableStore globalVariableStore = GlobalVariableStore.getInstance();
                globalVariableStore.clear();
                globalVariableStore.clearSync();
            }
        } catch (ControllerException e) {
            logger.error("Could not clear the global map.", e);
        }
    }

    protected void executeGlobalDeployScript() {
        try {
            scriptController.executeGlobalDeployScript();
        } catch (Exception e) {
            logger.error("Error executing global deploy script.", e);
        }
    }

    protected void executeGlobalUndeployScript() {
        try {
            scriptController.executeGlobalUndeployScript();
        } catch (Exception e) {
            logger.error("Error executing global undeploy script.", e);
        }
    }

    protected void executeChannelPluginOnDeploy(ServerEventContext context) {
        // Execute the overall channel plugin deploy hook
        for (ChannelPlugin channelPlugin : extensionController.getChannelPlugins().values()) {
            channelPlugin.deploy(context);
        }
    }

    protected void executeChannelPluginOnUndeploy(ServerEventContext context) {
        // Execute the overall channel plugin undeploy hook
        for (ChannelPlugin channelPlugin : extensionController.getChannelPlugins().values()) {
            channelPlugin.undeploy(context);
        }
    }

    private void shutdownExecutor(String channelId) {
        ExecutorService engineExecutor = engineExecutors.get(channelId);

        if (engineExecutor != null) {
            List<Runnable> tasks = engineExecutor.shutdownNow();
            // Cancel any tasks that had not yet started. Otherwise those tasks would be blocked at future.get() indefinitely.
            for (Runnable task : tasks) {
                ((Future<?>) task).cancel(true);
            }
        }
    }

    private List<ChannelTask> buildChannelStatusTasks(Set<String> channelIds, StatusTask task) {
        List<ChannelTask> tasks = new ArrayList<ChannelTask>();

        for (String channelId : channelIds) {
            tasks.add(new ChannelStatusTask(channelId, task));
        }

        return tasks;
    }

    private List<ChannelTask> buildConnectorStatusTasks(Map<String, List<Integer>> connectorInfo, StatusTask task) {
        List<ChannelTask> tasks = new ArrayList<ChannelTask>();

        for (Entry<String, List<Integer>> entry : connectorInfo.entrySet()) {
            String channelId = entry.getKey();
            List<Integer> metaDataIds = entry.getValue();

            for (Integer metaDataId : metaDataIds) {
                tasks.add(new ConnectorStatusTask(channelId, metaDataId, task));
            }
        }

        return tasks;
    }

    @Override
    public synchronized List<ChannelFuture> submitTasks(List<ChannelTask> tasks, ChannelTaskHandler handler) {
        List<ChannelFuture> futures = new ArrayList<ChannelFuture>();

        /*
         * If no handler is given then use the default handler to that at least errors will be
         * logged out.
         */
        if (handler == null) {
            handler = new LoggingTaskHandler();
        }

        for (ChannelTask task : tasks) {
            ExecutorService engineExecutor = engineExecutors.get(task.getChannelId());

            if (engineExecutor == null) {
                engineExecutor = new ThreadPoolExecutor(0, 1, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
                engineExecutors.put(task.getChannelId(), engineExecutor);
            }

            task.setHandler(handler);
            try {
                futures.add(task.submitTo(engineExecutor));
            } catch (RejectedExecutionException e) {
                /*
                 * This can happen if a channel was halted, in which case we don't want to perform
                 * whatever task this was anyway.
                 */
                handler.taskErrored(task.getChannelId(), task.getMetaDataId(), e);
            }
        }

        return futures;
    }

    private List<ChannelFuture> submitHaltTasks(Set<String> channelIds, ChannelTaskHandler handler) {
        List<ChannelFuture> futures = new ArrayList<ChannelFuture>();

        /*
         * If no handler is given then use the default handler to that at least errors will be
         * logged out.
         */
        if (handler == null) {
            handler = new LoggingTaskHandler();
        }

        for (String channelId : channelIds) {
            /*
             * Shutdown the executor to prevent any new tasks from being submitted. This needs to be
             * called once outside of the synchronized block in order to halt certain actions such
             * as restoring server configuration.
             */
            shutdownExecutor(channelId);

            synchronized (this) {
                /*
                 * Shutdown the executor to prevent any new tasks from being submitted. This needs
                 * to be called once inside the synchronized block in case multiple halts were
                 * performed.
                 */
                shutdownExecutor(channelId);

                /*
                 * Create a new executor to submit the halt task to. Since all the submit methods
                 * are synchronized, it is not possible for any other tasks for this channel to
                 * occur before the halt task.
                 */
                ExecutorService engineExecutor = new ThreadPoolExecutor(0, 1, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
                engineExecutors.put(channelId, engineExecutor);

                ChannelTask haltTask = new HaltTask(channelId);
                haltTask.setHandler(handler);
                futures.add(haltTask.submitTo(engineExecutor));
            }

        }
        return futures;
    }

    protected void waitForTasks(List<ChannelFuture> futures) {
        /*
         * Create a new list to prevent modifying the one that is passed in, in case it will be used
         * afterwards.
         */
        List<ChannelFuture> remainingFutures = new ArrayList<ChannelFuture>(futures);

        int attemptsUntilPause = 10;
        while (CollectionUtils.isNotEmpty(remainingFutures)) {
            if (attemptsUntilPause > 0) {
                attemptsUntilPause--;
            } else {
                /*
                 * After 10 attempts we will pause longer to lighten the CPU load during long
                 * running tasks.
                 */
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }

            Iterator<ChannelFuture> iterator = remainingFutures.iterator();
            while (iterator.hasNext()) {
                boolean finished = false;
                ChannelFuture future = iterator.next();

                try {
                    if (remainingFutures.size() == 1) {
                        // Wait indefinitely when only one future remains.
                        future.get();
                    } else {
                        // When multiple futures remain, timeout the wait so we can check others in the meantime.
                        future.get(50, TimeUnit.MILLISECONDS);
                    }
                    finished = true;
                } catch (TimeoutException e) {
                } finally {
                    if (finished) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    protected class DeployTask extends ChannelTask {

        private DeployedState initialState;
        private Set<Integer> connectorsToStart;
        private ServerEventContext context;

        public DeployTask(String channelId, DeployedState initialState, Set<Integer> connectorsToStart, ServerEventContext context) {
            super(channelId);
            this.initialState = initialState;
            this.connectorsToStart = connectorsToStart;
            this.context = context;
        }

        @Override
        public Void execute() throws Exception {
            com.mirth.connect.model.Channel channelModel = channelController.getChannelById(channelId);

            if (channelModel == null || !channelModel.isEnabled() || isDeployed(channelId)) {
                return null;
            }

            Channel channel = null;

            try {
                channel = createChannelFromModel(channelModel);
            } catch (Exception e) {
                throw new DeployException(e.getMessage(), e);
            }

            try {
                channel.updateCurrentState(DeployedState.DEPLOYING);
                deployingChannels.add(channel);
                channelController.putDeployedChannelInCache(channelModel);

                try {
                    scriptController.compileChannelScripts(channelModel);
                } catch (ScriptCompileException e) {
                    throw new DeployException("Failed to deploy channel " + channelId + ".", e);
                }

                clearGlobalChannelMap(channelModel);

                try {
                    scriptController.executeChannelDeployScript(channelId);
                } catch (Exception e) {
                    Throwable t = e;
                    if (e instanceof JavaScriptExecutorException) {
                        t = e.getCause();
                    }

                    eventController.dispatchEvent(new ErrorEvent(channelModel.getId(), null, ErrorEventType.DEPLOY_SCRIPT, null, null, "Error running channel deploy script", t));
                    throw new DeployException("Failed to deploy channel " + channelId + ".", e);
                }

                // Execute the individual channel plugin deploy hook
                for (ChannelPlugin channelPlugin : extensionController.getChannelPlugins().values()) {
                    channelPlugin.deploy(channelModel, context);
                }

                // TODO This may not be necessary anymore
                channel.setRevision(channelModel.getRevision());

                channel.setDeployDate(Calendar.getInstance());
                donkey.getDeployedChannels().put(channelId, channel);

                try {
                    channel.deploy();
                } catch (DeployException e) {
                    donkey.getDeployedChannels().remove(channelId);
                    throw e;
                }

                // Use the initial state from the channel settings if none are provided
                if (initialState == null) {
                    initialState = channel.getInitialState();
                }

                // Use all connectors if none are provided
                if (connectorsToStart == null) {
                    connectorsToStart = new HashSet<Integer>(channel.getMetaDataIds());
                }

                if (initialState == DeployedState.PAUSED) {
                    // If the initial state is paused, never start the source connector
                    connectorsToStart.remove(0);
                } else if (initialState == DeployedState.STOPPED) {
                    // If the initial state is stopped, never start any connector
                    connectorsToStart.clear();
                }

                // For connectors that won't be started, update their state to stopped to dispatch their event
                if (!connectorsToStart.contains(0)) {
                    channel.getSourceConnector().updateCurrentState(DeployedState.STOPPED);
                }
                for (DestinationChain destinationChain : channel.getDestinationChains()) {
                    for (Entry<Integer, DestinationConnector> entry : destinationChain.getDestinationConnectors().entrySet()) {
                        if (!connectorsToStart.contains(entry.getKey())) {
                            entry.getValue().updateCurrentState(DeployedState.STOPPED);
                        }
                    }
                }

                if (initialState == DeployedState.STOPPED) {
                    // If the initial state is stopped, update the channel's state to dispatch its event
                    channel.updateCurrentState(DeployedState.STOPPED);
                } else {
                    // Unless the initial state is stopped, always start the channel
                    channel.start(connectorsToStart);
                }
            } catch (DeployException e) {
                // Remove the channel from the deployed channel cache if an exception occurred on deploy.
                channelController.removeDeployedChannelFromCache(channelId);
                // Remove the channel scripts from the script cache if an exception occurred on deploy.
                scriptController.removeChannelScriptsFromCache(channelId);

                throw e;
            } finally {
                deployingChannels.remove(channel);
            }

            return null;
        }
    }

    protected class UndeployTask extends ChannelTask {

        private ServerEventContext context;

        public UndeployTask(String channelId, ServerEventContext context) {
            super(channelId);
            this.context = context;
        }

        @Override
        public Void execute() throws Exception {
            // Get a reference to the deployed channel for later
            Channel channel = getDeployedChannel(channelId);

            if (channel != null) {
                if (channel.isActive()) {
                    channel.stop();
                }

                try {
                    undeployingChannels.add(channel);
                    donkey.getDeployedChannels().remove(channelId);
                    channel.undeploy();

                    // Remove connector scripts
                    if (channel.getSourceFilterTransformer().getFilterTransformer() != null) {
                        channel.getSourceFilterTransformer().getFilterTransformer().dispose();
                    }

                    for (DestinationChain chain : channel.getDestinationChains()) {
                        for (Integer metaDataId : chain.getDestinationConnectors().keySet()) {
                            if (chain.getFilterTransformerExecutors().get(metaDataId).getFilterTransformer() != null) {
                                chain.getFilterTransformerExecutors().get(metaDataId).getFilterTransformer().dispose();
                            }
                            if (chain.getDestinationConnectors().get(metaDataId).getResponseTransformerExecutor().getResponseTransformer() != null) {
                                chain.getDestinationConnectors().get(metaDataId).getResponseTransformerExecutor().getResponseTransformer().dispose();
                            }
                        }
                    }

                    // Execute the individual channel plugin undeploy hook
                    for (ChannelPlugin channelPlugin : extensionController.getChannelPlugins().values()) {
                        channelPlugin.undeploy(channelId, context);
                    }

                    // Execute channel undeploy script
                    try {
                        scriptController.executeChannelUndeployScript(channelId);
                    } catch (Exception e) {
                        Throwable t = e;
                        if (e instanceof JavaScriptExecutorException) {
                            t = e.getCause();
                        }

                        eventController.dispatchEvent(new ErrorEvent(channelId, null, ErrorEventType.UNDEPLOY_SCRIPT, null, null, "Error running channel undeploy script", t));
                        logger.error("Error executing undeploy script for channel " + channelId + ".", e);
                    }

                    // Remove channel scripts
                    scriptController.removeChannelScriptsFromCache(channelId);

                    channelController.removeDeployedChannelFromCache(channelId);
                } finally {
                    undeployingChannels.remove(channel);
                }
            }

            return null;
        }
    }

    protected class ChannelStatusTask extends ChannelTask {

        private StatusTask task;

        public ChannelStatusTask(String channelId, StatusTask task) {
            super(channelId);
            this.task = task;
        }

        @Override
        public Void execute() throws Exception {
            Channel channel = getDeployedChannel(channelId);

            if (channel != null) {
                if (task == StatusTask.START) {
                    channel.start(null);
                } else if (task == StatusTask.STOP) {
                    channel.stop();
                } else if (task == StatusTask.PAUSE) {
                    channel.pause();
                } else if (task == StatusTask.RESUME) {
                    channel.resume();
                }
            }

            return null;
        }
    }

    protected class ConnectorStatusTask extends ChannelTask {

        private StatusTask task;

        public ConnectorStatusTask(String channelId, Integer metaDataId, StatusTask task) {
            super(channelId, metaDataId);
            this.task = task;
        }

        @Override
        public Void execute() throws Exception {
            Channel channel = getDeployedChannel(channelId);

            if (channel != null) {
                if (task == StatusTask.START) {
                    channel.startConnector(metaDataId);
                } else if (task == StatusTask.STOP) {
                    channel.stopConnector(metaDataId);
                }
            }

            return null;
        }
    }

    protected class HaltTask extends ChannelTask {

        public HaltTask(String channelId) {
            super(channelId);
        }

        @Override
        public Void execute() throws Exception {
            Channel channel = getDeployedChannel(channelId);

            if (channel != null) {
                channel.halt();
            }

            return null;
        }
    }

    protected class RemoveTask extends ChannelTask {

        private com.mirth.connect.model.Channel channelModel;
        private ServerEventContext context;

        public RemoveTask(com.mirth.connect.model.Channel channelModel, ServerEventContext context) {
            super(channelModel.getId());
            this.channelModel = channelModel;
            this.context = context;
        }

        @Override
        public Void execute() throws Exception {
            channelController.removeChannel(channelModel, context);

            synchronized (DonkeyEngineController.this) {
                // Shutdown the executor to prevent any new tasks from being submitted.
                shutdownExecutor(channelId);

                // Remove the executor since it has been shutdown. If another task comes in for this channel Id, a new executor will be created.
                engineExecutors.remove(channelId);
            }

            return null;
        }
    }

    protected class RemoveMessagesTask extends ChannelTask {

        private Map<Long, MessageSearchResult> results;

        public RemoveMessagesTask(String channelId, Map<Long, MessageSearchResult> results) {
            super(channelId);
            this.results = results;
        }

        @Override
        public Void execute() throws Exception {
            Map<Long, Set<Integer>> messages = new HashMap<Long, Set<Integer>>();

            // For each message that was retrieved
            for (Entry<Long, MessageSearchResult> entry : results.entrySet()) {
                Long messageId = entry.getKey();
                MessageSearchResult result = entry.getValue();
                Set<Integer> metaDataIds = result.getMetaDataIdSet();
                boolean processed = result.isProcessed();

                Channel channel = getDeployedChannel(channelId);
                // Allow unprocessed messages to be deleted only if the channel is undeployed or stopped.
                if (channel != null && (channel.getCurrentState() == DeployedState.STOPPED || processed)) {
                    if (metaDataIds.contains(0)) {
                        // Delete the entire message if the source connector message is to be deleted
                        messages.put(messageId, null);
                    } else {
                        // Otherwise only deleted the destination connector message
                        messages.put(messageId, metaDataIds);
                    }
                }
            }

            Channel.DELETE_PERMIT.acquire();

            try {
                com.mirth.connect.donkey.server.controllers.MessageController.getInstance().deleteMessages(channelId, messages);
            } finally {
                Channel.DELETE_PERMIT.release();
            }

            return null;
        }
    }

    protected class RemoveAllMessagesTask extends ChannelTask {

        private boolean force;
        private boolean clearStatistics;

        public RemoveAllMessagesTask(String channelId, boolean force, boolean clearStatistics) {
            super(channelId);
            this.force = force;
            this.clearStatistics = clearStatistics;
        }

        @Override
        public Void execute() throws Exception {
            Channel channel = getDeployedChannel(channelId);

            if (channel != null) {
                channel.removeAllMessages(force, clearStatistics);
            }

            return null;
        }
    }
}
