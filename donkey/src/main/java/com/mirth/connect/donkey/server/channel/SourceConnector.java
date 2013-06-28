/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ChannelState;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.util.Serializer;

/**
 * The base class for all source connectors.
 */
public abstract class SourceConnector extends Connector {
    private Channel channel;
    private boolean respondAfterProcessing = true;
    private MetaDataReplacer metaDataReplacer;
    private String sourceName = "Source";

    private Logger logger = Logger.getLogger(getClass());

    public void setChannel(Channel channel) {
        this.channel = channel;
        setChannelId(channel.getChannelId());
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

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    /**
     * Start the connector
     */
    @Override
    public void start() throws StartException {
        setCurrentState(ChannelState.STARTING);

        onStart();

        setCurrentState(ChannelState.STARTED);
    }

    /**
     * Stop the connector
     */
    @Override
    public void stop() throws StopException {
        //TODO make this happen before the poll connector's stop method
        setCurrentState(ChannelState.STOPPING);

        try {
            onStop();
            setCurrentState(ChannelState.STOPPED);
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
                setCurrentState(ChannelState.STOPPED);
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
        setCurrentState(ChannelState.STOPPING);

        try {
            onHalt();
        } finally {
            setCurrentState(ChannelState.STOPPED);
        }
    }

    /**
     * Takes a raw message and processes it if the connector is set to wait for
     * the destination
     * connectors to complete, otherwise it queues the message for processing
     * 
     * @param rawMessage
     *            A raw message
     * @return The MessageResponse, containing the message id and a response if
     *         one was received
     * @throws StoppedException
     * @throws ChannelErrorException
     * @throws StoppingException
     */
    public DispatchResult dispatchRawMessage(RawMessage rawMessage) throws ChannelException {
        if (getCurrentState() == ChannelState.STOPPED) {
            ChannelException e = new ChannelException(true);
            logger.error("Source connector is currently stopped. Channel Id: " + channel.getChannelId(), e);
            throw e;
        }

        return channel.dispatchRawMessage(rawMessage);
    }

    /**
     * Handles a response generated for a message that was recovered by the
     * channel
     * 
     * @throws ChannelException
     */
    public abstract void handleRecoveredResponse(DispatchResult dispatchResult);

    /**
     * Finish a message dispatch
     * 
     * @param dispatchResult
     *            The DispatchResult returned by dispatchRawMessage()
     */
    public void finishDispatch(DispatchResult dispatchResult) {
        finishDispatch(dispatchResult, false, null);
    }

    /**
     * Finish a message dispatch
     * 
     * @param dispatchResult
     *            The DispatchResult returned by dispatchRawMessage()
     * @param attemptedResponse
     *            True if an attempt to send a response was made, false if not
     * @param responseError
     *            An error message if an error occurred when attempting to send
     *            a response
     */
    public void finishDispatch(DispatchResult dispatchResult, boolean attemptedResponse, String responseError) {
        if (dispatchResult == null) {
            return;
        }

        try {
            Response selectedResponse = dispatchResult.getSelectedResponse();

            DonkeyDaoFactory daoFactory = channel.getDaoFactory();
            StorageSettings storageSettings = channel.getStorageSettings();
            DonkeyDao dao = null;
            long messageId = dispatchResult.getMessageId();

            try {
                if (selectedResponse != null && storageSettings.isStoreSentResponse()) {
                    dao = daoFactory.getDao();
                    Serializer serializer = Donkey.getInstance().getSerializer();
                    String responseContent = serializer.serialize(selectedResponse);

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
                        dao.updateSourceResponse(connectorMessage);
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

                    if (dispatchResult.isRemoveContent()) {
                        dao.deleteMessageContent(getChannelId(), messageId);
                    }

                    if (dispatchResult.isRemoveAttachments()) {
                        dao.deleteMessageAttachments(getChannelId(), messageId);
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
        } finally {
            if (dispatchResult != null && dispatchResult.isLockAcquired()) {
                channel.releaseProcessLock();
                dispatchResult.setLockAcquired(false);
            }
        }
    }
}
