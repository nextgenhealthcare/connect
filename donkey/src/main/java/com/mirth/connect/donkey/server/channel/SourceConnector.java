/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.DeployedStateEventType;
import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.event.DeployedStateEvent;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptorFactory;
import com.mirth.connect.donkey.server.message.batch.BatchMessageException;
import com.mirth.connect.donkey.server.message.batch.ResponseHandler;
import com.mirth.connect.donkey.server.message.batch.SimpleResponseHandler;

/**
 * The base class for all source connectors.
 */
public abstract class SourceConnector extends Connector {
    private Channel channel;
    private boolean respondAfterProcessing = true;
    private MetaDataReplacer metaDataReplacer;
    private BatchAdaptorFactory batchAdaptorFactory;
    private String sourceName = "Source";

    private Logger logger = Logger.getLogger(getClass());

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public boolean isRespondAfterProcessing() {
        return respondAfterProcessing;
    }

    public void setRespondAfterProcessing(boolean respondAfterProcessing) {
        this.respondAfterProcessing = respondAfterProcessing;
    }

    public MetaDataReplacer getMetaDataReplacer() {
        return metaDataReplacer;
    }

    public void setMetaDataReplacer(MetaDataReplacer metaDataReplacer) {
        this.metaDataReplacer = metaDataReplacer;
    }

    public BatchAdaptorFactory getBatchAdaptorFactory() {
        return batchAdaptorFactory;
    }

    public void setBatchAdaptorFactory(BatchAdaptorFactory batchAdaptorFactory) {
        this.batchAdaptorFactory = batchAdaptorFactory;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public void updateCurrentState(DeployedState currentState) {
        setCurrentState(currentState);
        channel.getEventDispatcher().dispatchEvent(new DeployedStateEvent(getChannelId(), channel.getName(), getMetaDataId(), sourceName, DeployedStateEventType.getTypeFromDeployedState(currentState)));
    }

    public boolean isProcessBatch() {
        return batchAdaptorFactory != null;
    }

    /**
     * Start the connector
     */
    @Override
    public void start() throws StartException {
        updateCurrentState(DeployedState.STARTING);

        if (isProcessBatch()) {
            batchAdaptorFactory.start();
        }
        onStart();

        updateCurrentState(DeployedState.STARTED);
    }

    /**
     * Stop the connector
     */
    @Override
    public void stop() throws StopException {
        //TODO make this happen before the poll connector's stop method
        updateCurrentState(DeployedState.STOPPING);

        try {
            onStop();
            if (isProcessBatch()) {
                batchAdaptorFactory.stop();
            }
            updateCurrentState(DeployedState.STOPPED);
        } catch (Throwable t) {
            Throwable cause = t;

            if (cause instanceof StopException) {
                cause = cause.getCause();
            }
            if (cause instanceof ExecutionException) {
                cause = cause.getCause();
            }

            // If the thread has been interrupted, we don't want to set the state here because halt() will do it
            if (!(cause instanceof InterruptedException)) {
                updateCurrentState(DeployedState.STOPPED);
            }

            if (t instanceof StopException) {
                throw (StopException) t;
            } else {
                throw new StopException(t);
            }
        }
    }

    /**
     * Stop the connector
     */
    @Override
    public void halt() throws HaltException {
        //TODO make this happen before the poll connector's stop method
        updateCurrentState(DeployedState.STOPPING);

        try {
            onHalt();
        } finally {
            channel.getEventDispatcher().dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));
            updateCurrentState(DeployedState.STOPPED);
        }
    }

    /**
     * Takes a raw message and processes it if the connector is set to wait for the destination
     * connectors to complete, otherwise it queues the message for processing.
     * 
     * @param rawMessage
     *            A raw message
     * @return The MessageResponse, containing the message id and a response if one was received
     * @throws ChannelException
     */
    public DispatchResult dispatchRawMessage(RawMessage rawMessage) throws ChannelException {
        return dispatchRawMessage(rawMessage, false);
    }

    /**
     * Takes a raw message and processes it if the connector is set to wait for the destination
     * connectors to complete, otherwise it queues the message for processing.
     * 
     * @param rawMessage
     *            A raw message
     * @param force
     *            If true, dispatch the message to the channel even if the source connector is
     *            stopped
     * @return The MessageResponse, containing the message id and a response if one was received
     * @throws ChannelException
     */
    public DispatchResult dispatchRawMessage(RawMessage rawMessage, boolean force) throws ChannelException {
        if (!force && getCurrentState() == DeployedState.STOPPED) {
            ChannelException e = new ChannelException(true);
            logger.error("Source connector is currently stopped. Channel Id: " + channel.getChannelId(), e);
            throw e;
        }

        return channel.dispatchRawMessage(rawMessage, false);
    }

    public void dispatchBatchMessage(BatchRawMessage batchRawMessage, ResponseHandler responseHandler) throws BatchMessageException {
        // Prevent new batches from starting if the connector is stopping
        if (getCurrentState() == DeployedState.STOPPING) {
            return;
        }

        // Throw an error if a new batch arrives when the connector is stopped
        if (getCurrentState() == DeployedState.STOPPED) {
            BatchMessageException e = new BatchMessageException();
            logger.error("Source connector is currently stopped. Channel Id: " + channel.getChannelId(), e);
            throw e;
        }

        // Use an empty response handler if one is not provided
        if (responseHandler == null) {
            responseHandler = new SimpleResponseHandler();
        }

        BatchAdaptor batchAdaptor = null;
        // Attempt to start the batch. It will not start if the batch adaptor factory is in the process of being stopped
        if (batchAdaptorFactory.startBatch()) {
            try {
                // Tell the response handler which response to store
                responseHandler.setUseFirstResponse(batchAdaptorFactory.isUseFirstReponse());

                // Create a new adaptor for this batch
                batchAdaptor = batchAdaptorFactory.createBatchAdaptor(batchRawMessage.getBatchMessageSource());

                Long batchSet = null;
                String message;
                // Get the next message for this batch
                while ((message = batchAdaptor.getMessage()) != null) {
                    // Create a copy of the source map for this message
                    Map<String, Object> sourceMap = new HashMap<String, Object>(batchRawMessage.getSourceMap());

                    // Add the batchId to identify the message's position in the batch
                    sourceMap.put(Constants.BATCH_SEQUENCE_ID_KEY, batchAdaptor.getBatchSequenceId());

                    // Add the message Id of the first message in the batch
                    if (batchSet != null) {
                        sourceMap.put(Constants.BATCH_ID_KEY, batchSet);
                    }

                    if (batchAdaptor.isLookAhead()) {
                        sourceMap.put(Constants.BATCH_COMPLETE_KEY, batchAdaptor.isBatchComplete());
                    }

                    // Create a new RawMessage to be dispatched
                    RawMessage rawMessage = new RawMessage(message, null, sourceMap);

                    DispatchResult dispatchResult = null;
                    try {
                        // Dispatch the message
                        dispatchResult = channel.dispatchRawMessage(rawMessage, true);
                        // Set the dispatch result for this message into the response handler
                        responseHandler.setDispatchResult(dispatchResult);

                        // If this was the first message in the batch, keep track of the message Id
                        if (batchAdaptor.getBatchSequenceId() == 1) {
                            batchSet = dispatchResult.getMessageId();
                        }

                        try {
                            // Allow the response handler to process the result
                            responseHandler.responseProcess(batchAdaptor.getBatchSequenceId(), batchAdaptor.isBatchComplete());
                        } catch (Exception e) {
                            // Stop the entire batch if an exceptions occurs processing a response
                            throw new BatchMessageException("Failed to process response for batch message at message " + batchAdaptor.getBatchSequenceId(), e);
                        }
                    } catch (ChannelException e) {
                        // Call back to the response handler if a channel exception occurred. The message should not have been persisted
                        responseHandler.responseError(e);
                        throw new BatchMessageException("Failed to process batch message at message " + batchAdaptor.getBatchSequenceId(), e);
                    } finally {
                        finishDispatch(dispatchResult);
                    }
                }
            } finally {
                try {
                    // Cleanup any resources used by the batch adaptor
                    batchAdaptor.cleanup();
                } finally {
                    // Finish the batch
                    batchAdaptorFactory.finishBatch();
                }
            }
        }
    }

    /**
     * Handles a response generated for a message that was recovered by the channel
     * 
     * @throws ChannelException
     */
    public abstract void handleRecoveredResponse(DispatchResult dispatchResult);

    /**
     * Finish a message dispatch
     * 
     * @param dispatchResult
     *            The DispatchResult returned by dispatchRawMessage()
     * @param attemptedResponse
     *            True if an attempt to send a response was made, false if not
     * @param responseError
     *            An error message if an error occurred when attempting to send a response
     */
    public void finishDispatch(DispatchResult dispatchResult) {
        if (dispatchResult == null) {
            return;
        }

        try {
            boolean attemptedResponse = dispatchResult.isAttemptedResponse();
            String responseError = dispatchResult.getResponseError();
            Response selectedResponse = dispatchResult.getSelectedResponse();

            DonkeyDaoFactory daoFactory = channel.getDaoFactory();
            StorageSettings storageSettings = channel.getStorageSettings();
            DonkeyDao dao = null;
            long messageId = dispatchResult.getMessageId();

            try {
                if (selectedResponse != null && storageSettings.isStoreSentResponse()) {
                    dao = daoFactory.getDao();
                    String responseContent = channel.getSerializer().serialize(selectedResponse);

                    // The source response content cannot know the data type of the response it is using.
                    dao.insertMessageContent(new MessageContent(getChannelId(), messageId, 0, ContentType.RESPONSE, responseContent, null, false));
                }

                Message processedMessage = dispatchResult.getProcessedMessage();

                if (storageSettings.isStoreMergedResponseMap() && processedMessage != null) {
                    if (dao == null) {
                        dao = daoFactory.getDao();
                    }

                    // Store the merged response map
                    dao.updateResponseMap(processedMessage.getConnectorMessages().get(0));
                }

                if (attemptedResponse || responseError != null) {
                    if (dao == null) {
                        dao = daoFactory.getDao();
                    }

                    ConnectorMessage connectorMessage = null;
                    if (processedMessage != null) {
                        connectorMessage = processedMessage.getConnectorMessages().get(0);
                    } else {
                        Set<Integer> metaDataIds = new HashSet<Integer>();
                        metaDataIds.add(0);

                        /*
                         * If there is no existing connector message, then the channel was either
                         * halted or a runtime exception occurred. In either case, we may still want
                         * to update the response date and error. Therefore, we must retrieve the
                         * existing connector message from the database.
                         */
                        connectorMessage = dao.getConnectorMessages(getChannelId(), messageId, metaDataIds, true).get(0);
                    }

                    // There are no retry attempts for sending the response back, so send attempts is either 0 or 1.
                    if (attemptedResponse) {
                        connectorMessage.setSendAttempts(1);
                        connectorMessage.setResponseDate(dispatchResult.getResponseDate());
                        dao.updateSendAttempts(connectorMessage);
                    }

                    if (responseError != null) {
                        connectorMessage.setResponseError(responseError);
                        dao.updateErrors(connectorMessage);
                    }
                }

                if (dispatchResult.isMarkAsProcessed()) {
                    if (dao == null) {
                        dao = daoFactory.getDao();
                    }

                    dao.markAsProcessed(getChannelId(), messageId);

                    // If destination queuing is disabled, it's safe to remove content in the same transaction
                    if (!channel.isUsingDestinationQueues()) {
                        channel.removeContent(dao, processedMessage, messageId, false, false);
                    }
                }

                if (dao != null) {
                    dao.commit(storageSettings.isDurable());
                }

                // If destination queuing is enabled, we have to remove content in a separate transaction
                if (dispatchResult.isMarkAsProcessed() && channel.isUsingDestinationQueues()) {
                    channel.removeContent(dao, processedMessage, messageId, false, true);
                }
            } finally {
                if (dao != null) {
                    dao.close();
                }
            }
        } finally {
            if (dispatchResult != null && dispatchResult.isLockAcquired()) {
                channel.releaseProcessLock();
                dispatchResult.setLockAcquired(false);
            }
        }
    }
}
