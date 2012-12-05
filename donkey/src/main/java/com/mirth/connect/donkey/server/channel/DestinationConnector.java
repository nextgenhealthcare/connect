/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.channel.ChannelState;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.QueueConnectorProperties;
import com.mirth.connect.donkey.model.channel.QueueConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.Encryptor;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.channel.components.ResponseTransformer;
import com.mirth.connect.donkey.server.controllers.MessageController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.queue.ConnectorMessageQueue;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.donkey.util.ThreadUtils;

public abstract class DestinationConnector extends Connector implements ConnectorInterface, Runnable {
    private Thread thread;
    private QueueConnectorProperties queueProperties;
    private ConnectorMessageQueue queue = new ConnectorMessageQueue();
    private String destinationName;
    private boolean enabled;
    private ResponseTransformer responseTransformer;
    private StorageSettings storageSettings = new StorageSettings();
    private DonkeyDaoFactory daoFactory;
    private Encryptor encryptor;
    private ChannelState currentState = ChannelState.STOPPED;
    private Logger logger = Logger.getLogger(getClass());

    public abstract ConnectorProperties getReplacedConnectorProperties(ConnectorMessage message);

    public abstract Response send(ConnectorProperties connectorProperties, ConnectorMessage message) throws InterruptedException;

    public ConnectorMessageQueue getQueue() {
        return queue;
    }

    public void setQueue(ConnectorMessageQueue connectorMessages) {
        this.queue = connectorMessages;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Deprecated
    public boolean isRunning() {
        return currentState != ChannelState.STOPPED && currentState != ChannelState.STOPPING;
    }

    @Override
    public void setConnectorProperties(ConnectorProperties connectorProperties) {
        super.setConnectorProperties(connectorProperties);

        if (connectorProperties instanceof QueueConnectorPropertiesInterface) {
            this.queueProperties = ((QueueConnectorPropertiesInterface) connectorProperties).getQueueConnectorProperties();
        }
    }

    public ResponseTransformer getResponseTransformer() {
        return responseTransformer;
    }

    public void setResponseTransformer(ResponseTransformer responseTransformer) {
        this.responseTransformer = responseTransformer;
    }

    protected void setStorageSettings(StorageSettings storageSettings) {
        this.storageSettings = storageSettings;
    }
    
    protected void setDaoFactory(DonkeyDaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    protected void setEncryptor(Encryptor encryptor) {
        this.encryptor = encryptor;
    }

    public ChannelState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(ChannelState currentState) {
        this.currentState = currentState;
    }

    /**
     * Tells whether or not queueing is enabled
     */
    public boolean isQueueEnabled() {
        return (queueProperties != null && queueProperties.isQueueEnabled());
    }
    
    /**
     * Tells whether or not queue rotation is enabled
     */
    public boolean isQueueRotate() {
    	return (queueProperties != null && queueProperties.isRotate());
    }

    @Override
    public void start() throws StartException {
        setCurrentState(ChannelState.STARTING);

        if (isQueueEnabled()) {
        	// Remove any items in the queue's buffer because they may be outdated.
            queue.invalidate();
            // refresh the queue size from it's data source
            queue.updateSize();

            thread = new Thread(this);
            thread.start();
        }

        onStart();

        setCurrentState(ChannelState.STARTED);
    }

    @Override
    public void stop() throws StopException {
        setCurrentState(ChannelState.STOPPING);

        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new StopException("Failed to stop destination connector for channel: " + getChannelId(), e);
            }
        }

        onStop();

        setCurrentState(ChannelState.STOPPED);
    }

    @Override
    public void halt() throws StopException {
        setCurrentState(ChannelState.STOPPING);

        if (thread != null) {
            try {
                thread.interrupt();
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new StopException("Failed to stop destination connector for channel: " + getChannelId(), e);
            }
        }

        onStop();

        setCurrentState(ChannelState.STOPPED);
    }

    private MessageContent getSentContent(ConnectorMessage message, ConnectorProperties connectorProperties) {
        // TODO: store the serializer as a class variable?
        String content = Donkey.getInstance().getSerializer().serialize(connectorProperties);
        return new MessageContent(message.getChannelId(), message.getMessageId(), message.getMetaDataId(), ContentType.SENT, content, encryptor.encrypt(content));
    }

    /**
     * Process a transformed message. Attempt to send the message unless the
     * destination connector is configured to immediately queue messages.
     * 
     * @return The status of the message at the end of processing. If the
     *         message was placed in the destination connector queue, then
     *         QUEUED is returned.
     * @throws InterruptedException
     */
    public void process(DonkeyDao dao, ConnectorMessage message, Status previousStatus) throws InterruptedException {
        ConnectorProperties connectorProperties = null;
        boolean attemptSend = (!isQueueEnabled() || queueProperties.isSendFirst());

        // we need to get the connector envelope if we will be attempting to send the message     
        if (attemptSend) {
            ThreadUtils.checkInterruptedStatus();

            // have the connector generate the connector envelope and store it in the message
            connectorProperties = getReplacedConnectorProperties(message);

            if (storageSettings.isStoreSent()) {
                ThreadUtils.checkInterruptedStatus();

                MessageContent sentContent = getSentContent(message, connectorProperties);
                message.setSent(sentContent);

                if (sentContent != null) {
                    ThreadUtils.checkInterruptedStatus();
                    dao.insertMessageContent(sentContent);
                }
            }

            int retryCount = (queueProperties == null) ? 0 : queueProperties.getRetryCount();
            int sendAttempts = 0;
            Response response = null;
            Status responseStatus = null;

            do {
                // pause for the given retry interval if this is not the first send attempt
                if (sendAttempts > 0) {
                    Thread.sleep(queueProperties.getRetryIntervalMillis());
                } else {
                    ThreadUtils.checkInterruptedStatus();
                }

                // have the connector send the message and return a response
                response = handleSend(connectorProperties, message);
                // NOTE: Send attempts here will not be persisted until all attempts have completed since there is only one transaction.
                // Each attempt from the queue will be persisted though.
                message.setSendAttempts(++sendAttempts);
                fixResponseStatus(response);
                responseStatus = response.getNewMessageStatus();
            } while ((responseStatus == Status.ERROR || responseStatus == Status.QUEUED) && (sendAttempts - 1) < retryCount);

            afterSend(dao, message, response, previousStatus);
        } else {
            dao.updateStatus(message, previousStatus);
        }
    }

    /**
     * Process a connector message with PENDING status
     * 
     * @throws InterruptedException
     */
    public void processPendingConnectorMessage(DonkeyDao dao, ConnectorMessage message) throws InterruptedException {
        Response response = Response.fromString(message.getResponse().getContent());
        runResponseTransformer(dao, message, response);
        afterResponse(dao, message, response, message.getStatus());
    }

    @Override
    public void run() {
        DonkeyDao dao = null;
        try {
            Serializer serializer = Donkey.getInstance().getSerializer();
            ConnectorMessage connectorMessage = null;
            int retryIntervalMillis = queueProperties.getRetryIntervalMillis();
            boolean pauseBeforeNextMessage = false;

            do {
                connectorMessage = queue.peek();

                if (connectorMessage != null) {
                    try {
                        dao = daoFactory.getDao();
                        Status previousStatus = connectorMessage.getStatus();

                        ConnectorProperties connectorProperties = null;

                        // Generate the template if we are regenerating, or if this is the first send attempt (in which case the sent content should be null).
                        if (queueProperties.isRegenerateTemplate() || connectorMessage.getSent() == null) {
                            ThreadUtils.checkInterruptedStatus();
                            connectorProperties = getReplacedConnectorProperties(connectorMessage);
                            MessageContent sentContent = getSentContent(connectorMessage, connectorProperties);
                            connectorMessage.setSent(sentContent);

                            if (sentContent != null && storageSettings.isStoreSent()) {
                                ThreadUtils.checkInterruptedStatus();
                                dao.storeMessageContent(sentContent);
                            }
                        } else {
                            connectorProperties = (ConnectorProperties) serializer.deserialize(connectorMessage.getSent().getContent());
                        }
                        
                        ThreadUtils.checkInterruptedStatus();
                        Response response = handleSend(connectorProperties, connectorMessage);
                        connectorMessage.setSendAttempts(connectorMessage.getSendAttempts() + 1);
                        fixResponseStatus(response);

                        if (response == null) {
                            throw new RuntimeException("Received null response from destination " + destinationName + ".");
                        }

                        afterSend(dao, connectorMessage, response, previousStatus);

                        /*
                         * if the "remove content on completion" setting is enabled, we will need to
                         * retrieve a list of the other connector messages for this message id and
                         * check if the message is "completed"
                         */
                        if (storageSettings.isRemoveContentOnCompletion()) {
                            Map<Integer, ConnectorMessage> connectorMessages = dao.getConnectorMessages(getChannelId(), connectorMessage.getMessageId());

                            // update the map with the message that was just sent
                            connectorMessages.put(getMetaDataId(), connectorMessage);

                            if (MessageController.getInstance().isMessageCompleted(connectorMessages)) {
                                dao.deleteAllContent(getChannelId(), connectorMessage.getMessageId());
                            }
                        }

                        ThreadUtils.checkInterruptedStatus();
                        dao.commit(storageSettings.isDurable());

                        if (connectorMessage.getStatus() != Status.QUEUED) {
                            ThreadUtils.checkInterruptedStatus();

                            // We only peeked before, so this time actually remove the head of the queue.
                            // If the queue was invalidated, a different message could have been inserted to the front of the queue.
                            // Therefore, only poll the queue if the head is the same as the current message
                            synchronized (queue) {
                            	ConnectorMessage firstMessage = queue.peek();
                            	if (connectorMessage.getMessageId() == firstMessage.getMessageId() && connectorMessage.getMetaDataId() == firstMessage.getMetaDataId()) {
                            		queue.poll();
                            	}
                            }
                        } else {
                        	if (queueProperties.isRotate()) {
                        		// If the message is still queued and rotation is enabled, notify the queue that the message is to be rotated.
	                        	synchronized (queue) {
	                        		ConnectorMessage firstMessage = queue.peek();
	                        		if (connectorMessage.getMessageId() == firstMessage.getMessageId() && connectorMessage.getMetaDataId() == firstMessage.getMetaDataId()) {
	                        			queue.rotate(connectorMessage);
	                        		}
	                        	}
	                        }
                        	
                        	// If the same message is still queued, allow some time before attempting another message.
                        	pauseBeforeNextMessage = true;
                        }
                    } catch (RuntimeException e) {
                        logger.error("Error processing queued " + (connectorMessage != null ? connectorMessage.toString() : "message (null)") + " for channel " + getChannelId() + " (" + destinationName + "). This error is expected if the message was manually removed from the queue.", e);
                        // Invalidate the queue's buffer if any errors occurred. If the message being processed by the queue was deleted,
                        // This will prevent the queue from trying to process that message repeatedly.
                        queue.invalidate();
                    } finally {
                        if (dao != null) {
                            dao.close();
                        }
                    }
                } else {
                    pauseBeforeNextMessage = true;
                }
                
                // Pause at the end of the loop instead of during so we don't keep the connections open longer than they need to be.
                if (pauseBeforeNextMessage) {
                	Thread.sleep(retryIntervalMillis);
                	pauseBeforeNextMessage = false;
                }
            } while (currentState == ChannelState.STARTED || currentState == ChannelState.STARTING);
        } catch (InterruptedException e) {
        } catch (Exception e) {
            logger.error(e);
        } finally {
        	// Invalidate the queue's buffer when the queue is stopped to prevent the buffer becoming 
        	// unsynchronized with the data store.
            queue.invalidate();
            currentState = ChannelState.STOPPED;

            if (dao != null) {
                dao.close();
            }
        }
    }

    private Response handleSend(ConnectorProperties connectorProperties, ConnectorMessage message) throws InterruptedException {
        return send(connectorProperties, message);
    }

    private void afterSend(DonkeyDao dao, ConnectorMessage message, Response response, Status previousStatus) throws InterruptedException {
    	// Insert errors if necessary
        if (StringUtils.isNotBlank(response.getError())) {
            message.setErrors(response.getError());
            dao.updateErrors(message);
        }
    	
        String responseString = response.toString();
        MessageContent responseContent = new MessageContent(message.getChannelId(), message.getMessageId(), message.getMetaDataId(), ContentType.RESPONSE, responseString, encryptor.encrypt(responseString));

        if (storageSettings.isStoreResponse()) {
            ThreadUtils.checkInterruptedStatus();

            if (message.getResponse() != null) {
                dao.storeMessageContent(responseContent);
            } else {
                dao.insertMessageContent(responseContent);
            }
        }

        message.setResponse(responseContent);

        if (responseTransformer != null) {
            ThreadUtils.checkInterruptedStatus();
            message.setStatus(Status.PENDING);

            dao.updateStatus(message, previousStatus);
            dao.commit(storageSettings.isDurable());

            previousStatus = message.getStatus();

            runResponseTransformer(dao, message, response);
        } else {
            fixResponseStatus(response);
        }

        message.getResponseMap().put(destinationName, response);

        if (storageSettings.isStoreResponseMap()) {
            dao.updateResponseMap(message);
        }

        ThreadUtils.checkInterruptedStatus();
        afterResponse(dao, message, response, previousStatus);
    }

    private void runResponseTransformer(DonkeyDao dao, ConnectorMessage message, Response response) throws InterruptedException {
        ThreadUtils.checkInterruptedStatus();

        try {
            responseTransformer.doTransform(response);
        } catch (DonkeyException e) {
            logger.error("Error executing response transformer for channel " + message.getChannelId() + " (" + destinationName + ").", e);
            response.setNewMessageStatus(Status.ERROR);
            response.setError(e.getFormattedError());
            message.setErrors(message.getErrors() != null ? message.getErrors() + System.getProperty("line.separator") + System.getProperty("line.separator") + e.getFormattedError() : e.getFormattedError());
            dao.updateErrors(message);
            return;
        }

        fixResponseStatus(response);

        /*
         * TRANSACTION: Process Response
         * - (if there is a response transformer) store the processed response
         * and the response map
         * - update message status based on the response status
         * - if there is a next destination in the chain, create it's message
         */

        // store the processed response in the message
        String responseString = response.toString();
        MessageContent processedResponse = new MessageContent(getChannelId(), message.getMessageId(), message.getMetaDataId(), ContentType.PROCESSED_RESPONSE, responseString, encryptor.encrypt(responseString));
        message.setProcessedResponse(processedResponse);

        if (storageSettings.isStoreProcessedResponse()) {
            ThreadUtils.checkInterruptedStatus();

            if (message.getProcessedResponse() != null) {
                dao.storeMessageContent(processedResponse);
            } else {
                dao.insertMessageContent(processedResponse);
            }
        }
    }

    private void afterResponse(DonkeyDao dao, ConnectorMessage connectorMessage, Response response, Status previousStatus) {
        // the response status from the response transformer should be one of: FILTERED, ERROR, SENT, or QUEUED
        connectorMessage.setStatus(response.getNewMessageStatus());
        dao.updateStatus(connectorMessage, previousStatus);
        previousStatus = connectorMessage.getStatus();
    }

    private void fixResponseStatus(Response response) {
        if (response != null) {
            Status status = response.getNewMessageStatus();

            if (status != Status.FILTERED && status != Status.ERROR && status != Status.SENT && status != Status.QUEUED) {
                // If the response is invalid for a final destination status, change the status to ERROR
                response.setNewMessageStatus(Status.ERROR);
            } else if (!isQueueEnabled() && status == Status.QUEUED) {
                // If the status is QUEUED and queuing is disabled, change the status to ERROR
                response.setNewMessageStatus(Status.ERROR);
            }
        }
    }
}
