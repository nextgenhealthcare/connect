/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.connect.donkey.model.channel.ChannelState;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.QueueConnectorProperties;
import com.mirth.connect.donkey.model.channel.QueueConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.ResponseConnectorProperties;
import com.mirth.connect.donkey.model.channel.ResponseConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.SerializationType;
import com.mirth.connect.donkey.model.message.SerializerException;
import com.mirth.connect.donkey.model.message.XmlSerializer;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.DonkeyConfiguration;
import com.mirth.connect.donkey.server.PauseException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.ChannelException;
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
import com.mirth.connect.donkey.server.message.DataType;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelProperties;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.DashboardStatus.StatusType;
import com.mirth.connect.model.Filter;
import com.mirth.connect.model.MessageStorageMode;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.model.attachments.AttachmentHandlerFactory;
import com.mirth.connect.plugins.ChannelPlugin;
import com.mirth.connect.plugins.DataTypeServerPlugin;
import com.mirth.connect.server.attachments.JavaScriptAttachmentHandler;
import com.mirth.connect.server.builders.JavaScriptBuilder;
import com.mirth.connect.server.channel.MirthMetaDataReplacer;
import com.mirth.connect.server.message.DataTypeFactory;
import com.mirth.connect.server.transformers.JavaScriptFilterTransformer;
import com.mirth.connect.server.transformers.JavaScriptPostprocessor;
import com.mirth.connect.server.transformers.JavaScriptPreprocessor;
import com.mirth.connect.server.transformers.JavaScriptResponseTransformer;
import com.mirth.connect.server.util.GlobalChannelVariableStoreFactory;
import com.mirth.connect.server.util.GlobalVariableStore;
import com.mirth.connect.server.util.JavaScriptUtil;

public class DonkeyEngineController implements EngineController {
    private static DonkeyEngineController instance = null;

    public static DonkeyEngineController getInstance() {
        synchronized (DonkeyEngineController.class) {
            if (instance == null) {
                instance = new DonkeyEngineController();
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
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();

    private DonkeyEngineController() {}

    @Override
    public void startEngine() throws StartException, StopException, ControllerException, InterruptedException {
        logger.debug("starting donkey engine");

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

        donkey.startEngine(new DonkeyConfiguration(configurationController.getApplicationDataDir(), configurationController.getDatabaseSettings().getProperties(), donkeyEncryptor));
    }

    @Override
    public void stopEngine() throws StopException, InterruptedException {
        undeployChannels(donkey.getDeployedChannelIds(), ServerEventContext.SYSTEM_USER_EVENT_CONTEXT);
        donkey.stopEngine();
    }

    @Override
    public boolean isRunning() {
        return donkey.isRunning();
    }

    @Override
    public void deployChannel(String channelId, ServerEventContext context) throws StartException, StopException, DeployException, UndeployException {
        Channel channel = channelController.getCachedChannelById(channelId);

        if (channel == null) {
            throw new DeployException("Unable to deploy channel, channel ID " + channelId + " not found.");
        }

        if (!channel.isEnabled()) {
            return;
        }

        if (donkey.getDeployedChannels().containsKey(channelId)) {
            undeployChannel(channelId, context);
        }

        com.mirth.connect.donkey.server.channel.Channel donkeyChannel = null;

        try {
            donkeyChannel = convertToDonkeyChannel(channel);
        } catch (Exception e) {
            throw new DeployException(e.getMessage(), e);
        }

        for (ChannelPlugin channelPlugin : extensionController.getChannelPlugins().values()) {
            channelPlugin.deploy(context);
        }

        try {
            scriptController.compileChannelScripts(channel);
        } catch (ScriptCompileException e) {
            throw new StartException("Failed to deploy channel " + channelId + ".", e);
        }

        clearGlobalChannelMap(channel);

        try {
            scriptController.executeChannelDeployScript(channel.getId());
        } catch (Exception e) {
            throw new StartException("Failed to deploy channel " + channelId + ".", e);
        }
        channelController.putDeployedChannelInCache(channel);

        for (ChannelPlugin channelPlugin : extensionController.getChannelPlugins().values()) {
            channelPlugin.deploy(channel, context);
        }

        donkeyChannel.setRevision(channel.getRevision());

        donkey.deployChannel(donkeyChannel);
    }

    @Override
    public void deployChannels(List<String> channelIds, ServerEventContext context) {
        if (channelIds == null) {
            throw new NullPointerException();
        }

        // Execute global deploy script before channel deploy script
        scriptController.executeGlobalDeployScript();

        for (String channelId : channelIds) {
            try {
                deployChannel(channelId, context);
            } catch (Exception e) {
                logger.error("Error deploying channel " + channelId + ".", e);
            }
        }
    }

    @Override
    public void undeployChannel(String channelId, ServerEventContext context) throws StopException, UndeployException {
        // Get a reference to the deployed channel for later
        com.mirth.connect.donkey.server.channel.Channel channel = getDeployedChannel(channelId);

        donkey.undeployChannel(channelId);

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

        // Execute channel shutdown script
        try {
            scriptController.executeChannelShutdownScript(channelId);
        } catch (Exception e) {
            logger.error("Error executing shutdown script for channel " + channelId + ".", e);
        }

        // Remove channel scripts
        scriptController.removeChannelScriptsFromCache(channelId);
    }

    @Override
    public void undeployChannels(List<String> channelIds, ServerEventContext context) throws InterruptedException {
        for (String channelId : channelIds) {
            try {
                undeployChannel(channelId, context);
            } catch (Exception e) {
                logger.error("Error undeploying channel " + channelId + ".", e);
            }
        }

        // Execute global shutdown script
        scriptController.executeGlobalShutdownScript();
    }

    @Override
    public void redeployAllChannels() throws StartException, StopException, InterruptedException {
        undeployChannels(donkey.getDeployedChannelIds(), ServerEventContext.SYSTEM_USER_EVENT_CONTEXT);
        clearGlobalMap();
        deployChannels(channelController.getCachedChannelIds(), ServerEventContext.SYSTEM_USER_EVENT_CONTEXT);
    }

    @Override
    public void startChannel(String channelId) throws StartException {
        donkey.startChannel(channelId);
    }

    @Override
    public void stopChannel(String channelId) throws StopException {
        donkey.stopChannel(channelId);
    }

    @Override
    public void haltChannel(String channelId) throws StopException {
        donkey.haltChannel(channelId);
    }

    @Override
    public void pauseChannel(String channelId) throws PauseException {
        donkey.pauseChannel(channelId);
    }

    @Override
    public void resumeChannel(String channelId) throws StartException, StopException {
        donkey.resumeChannel(channelId);
    }

    @Override
    public List<DashboardStatus> getChannelStatusList() {
        List<DashboardStatus> statuses = new ArrayList<DashboardStatus>();

        for (com.mirth.connect.donkey.server.channel.Channel donkeyChannel : donkey.getDeployedChannels().values()) {
            Statistics stats = donkeyChannelController.getStatistics();
            Statistics lifetimeStats = donkeyChannelController.getTotalStatistics();

            DashboardStatus status = new DashboardStatus();
            status.setStatusType(StatusType.CHANNEL);
            status.setChannelId(donkeyChannel.getChannelId());
            status.setName(donkeyChannel.getName());

            if (donkeyChannel.isPaused()) {
                status.setState(ChannelState.PAUSED);
            } else {
                status.setState(donkeyChannel.getCurrentState());
            }

            status.setDeployedDate(donkeyChannel.getDeployDate());

            Channel channel = channelController.getCachedChannelById(donkeyChannel.getChannelId());
            status.setDeployedRevisionDelta(channel.getRevision() - donkeyChannel.getRevision());
            status.setStatistics(stats.getConnectorStats(donkeyChannel.getChannelId(), null));
            status.setLifetimeStatistics(lifetimeStats.getConnectorStats(donkeyChannel.getChannelId(), null));
            status.setTags(channel.getTags());

            DashboardStatus sourceStatus = new DashboardStatus();
            sourceStatus.setStatusType(StatusType.SOURCE_CONNECTOR);
            sourceStatus.setChannelId(donkeyChannel.getChannelId());
            sourceStatus.setMetaDataId(0);
            sourceStatus.setName("Source");
            sourceStatus.setState(donkeyChannel.getSourceConnector().getCurrentState());
            sourceStatus.setStatistics(stats.getConnectorStats(donkeyChannel.getChannelId(), 0));
            sourceStatus.setLifetimeStatistics(lifetimeStats.getConnectorStats(donkeyChannel.getChannelId(), 0));
            sourceStatus.setTags(channel.getTags());
            sourceStatus.setQueued(new Long(donkeyChannel.getSourceQueue().size()));

            status.setQueued(sourceStatus.getQueued());

            status.getChildStatuses().add(sourceStatus);

            for (DestinationChain chain : donkeyChannel.getDestinationChains()) {
                for (Entry<Integer, DestinationConnector> connectorEntry : chain.getDestinationConnectors().entrySet()) {
                    Integer metaDataId = connectorEntry.getKey();
                    DestinationConnector connector = connectorEntry.getValue();

                    DashboardStatus destinationStatus = new DashboardStatus();
                    destinationStatus.setStatusType(StatusType.DESTINATION_CONNECTOR);
                    destinationStatus.setChannelId(donkeyChannel.getChannelId());
                    destinationStatus.setMetaDataId(metaDataId);
                    destinationStatus.setName(connector.getDestinationName());
                    destinationStatus.setState(connector.getCurrentState());
                    destinationStatus.setStatistics(stats.getConnectorStats(donkeyChannel.getChannelId(), metaDataId));
                    destinationStatus.setLifetimeStatistics(lifetimeStats.getConnectorStats(donkeyChannel.getChannelId(), metaDataId));
                    destinationStatus.setTags(channel.getTags());
                    destinationStatus.setQueued(new Long(connector.getQueue().size()));

                    status.setQueued(status.getQueued() + destinationStatus.getQueued());

                    status.getChildStatuses().add(destinationStatus);
                }
            }

            statuses.add(status);
        }

        Collections.sort(statuses, new Comparator<DashboardStatus>() {

            public int compare(DashboardStatus o1, DashboardStatus o2) {
                Calendar c1 = o1.getDeployedDate();
                Calendar c2 = o2.getDeployedDate();

                return c1.compareTo(c2);
            }

        });

        return statuses;
    }

    @Override
    public List<String> getDeployedIds() {
        return donkey.getDeployedChannelIds();
    }

    @Override
    public boolean isDeployed(String channelId) {
        return donkey.getDeployedChannels().containsKey(channelId);
    }

    @Override
    public com.mirth.connect.donkey.server.channel.Channel getDeployedChannel(String channelId) {
        return donkey.getDeployedChannels().get(channelId);
    }

    @Override
    public DispatchResult dispatchRawMessage(String channelId, RawMessage rawMessage) throws ChannelException {
        if (!isDeployed(channelId)) {
            ChannelException e = new ChannelException(true);
            logger.error("Could not find channel to route to: " + channelId, e);
            throw e;
        }

        SourceConnector sourceConnector = donkey.getDeployedChannels().get(channelId).getSourceConnector();
        DispatchResult dispatchResult = null;

        try {
            dispatchResult = sourceConnector.dispatchRawMessage(rawMessage);
        } finally {
            sourceConnector.finishDispatch(dispatchResult, true, null);
        }

        return dispatchResult;
    }

    private com.mirth.connect.donkey.server.channel.Channel convertToDonkeyChannel(Channel model) throws Exception {
        String channelId = model.getId();
        ChannelProperties channelProperties = model.getProperties();
        StorageSettings storageSettings = getStorageSettings(channelProperties.getMessageStorageMode(), channelProperties);

        com.mirth.connect.donkey.server.channel.Channel channel = new com.mirth.connect.donkey.server.channel.Channel();

        channel.setChannelId(channelId);
        channel.setServerId(ConfigurationController.getInstance().getServerId());
        channel.setName(model.getName());
        channel.setEnabled(model.isEnabled());
        channel.setRevision(model.getRevision());
        channel.setVersion(model.getVersion());
        channel.setInitialState(channelProperties.isInitialStateStarted() ? ChannelState.STARTED : ChannelState.STOPPED);
        channel.setStorageSettings(storageSettings);
        channel.setMetaDataColumns(channelProperties.getMetaDataColumns());
        channel.setAttachmentHandler(createAttachmentHandler(channelId, channelProperties.getAttachmentProperties()));
        channel.setPreProcessor(createPreProcessor(channelId, model.getPreprocessingScript()));
        channel.setPostProcessor(createPostProcessor(channelId, model.getPostprocessingScript()));
        channel.setSourceConnector(createSourceConnector(channel, model.getSourceConnector(), storageSettings));
        channel.setResponseSelector(new ResponseSelector(channel.getSourceConnector().getInboundDataType()));
        channel.setSourceFilterTransformer(createFilterTransformerExecutor(channelId, model.getSourceConnector()));

        if (model.getSourceConnector().getProperties() instanceof ResponseConnectorPropertiesInterface) {
            ResponseConnectorProperties responseConnectorProperties = ((ResponseConnectorPropertiesInterface) model.getSourceConnector().getProperties()).getResponseConnectorProperties();
            channel.getResponseSelector().setRespondFromName(responseConnectorProperties.getResponseVariable());
        }

        if (storageSettings.isEnabled()) {
            BufferedDaoFactory bufferedDaoFactory = new BufferedDaoFactory(Donkey.getInstance().getDaoFactory());
            bufferedDaoFactory.setEncryptData(channelProperties.isEncryptData());

            channel.setDaoFactory(bufferedDaoFactory);
        } else {
            channel.setDaoFactory(new PassthruDaoFactory(new DelayedStatisticsUpdater(Donkey.getInstance().getDaoFactory())));
        }

        DestinationChain chain = createDestinationChain(channel);

        for (Connector connector : model.getDestinationConnectors()) {
            if (connector.isEnabled()) {
                // read 'waitForPrevious' property and add new chains as needed
                // if there are currently no chains, add a new one regardless of 'waitForPrevious'
                if (!connector.isWaitForPrevious() || channel.getDestinationChains().size() == 0) {
                    chain = createDestinationChain(channel);
                    channel.addDestinationChain(chain);
                }

                chain.addDestination(connector.getMetaDataId(), createFilterTransformerExecutor(channelId, connector), createDestinationConnector(channelId, connector, storageSettings));
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
                storageSettings.setStoreCustomMetaData(false);
                storageSettings.setStoreMaps(false);
                storageSettings.setStoreResponseMap(false);
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
        AttachmentHandler attachmentHandler = AttachmentHandlerFactory.getAttachmentHandler(attachmentHandlerProperties);

        if (attachmentHandler instanceof JavaScriptAttachmentHandler) {
            String scriptId = ScriptController.getScriptId(ScriptController.ATTACHMENT_SCRIPT_KEY, channelId);
            String attachmentScript = attachmentHandlerProperties.getProperties().get("javascript.script");

            if (attachmentScript != null) {
                try {
                    Set<String> scriptOptions = new HashSet<String>();
                    scriptOptions.add("useAttachmentList");
                    JavaScriptUtil.compileAndAddScript(scriptId, attachmentScript, scriptOptions);
                } catch (Exception e) {
                    logger.error("Error compiling attachment handler script " + scriptId + ".", e);
                }
            }
        }

        return attachmentHandler;
    }

    private PreProcessor createPreProcessor(String channelId, String preProcessingScript) {
        String scriptId = ScriptController.getScriptId(ScriptController.PREPROCESSOR_SCRIPT_KEY, channelId);

        try {
            JavaScriptUtil.compileAndAddScript(scriptId, preProcessingScript);
        } catch (Exception e) {
            logger.error("Error compiling preprocessor script " + scriptId + ".", e);
        }

        JavaScriptPreprocessor preProcessor = new JavaScriptPreprocessor();
        preProcessor.setChannelId(channelId);
        return preProcessor;
    }

    private PostProcessor createPostProcessor(String channelId, String postProcessingScript) {
        String scriptId = ScriptController.getScriptId(ScriptController.POSTPROCESSOR_SCRIPT_KEY, channelId);

        try {
            JavaScriptUtil.compileAndAddScript(scriptId, postProcessingScript);
        } catch (Exception e) {
            logger.error("Error compiling postprocessor script " + scriptId + ".", e);
        }

        return new JavaScriptPostprocessor();
    }

    private SourceConnector createSourceConnector(com.mirth.connect.donkey.server.channel.Channel donkeyChannel, Connector model, StorageSettings storageSettings) throws Exception {
        ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
        ConnectorProperties connectorProperties = model.getProperties();
        ConnectorMetaData connectorMetaData = extensionController.getConnectorMetaData().get(connectorProperties.getName());
        SourceConnector sourceConnector = (SourceConnector) Class.forName(connectorMetaData.getServerClassName()).newInstance();

        setCommonConnectorProperties(donkeyChannel.getChannelId(), sourceConnector, model);

        sourceConnector.setMetaDataReplacer(createMetaDataReplacer(model));
        sourceConnector.setChannel(donkeyChannel);

        if (connectorProperties instanceof ResponseConnectorPropertiesInterface) {
            ResponseConnectorProperties responseConnectorProperties = ((ResponseConnectorPropertiesInterface) connectorProperties).getResponseConnectorProperties();

            // queueing on the source connector is not possible if we are not storing raw content
            if (!storageSettings.isEnabled() || !storageSettings.isStoreRaw()) {
                responseConnectorProperties.setRespondAfterProcessing(true);
            }

            sourceConnector.setRespondAfterProcessing(responseConnectorProperties.isRespondAfterProcessing());
        } else {
            sourceConnector.setRespondAfterProcessing(true);
        }

        return sourceConnector;
    }

    private FilterTransformerExecutor createFilterTransformerExecutor(String channelId, Connector connector) throws Exception {
        boolean runFilterTransformer = false;
        String template = null;
        Transformer transformer = connector.getTransformer();
        Filter filter = connector.getFilter();

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
                } catch (SerializerException e) {
                    throw new SerializerException("Error serializing transformer outbound template for connector \"" + connector.getName() + "\": " + e.getMessage(), e.getCause(), e.getFormattedError());
                }
            }

            runFilterTransformer = true;
        }

        FilterTransformerExecutor filterTransformerExecutor = new FilterTransformerExecutor(inboundDataType, outboundDataType);

        if (runFilterTransformer) {
            String script = JavaScriptBuilder.generateFilterTransformerScript(filter, transformer);
            filterTransformerExecutor.setFilterTransformer(new JavaScriptFilterTransformer(channelId, connector.getName(), script, template));
        }

        return filterTransformerExecutor;
    }

    private ResponseTransformerExecutor createResponseTransformerExecutor(String channelId, Connector connector) throws Exception {
        boolean runResponseTransformer = false;
        String template = null;
        Transformer transformer = connector.getResponseTransformer();

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
                } catch (SerializerException e) {
                    throw new SerializerException("Error serializing response transformer outbound template for connector \"" + connector.getName() + "\": " + e.getMessage(), e.getCause(), e.getFormattedError());
                }
            }

            runResponseTransformer = true;
        }

        ResponseTransformerExecutor responseTransformerExecutor = new ResponseTransformerExecutor(inboundDataType, outboundDataType);

        if (runResponseTransformer) {
            String script = JavaScriptBuilder.generateResponseTransformerScript(transformer);
            responseTransformerExecutor.setResponseTransformer(new JavaScriptResponseTransformer(channelId, connector.getName(), script, template));
        }

        return responseTransformerExecutor;
    }

    private DestinationChain createDestinationChain(com.mirth.connect.donkey.server.channel.Channel donkeyChannel) {
        DestinationChain chain = new DestinationChain();
        chain.setChannelId(donkeyChannel.getChannelId());
        chain.setMetaDataReplacer(donkeyChannel.getSourceConnector().getMetaDataReplacer());
        chain.setMetaDataColumns(donkeyChannel.getMetaDataColumns());

        return chain;
    }

    private DestinationConnector createDestinationConnector(String channelId, Connector model, StorageSettings storageSettings) throws Exception {
        ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
        ConnectorProperties connectorProperties = model.getProperties();
        ConnectorMetaData connectorMetaData = extensionController.getConnectorMetaData().get(connectorProperties.getName());
        String className = connectorMetaData.getServerClassName();
        DestinationConnector destinationConnector = (DestinationConnector) Class.forName(className).newInstance();

        setCommonConnectorProperties(channelId, destinationConnector, model);

        destinationConnector.setDestinationName(model.getName());
        destinationConnector.setResponseTransformerExecutor(createResponseTransformerExecutor(channelId, model));

        if (connectorProperties instanceof QueueConnectorPropertiesInterface) {
            QueueConnectorProperties queueConnectorProperties = ((QueueConnectorPropertiesInterface) connectorProperties).getQueueConnectorProperties();

            // queueing on the destination connector will be disabled if we are not storing encoded, sent or map data
            if (queueConnectorProperties.isQueueEnabled() && (!storageSettings.isEnabled() || !storageSettings.isStoreSourceEncoded() || !storageSettings.isStoreSent() || !storageSettings.isStoreMaps())) {
                logger.debug("Disabling queue for destination '" + connectorProperties.getName() + "', channel '" + channelId + "' since one or more required storage options are currently disabled");
                queueConnectorProperties.setQueueEnabled(false);
            }
        }

        return destinationConnector;
    }

    private void setCommonConnectorProperties(String channelId, com.mirth.connect.donkey.server.channel.Connector connector, Connector model) {
        connector.setChannelId(channelId);
        connector.setMetaDataId(model.getMetaDataId());
        connector.setConnectorProperties(model.getProperties());

        Transformer transformerModel = model.getTransformer();

        connector.setInboundDataType(DataTypeFactory.getDataType(transformerModel.getInboundDataType(), transformerModel.getInboundProperties()));
        connector.setOutboundDataType(DataTypeFactory.getDataType(transformerModel.getOutboundDataType(), transformerModel.getOutboundProperties()));
    }

    private MetaDataReplacer createMetaDataReplacer(Connector connector) {
        // TODO: Extract this from the Connector model based on the inbound data type
        return new MirthMetaDataReplacer();
    }

    private void clearGlobalChannelMap(Channel channel) {
        if (channel.getProperties().isClearGlobalChannelMap()) {
            logger.debug("clearing global channel map for channel: " + channel.getId());
            GlobalChannelVariableStoreFactory.getInstance().get(channel.getId()).clear();
            GlobalChannelVariableStoreFactory.getInstance().get(channel.getId()).clearSync();
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
}
