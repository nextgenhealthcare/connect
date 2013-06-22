/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.channel.ChannelState;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DispatcherConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.QueueConnectorProperties;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.controllers.MessageController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.queue.ConnectorMessageQueue;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.donkey.util.ThreadUtils;

public abstract class DestinationConnector extends Connector implements Runnable {
    private final static String QUEUED_RESPONSE = "Message queued successfully";

    private Integer orderId;
    private Thread thread;
    private QueueConnectorProperties queueProperties;
    private ConnectorMessageQueue queue = new ConnectorMessageQueue();
    private String destinationName;
    private boolean enabled;
    private AtomicBoolean forceQueue = new AtomicBoolean(false);
    private ResponseTransformerExecutor responseTransformerExecutor;
    private StorageSettings storageSettings = new StorageSettings();
    private DonkeyDaoFactory daoFactory;
    private Logger logger = Logger.getLogger(getClass());

    public abstract void replaceConnectorProperties(ConnectorProperties connectorProperties, ConnectorMessage message);

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

    public boolean isForceQueue() {
        return forceQueue.get();
    }

    public void setForceQueue(boolean forceQueue) {
        this.forceQueue.set(forceQueue);
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    @Override
    public void setConnectorProperties(ConnectorProperties connectorProperties) {
        super.setConnectorProperties(connectorProperties);

        if (connectorProperties instanceof DispatcherConnectorPropertiesInterface) {
            this.queueProperties = ((DispatcherConnectorPropertiesInterface) connectorProperties).getQueueConnectorProperties();
        }
    }

    public ResponseTransformerExecutor getResponseTransformerExecutor() {
        return responseTransformerExecutor;
    }

    public void setResponseTransformerExecutor(ResponseTransformerExecutor responseTransformerExecutor) {
        this.responseTransformerExecutor = responseTransformerExecutor;
    }

    protected void setStorageSettings(StorageSettings storageSettings) {
        this.storageSettings = storageSettings;
    }

    protected void setDaoFactory(DonkeyDaoFactory daoFactory) {
        this.daoFactory = daoFactory;
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
            // Remove any items in the queue's buffer because they may be outdated and refresh the queue size
            queue.invalidate(true);

            thread = new Thread(this);
            thread.start();
        }

        onStart();

        /*
         * If forceQueue was enabled because this connector was stopped individually, disable it
         * AFTER onStart() so make sure the connector does not attempt to send before it is finished
         * starting.
         */
        forceQueue.set(false);

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
    public void halt() throws HaltException {
        setCurrentState(ChannelState.STOPPING);

        if (thread != null) {
            try {
                thread.interrupt();
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new HaltException("Failed to stop destination connector for channel: " + getChannelId(), e);
            }
        }

        onHalt();

        setCurrentState(ChannelState.STOPPED);
    }

    private MessageContent getSentContent(ConnectorMessage message, ConnectorProperties connectorProperties) {
        // TODO: store the serializer as a class variable?
        String content = Donkey.getInstance().getSerializer().serialize(connectorProperties);
        return new MessageContent(message.getChannelId(), message.getMessageId(), message.getMetaDataId(), ContentType.SENT, content, null, false);
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
        boolean attemptSend = !isQueueEnabled() || (queueProperties.isSendFirst() && queue.size() == 0 && !isForceQueue());

        // we need to get the connector envelope if we will be attempting to send the message     
        if (attemptSend) {
            ThreadUtils.checkInterruptedStatus();

            // have the connector generate the connector envelope and store it in the message
            connectorProperties = ((DispatcherConnectorPropertiesInterface) getConnectorProperties()).clone();
            replaceConnectorProperties(connectorProperties, message);

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
                // Check to see if the connector has been interrupted before each send attempt
                ThreadUtils.checkInterruptedStatus();

                // pause for the given retry interval if this is not the first send attempt
                if (sendAttempts > 0) {
                    Thread.sleep(queueProperties.getRetryIntervalMillis());
                }

                // have the connector send the message and return a response
                response = handleSend(connectorProperties, message);
                // NOTE: Send attempts here will not be persisted until all attempts have completed since there is only one transaction.
                // Each attempt from the queue will be persisted though.
                message.setSendAttempts(++sendAttempts);
                response.fixStatus(isQueueEnabled());
                responseStatus = response.getStatus();
            } while ((responseStatus == Status.ERROR || responseStatus == Status.QUEUED) && (sendAttempts - 1) < retryCount);

            afterSend(dao, message, response, previousStatus);
        } else {
            message.getResponseMap().put("d" + String.valueOf(getMetaDataId()), new Response(Status.QUEUED, QUEUED_RESPONSE));

            if (storageSettings.isStoreResponseMap()) {
                dao.updateResponseMap(message);
                ThreadUtils.checkInterruptedStatus();
            }

            dao.updateStatus(message, previousStatus);
        }
    }

    /**
     * Process a connector message with PENDING status
     * 
     * @throws InterruptedException
     */
    public void processPendingConnectorMessage(DonkeyDao dao, ConnectorMessage message) throws InterruptedException {
        Serializer serializer = Donkey.getInstance().getSerializer();
        Response response = (Response) serializer.deserialize(message.getResponse().getContent());

        // ResponseTransformerExecutor could be null if the ResponseTransformer was removed before recovering
        if (responseTransformerExecutor != null) {
            try {
                responseTransformerExecutor.runResponseTransformer(dao, message, response, isQueueEnabled(), storageSettings, serializer);
            } catch (DonkeyException e) {
                logger.error("Error executing response transformer for channel " + message.getChannelId() + " (" + destinationName + ").", e);
                response.setStatus(Status.ERROR);
                response.setError(e.getFormattedError());
                message.setProcessingError(message.getProcessingError() != null ? message.getProcessingError() + System.getProperty("line.separator") + System.getProperty("line.separator") + e.getFormattedError() : e.getFormattedError());
                dao.updateErrors(message);
                return;
            }
        }

        afterResponse(dao, message, response, message.getStatus());
    }

    @Override
    public void run() {
        DonkeyDao dao = null;
        try {
            Serializer serializer = Donkey.getInstance().getSerializer();
            ConnectorMessage connectorMessage = null;
            int retryIntervalMillis = queueProperties.getRetryIntervalMillis();
            Long lastMessageId = null;

            do {
                connectorMessage = queue.peek();

                if (connectorMessage != null) {
                    /*
                     * If the last message id is equal to the current message id, then the message
                     * was not successfully send and is being retried, so wait the retry interval.
                     * 
                     * If the last message id is greater than the current message id, then some
                     * message was not successful, message rotation is on, and the queue is back to
                     * the oldest message, so wait the retry interval.
                     */
                    if (lastMessageId != null && lastMessageId >= connectorMessage.getMessageId()) {
                        Thread.sleep(retryIntervalMillis);
                    }

                    lastMessageId = connectorMessage.getMessageId();

                    try {
                        dao = daoFactory.getDao();
                        Status previousStatus = connectorMessage.getStatus();

                        Class<?> connectorPropertiesClass = getConnectorProperties().getClass();
                        Class<?> serializedPropertiesClass = null;

                        ConnectorProperties connectorProperties = null;

                        // Generate the template if we are regenerating, or if this is the first send attempt (in which case the sent content should be null).
                        if (queueProperties.isRegenerateTemplate() || connectorMessage.getSent() == null) {
                            ThreadUtils.checkInterruptedStatus();
                            connectorProperties = ((DispatcherConnectorPropertiesInterface) getConnectorProperties()).clone();

                            boolean updateProperties = true;
                            if (connectorMessage.getSent() == null) {
                                serializedPropertiesClass = connectorProperties.getClass();
                            } else {
                                serializedPropertiesClass = serializer.getClass(connectorMessage.getSent().getContent());

                                // If the serialized properties do not match, don't update the properties.
                                updateProperties = (serializedPropertiesClass == connectorPropertiesClass);
                            }

                            if (updateProperties) {
                                replaceConnectorProperties(connectorProperties, connectorMessage);
                                MessageContent sentContent = getSentContent(connectorMessage, connectorProperties);
                                connectorMessage.setSent(sentContent);

                                if (sentContent != null && storageSettings.isStoreSent()) {
                                    ThreadUtils.checkInterruptedStatus();
                                    dao.storeMessageContent(sentContent);
                                }
                            }
                        } else {
                            connectorProperties = (ConnectorProperties) serializer.deserialize(connectorMessage.getSent().getContent());

                            serializedPropertiesClass = connectorProperties.getClass();
                        }

                        /*
                         * Verify that the connector properties stored in the connector message
                         * match the properties from the current connector. Otherwise the connector
                         * type has changed and the message will be set to errored.
                         */
                        if (serializedPropertiesClass == connectorPropertiesClass) {
                            ThreadUtils.checkInterruptedStatus();
                            Response response = handleSend(connectorProperties, connectorMessage);
                            connectorMessage.setSendAttempts(connectorMessage.getSendAttempts() + 1);

                            if (response == null) {
                                throw new RuntimeException("Received null response from destination " + destinationName + ".");
                            }
                            response.fixStatus(isQueueEnabled());

                            afterSend(dao, connectorMessage, response, previousStatus);

                            /*
                             * if the "remove content on completion" setting is
                             * enabled, we will need to
                             * retrieve a list of the other connector messages for
                             * this message id and
                             * check if the message is "completed"
                             */
                            if (storageSettings.isRemoveContentOnCompletion() || storageSettings.isRemoveAttachmentsOnCompletion()) {
                                Map<Integer, ConnectorMessage> connectorMessages = dao.getConnectorMessages(getChannelId(), connectorMessage.getMessageId());

                                // update the map with the message that was just sent
                                connectorMessages.put(getMetaDataId(), connectorMessage);

                                if (MessageController.getInstance().isMessageCompleted(connectorMessages)) {
                                    if (storageSettings.isRemoveContentOnCompletion()) {
                                        dao.deleteMessageContent(getChannelId(), connectorMessage.getMessageId());
                                    }

                                    if (storageSettings.isRemoveAttachmentsOnCompletion()) {
                                        dao.deleteMessageAttachments(getChannelId(), connectorMessage.getMessageId());
                                    }
                                }
                            }
                        } else {
                            connectorMessage.setStatus(Status.ERROR);
                            connectorMessage.setProcessingError("Mismatched connector properties detected in queued message. The connector type may have changed since the message was queued.\nFOUND: " + serializedPropertiesClass.getSimpleName() + "\nEXPECTED: " + connectorPropertiesClass.getSimpleName());

                            dao.updateStatus(connectorMessage, previousStatus);
                            dao.updateErrors(connectorMessage);
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
                        } else if (queueProperties.isRotate()) {
                            // If the message is still queued and rotation is enabled, notify the queue that the message is to be rotated.
                            synchronized (queue) {
                                ConnectorMessage firstMessage = queue.peek();
                                if (connectorMessage.getMessageId() == firstMessage.getMessageId() && connectorMessage.getMetaDataId() == firstMessage.getMetaDataId()) {
                                    queue.rotate(connectorMessage);
                                }
                            }
                        }
                    } catch (RuntimeException e) {
                        logger.error("Error processing queued " + (connectorMessage != null ? connectorMessage.toString() : "message (null)") + " for channel " + getChannelId() + " (" + destinationName + "). This error is expected if the message was manually removed from the queue.", e);
                        // Invalidate the queue's buffer if any errors occurred. If the message being processed by the queue was deleted,
                        // This will prevent the queue from trying to process that message repeatedly.
                        queue.invalidate(true);
                    } finally {
                        if (dao != null) {
                            dao.close();
                        }
                    }
                } else {
                    /*
                     * This is necessary because there is no blocking peek. If the queue is empty,
                     * wait some time to free up the cpu.
                     */
                    Thread.sleep(Constants.DESTINATION_QUEUE_EMPTY_SLEEP_TIME);
                }
            } while (getCurrentState() == ChannelState.STARTED || getCurrentState() == ChannelState.STARTING);
        } catch (InterruptedException e) {
        } catch (Exception e) {
            logger.error(e);
        } finally {
            // Invalidate the queue's buffer when the queue is stopped to prevent the buffer becoming 
            // unsynchronized with the data store.
            queue.invalidate(false);

            if (dao != null) {
                dao.close();
            }
        }
    }

    private Response handleSend(ConnectorProperties connectorProperties, ConnectorMessage message) throws InterruptedException {
        message.setSendDate(Calendar.getInstance());
        Response response = send(connectorProperties, message);
        message.setResponseDate(Calendar.getInstance());

        return response;
    }

    private void afterSend(DonkeyDao dao, ConnectorMessage message, Response response, Status previousStatus) throws InterruptedException {
        Serializer serializer = Donkey.getInstance().getSerializer();

        if (storageSettings.isStoreResponse()) {
            String responseString = serializer.serialize(response);
            MessageContent responseContent = new MessageContent(message.getChannelId(), message.getMessageId(), message.getMetaDataId(), ContentType.RESPONSE, responseString, responseTransformerExecutor.getInbound().getType(), false);

            ThreadUtils.checkInterruptedStatus();

            if (message.getResponse() != null) {
                dao.storeMessageContent(responseContent);
            } else {
                dao.insertMessageContent(responseContent);
            }

            message.setResponse(responseContent);
        }

        ThreadUtils.checkInterruptedStatus();

        /*
         * If the response transformer (and serializer) will run, change the
         * current status to PENDING so it can be recovered. Still call
         * runResponseTransformer so that transformWithoutSerializing can still
         * run
         */
        if (responseTransformerExecutor.isActive(response)) {
            message.setStatus(Status.PENDING);
            dao.updateStatus(message, previousStatus);
            dao.commit(storageSettings.isDurable());
            previousStatus = message.getStatus();
        }

        try {
            // Perform transformation
            responseTransformerExecutor.runResponseTransformer(dao, message, response, isQueueEnabled(), storageSettings, serializer);

            String error = null;
            if (StringUtils.isNotBlank(response.getError())) {
                error = response.getError();
            }

            message.setProcessingError(error);
            // Insert errors if necessary
            if (message.getErrorCode() > 0) {
                dao.updateErrors(message);
            }
        } catch (DonkeyException e) {
            logger.error("Error executing response transformer for channel " + message.getChannelId() + " (" + destinationName + ").", e);
            response.setStatus(Status.ERROR);
            response.setError(e.getFormattedError());
            message.setStatus(response.getStatus());
            message.setProcessingError(message.getProcessingError() != null ? message.getProcessingError() + System.getProperty("line.separator") + System.getProperty("line.separator") + e.getFormattedError() : e.getFormattedError());
            dao.updateStatus(message, previousStatus);
            dao.updateErrors(message);
            return;
        }

        message.getResponseMap().put("d" + String.valueOf(getMetaDataId()), response);

        if (storageSettings.isStoreMaps()) {
            dao.updateMaps(message);
        }

        ThreadUtils.checkInterruptedStatus();
        afterResponse(dao, message, response, previousStatus);
    }

    private void afterResponse(DonkeyDao dao, ConnectorMessage connectorMessage, Response response, Status previousStatus) {
        // the response status from the response transformer should be one of: FILTERED, ERROR, SENT, or QUEUED
        connectorMessage.setStatus(response.getStatus());
        dao.updateStatus(connectorMessage, previousStatus);
        previousStatus = connectorMessage.getStatus();
    }
}
