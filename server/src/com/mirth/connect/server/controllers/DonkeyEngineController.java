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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ChannelState;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.QueueConnectorProperties;
import com.mirth.connect.donkey.model.channel.QueueConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.message.DataType;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
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
import com.mirth.connect.donkey.server.channel.MessageResponse;
import com.mirth.connect.donkey.server.channel.MetaDataReplacer;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.donkey.server.channel.StorageSettings;
import com.mirth.connect.donkey.server.channel.components.FilterTransformerExecutor;
import com.mirth.connect.donkey.server.channel.components.PostProcessor;
import com.mirth.connect.donkey.server.channel.components.PreProcessor;
import com.mirth.connect.donkey.server.channel.components.ResponseTransformer;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelProperties;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.DashboardStatus.StatusType;
import com.mirth.connect.model.MessageStorageMode;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.model.converters.DataTypeFactory;
import com.mirth.connect.model.converters.MirthMetaDataReplacer;
import com.mirth.connect.model.converters.SerializerFactory;
import com.mirth.connect.model.handlers.AttachmentHandlerFactory;
import com.mirth.connect.model.handlers.JavaScriptAttachmentHandler;
import com.mirth.connect.plugins.ChannelPlugin;
import com.mirth.connect.server.builders.JavaScriptBuilder;
import com.mirth.connect.server.transformers.JavaScriptFilterTransformer;
import com.mirth.connect.server.transformers.JavaScriptPostprocessor;
import com.mirth.connect.server.transformers.JavaScriptPreprocessor;
import com.mirth.connect.server.transformers.JavaScriptResponseTransformer;
import com.mirth.connect.server.util.GlobalChannelVariableStoreFactory;
import com.mirth.connect.server.util.GlobalVariableStore;
import com.mirth.connect.server.util.JavaScriptUtil;
import com.mirth.connect.server.util.UUIDGenerator;

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
    private TemplateController templateController = ControllerFactory.getFactory().createTemplateController();
    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private com.mirth.connect.donkey.server.controllers.ChannelController donkeyChannelController = com.mirth.connect.donkey.server.controllers.ChannelController.getInstance();
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();

    private DonkeyEngineController() {}

    @Override
    public void startEngine() throws StartException, StopException, ControllerException, InterruptedException {
        logger.debug("starting donkey engine");

        // remove all scripts and templates since the channels were never undeployed
        scriptController.removeAllExceptGlobalScripts();
        templateController.removeAllTemplates();

        donkey.startEngine(new DonkeyConfiguration(configurationController.getApplicationDataDir(), configurationController.getDatabaseSettings().getProperties()));

        redeployAllChannels();
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

        if (donkeyChannel.isEnabled()) {
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
        channel.getSourceFilterTransformer().getFilterTransformer().dispose();

        for (DestinationChain chain : channel.getDestinationChains()) {
            for (Integer metaDataId : chain.getDestinationConnectors().keySet()) {
                chain.getFilterTransformerExecutors().get(metaDataId).getFilterTransformer().dispose();
                if (chain.getDestinationConnectors().get(metaDataId).getResponseTransformer() != null) {
                    chain.getDestinationConnectors().get(metaDataId).getResponseTransformer().dispose();
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
            Statistics overallStats = donkeyChannelController.getTotalStatistics();

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
            status.setOverallStatistics(overallStats.getConnectorStats(donkeyChannel.getChannelId(), null));
            status.setTags(channel.getTags());

            DashboardStatus sourceStatus = new DashboardStatus();
            sourceStatus.setStatusType(StatusType.SOURCE_CONNECTOR);
            sourceStatus.setChannelId(donkeyChannel.getChannelId());
            sourceStatus.setMetaDataId(0);
            sourceStatus.setName("Source");
            sourceStatus.setState(donkeyChannel.getSourceConnector().getCurrentState());
            sourceStatus.setStatistics(stats.getConnectorStats(donkeyChannel.getChannelId(), 0));
            sourceStatus.setOverallStatistics(overallStats.getConnectorStats(donkeyChannel.getChannelId(), 0));
            sourceStatus.setTags(channel.getTags());

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
                    destinationStatus.setOverallStatistics(overallStats.getConnectorStats(donkeyChannel.getChannelId(), metaDataId));
                    destinationStatus.setTags(channel.getTags());

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
    public Response handleRawMessage(String channelId, RawMessage rawMessage) throws ChannelException {
        if (channelId.equals("sink")) {
            return null;
        } else if (!isDeployed(channelId)) {
            logger.error("Could not find channel to route to for channel id: " + channelId);
            throw new ChannelException(false, true);
        }

        SourceConnector sourceConnector = donkey.getDeployedChannels().get(channelId).getSourceConnector();
        MessageResponse messageResponse = null;
        
        try {
            messageResponse = sourceConnector.handleRawMessage(rawMessage);
        } finally {
            sourceConnector.storeMessageResponse(messageResponse);
        }
        
        return messageResponse.getResponse();
    }

    private com.mirth.connect.donkey.server.channel.Channel convertToDonkeyChannel(Channel model) throws Exception {
        String channelId = model.getId();
        ChannelProperties channelProperties = model.getProperties();
        StorageSettings sourceStorageSettings = getStorageSettings(channelProperties.getMessageStorageMode(), false);
        StorageSettings destinationStorageSettings = getStorageSettings(channelProperties.getMessageStorageMode(), true);

        com.mirth.connect.donkey.server.channel.Channel channel = new com.mirth.connect.donkey.server.channel.Channel();

        channel.setChannelId(channelId);
        channel.setServerId(ConfigurationController.getInstance().getServerId());
        channel.setName(model.getName());
        channel.setEnabled(model.isEnabled());
        channel.setRevision(model.getRevision());
        channel.setVersion(model.getVersion());
        channel.setInitialState(channelProperties.isInitialStateStarted() ? ChannelState.STARTED : ChannelState.STOPPED);
        channel.setMetaDataColumns(channelProperties.getMetaDataColumns());
        channel.setRemoveContentOnCompletion(channelProperties.isRemoveContentOnCompletion());
        channel.setAttachmentHandler(createAttachmentHandler(channelId, channelProperties.getAttachmentProperties()));
        channel.setPreProcessor(createPreProcessor(channelId, model.getPreprocessingScript()));
        channel.setPostProcessor(createPostProcessor(channelId, model.getPostprocessingScript()));
        channel.setSourceConnector(createSourceConnector(channel, model.getSourceConnector(), sourceStorageSettings));
        channel.setSourceFilterTransformer(createFilterTransformerExecutor(channelId, model.getSourceConnector()));

        DestinationChain chain = createDestinationChain(channel);

        for (Connector connector : model.getDestinationConnectors()) {
            if (connector.isEnabled()) {
                // read 'waitForPrevious' property and add new chains as needed
                // if there are currently no chains, add a new one regardless of 'waitForPrevious'
                if (!connector.isWaitForPrevious() || channel.getDestinationChains().size() == 0) {
                    chain = createDestinationChain(channel);
                    channel.getDestinationChains().add(chain);
                }

                chain.addDestination(connector.getMetaDataId(), createFilterTransformerExecutor(channelId, connector), createDestinationConnector(channelId, connector, destinationStorageSettings));
            }
        }

        return channel;
    }
    
    public static StorageSettings getStorageSettings(MessageStorageMode messageStorageMode, boolean isDestination) {
        StorageSettings storageSettings = new StorageSettings();

        // we assume that all storage settings are enabled by default
        switch (messageStorageMode) {
            case PRODUCTION:
                storageSettings.setStoreProcessedRaw(false);
                storageSettings.setStoreTransformed(false);
                storageSettings.setStoreProcessedResponse(false);
                break;

            case RAW:
                storageSettings.setMessageRecoveryEnabled(false);
                storageSettings.setDurable(false);
                storageSettings.setStoreCustomMetaData(false);
                storageSettings.setStoreMaps(false);
                storageSettings.setStoreProcessedRaw(false);
                storageSettings.setStoreTransformed(false);
                storageSettings.setStoreEncoded(false);
                storageSettings.setStoreSent(false);
                storageSettings.setStoreProcessedResponse(false);
                storageSettings.setStoreResponse(false);
                storageSettings.setStoreSentResponse(false);

                if (isDestination) {
                    storageSettings.setStoreRaw(false);
                }
                break;

            case METADATA:
                storageSettings.setMessageRecoveryEnabled(false);
                storageSettings.setDurable(false);
                storageSettings.setRawDurable(false);
                storageSettings.setStoreAttachments(false);
                storageSettings.setStoreCustomMetaData(false);
                storageSettings.setStoreMaps(false);
                storageSettings.setStoreRaw(false);
                storageSettings.setStoreProcessedRaw(false);
                storageSettings.setStoreTransformed(false);
                storageSettings.setStoreEncoded(false);
                storageSettings.setStoreSent(false);
                storageSettings.setStoreProcessedResponse(false);
                storageSettings.setStoreResponse(false);
                storageSettings.setStoreSentResponse(false);
                break;

            case DISABLED:
                storageSettings.setEnabled(false);
                storageSettings.setMessageRecoveryEnabled(false);
                storageSettings.setDurable(false);
                storageSettings.setRawDurable(false);
                storageSettings.setStoreAttachments(false);
                storageSettings.setStoreCustomMetaData(false);
                storageSettings.setStoreMaps(false);
                storageSettings.setStoreRaw(false);
                storageSettings.setStoreProcessedRaw(false);
                storageSettings.setStoreTransformed(false);
                storageSettings.setStoreEncoded(false);
                storageSettings.setStoreSent(false);
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
        if (preProcessingScript == null) {
            return null;
        }
        
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
        if (postProcessingScript == null) {
            return null;
        }
        
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
        sourceConnector.setStorageSettings(storageSettings);
        
        if (connectorProperties instanceof QueueConnectorPropertiesInterface) {
            QueueConnectorProperties queueConnectorProperties = ((QueueConnectorPropertiesInterface) connectorProperties).getQueueConnectorProperties();
            
            // queueing on the source connector is not possible if we are not storing raw content
            if (!storageSettings.isEnabled() || !storageSettings.isStoreRaw()) {
                queueConnectorProperties.setQueueEnabled(false);
            }
            
            sourceConnector.setWaitForDestinations(!queueConnectorProperties.isQueueEnabled());
        } else {
            sourceConnector.setWaitForDestinations(true);
        }

        return sourceConnector;
    }

    private FilterTransformerExecutor createFilterTransformerExecutor(String channelId, Connector connector) throws Exception {
        String templateId = null;

        // put the outbound template in the templates table
        if (connector.getTransformer().getOutboundTemplate() != null) {
            TemplateController templateController = ControllerFactory.getFactory().createTemplateController();
            XmlSerializer serializer = SerializerFactory.getSerializer(connector.getTransformer().getOutboundDataType(), connector.getTransformer().getOutboundProperties());
            templateId = UUIDGenerator.getUUID();

            if (StringUtils.isNotBlank(connector.getTransformer().getOutboundTemplate())) {
                if (connector.getTransformer().getOutboundDataType().equals(DataTypeFactory.DICOM)) {
                    templateController.putTemplate(channelId, templateId, connector.getTransformer().getOutboundTemplate());
                } else {
                    templateController.putTemplate(channelId, templateId, serializer.toXML(connector.getTransformer().getOutboundTemplate()));
                }
            }
        }

        // put the script in the scripts table
        String scriptId = UUIDGenerator.getUUID();
        String script = JavaScriptBuilder.generateFilterTransformerScript(connector.getFilter(), connector.getTransformer());
        scriptController.putScript(channelId, scriptId, script);

        DataType inboundDataType = DataTypeFactory.getDataType(connector.getTransformer().getInboundDataType(), connector.getTransformer().getInboundProperties());
        DataType outboundDataType = DataTypeFactory.getDataType(connector.getTransformer().getOutboundDataType(), connector.getTransformer().getOutboundProperties());
        
        FilterTransformerExecutor filterTransformerExecutor = new FilterTransformerExecutor(inboundDataType, outboundDataType);
        filterTransformerExecutor.setFilterTransformer(new JavaScriptFilterTransformer(channelId, connector.getName(), scriptId, templateId));
        
        return filterTransformerExecutor;
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
        destinationConnector.setResponseTransformer(createResponseTransformer(model, channelId));
        destinationConnector.setStorageSettings(storageSettings);

        if (connectorProperties instanceof QueueConnectorPropertiesInterface) {
            QueueConnectorProperties queueConnectorProperties = ((QueueConnectorPropertiesInterface) connectorProperties).getQueueConnectorProperties();
            
            // queueing on the destination connector will be disabled if we are not storing encoded, sent or map data
            if (queueConnectorProperties.isQueueEnabled() && (!storageSettings.isEnabled() || !storageSettings.isStoreEncoded() || !storageSettings.isStoreSent() || !storageSettings.isStoreMaps())) {
                logger.debug("Disabling queue for destination '" + connectorProperties.getName() + "', channel '" + channelId + "' since one or more required storage options are currently disabled");
                queueConnectorProperties.setQueueEnabled(false);
            }
        }
        
        return destinationConnector;
    }
    
    private ResponseTransformer createResponseTransformer(Connector connector, String channelId) throws Exception {
        ResponseTransformer responseTransformer = null;

        if (connector.getResponseTransformer() != null) {
            // Put the script in the scripts table
            String scriptId = UUIDGenerator.getUUID();
            String script = JavaScriptBuilder.generateResponseTransformerScript(connector.getResponseTransformer());
            scriptController.putScript(channelId, scriptId, script);

            // Initialize the response transformer
            responseTransformer = new JavaScriptResponseTransformer(channelId, connector.getName(), scriptId);
        }

        return responseTransformer;
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
