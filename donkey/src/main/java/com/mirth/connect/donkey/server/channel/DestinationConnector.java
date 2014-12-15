/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.channel.DestinationConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.DeployedStateEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.event.DeployedStateEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.donkey.server.message.ResponseValidator;
import com.mirth.connect.donkey.server.queue.DestinationQueue;
import com.mirth.connect.donkey.util.MessageMaps;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.donkey.util.ThreadUtils;

public abstract class DestinationConnector extends Connector implements Runnable {
    private final static String QUEUED_RESPONSE = "Message queued successfully";

    private Integer orderId;
    private Map<Long, Thread> queueThreads = new HashMap<Long, Thread>();
    private DestinationConnectorProperties destinationConnectorProperties;
    private DestinationQueue queue;
    private String destinationName;
    private boolean enabled;
    private AtomicBoolean forceQueue = new AtomicBoolean(false);
    private ResponseValidator responseValidator;
    private ResponseTransformerExecutor responseTransformerExecutor;
    private StorageSettings storageSettings = new StorageSettings();
    private DonkeyDaoFactory daoFactory;
    private Logger logger = Logger.getLogger(getClass());
    private AtomicBoolean attemptedFirst = new AtomicBoolean(false);

    public abstract void replaceConnectorProperties(ConnectorProperties connectorProperties, ConnectorMessage message);

    public abstract Response send(ConnectorProperties connectorProperties, ConnectorMessage message) throws InterruptedException;

    public DestinationQueue getQueue() {
        return queue;
    }

    public void setQueue(DestinationQueue queue) {
        this.queue = queue;
    }

    /**
     * Returns a unique id that the dispatcher can use for thread safety. If queuing is disabled or
     * if there is only 1 queue thread, returns -1. If there are multiple queue threads, returns the
     * thread's id if the current thread is a queue thread, otherwise it returns -1.
     */
    public long getDispatcherId() {
        long threadId = Thread.currentThread().getId();
        if (queueThreads.size() <= 1 || !queueThreads.containsKey(threadId)) {
            threadId = -1L;
        }

        return threadId;
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

    public Serializer getSerializer() {
        return channel.getSerializer();
    }

    public MessageMaps getMessageMaps() {
        return channel.getMessageMaps();
    }

    @Override
    public void setConnectorProperties(ConnectorProperties connectorProperties) {
        super.setConnectorProperties(connectorProperties);

        if (connectorProperties instanceof DestinationConnectorPropertiesInterface) {
            this.destinationConnectorProperties = ((DestinationConnectorPropertiesInterface) connectorProperties).getDestinationConnectorProperties();
        }
    }

    public ResponseValidator getResponseValidator() {
        return responseValidator;
    }

    public void setResponseValidator(ResponseValidator responseValidator) {
        this.responseValidator = responseValidator;
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
        return (destinationConnectorProperties != null && destinationConnectorProperties.isQueueEnabled());
    }

    /**
     * Tells whether or not queue rotation is enabled
     */
    public boolean isQueueRotate() {
        return (destinationConnectorProperties != null && destinationConnectorProperties.isRotate());
    }

    protected AttachmentHandler getAttachmentHandler() {
        return channel.getAttachmentHandler();
    }

    public void updateCurrentState(DeployedState currentState) {
        setCurrentState(currentState);
        channel.getEventDispatcher().dispatchEvent(new DeployedStateEvent(getChannelId(), channel.getName(), getMetaDataId(), destinationName, DeployedStateEventType.getTypeFromDeployedState(currentState)));
    }

    public void start() throws ConnectorTaskException, InterruptedException {
        updateCurrentState(DeployedState.STARTING);

        onStart();

        /*
         * If forceQueue was enabled because this connector was stopped individually, disable it
         * AFTER onStart() so make sure the connector does not attempt to send before it is finished
         * starting.
         */
        forceQueue.set(false);

        updateCurrentState(DeployedState.STARTED);
    }

    public void startQueue() {
        if (isQueueEnabled()) {
            // Remove any items in the queue's buffer because they may be outdated and refresh the queue size
            queue.invalidate(true, true);

            for (int i = 0; i < destinationConnectorProperties.getThreadCount(); i++) {
                Thread thread = new Thread(this);
                thread.start();
                queueThreads.put(thread.getId(), thread);
            }
        }
    }

    public void stop() throws ConnectorTaskException, InterruptedException {
        updateCurrentState(DeployedState.STOPPING);

        if (MapUtils.isNotEmpty(queueThreads)) {
            try {
                for (Thread thread : queueThreads.values()) {
                    thread.join();
                }

                queueThreads.clear();
            } finally {
                // Invalidate the queue's buffer when the queue is stopped to prevent the buffer becoming 
                // unsynchronized with the data store.
                queue.invalidate(false, true);
            }
        }

        try {
            onStop();
            updateCurrentState(DeployedState.STOPPED);
        } catch (Throwable t) {
            Throwable cause = t;

            if (cause instanceof ConnectorTaskException) {
                cause = cause.getCause();
            }
            if (cause instanceof ExecutionException) {
                cause = cause.getCause();
            }
            if (cause instanceof InterruptedException) {
                throw (InterruptedException) cause;
            }

            updateCurrentState(DeployedState.STOPPED);

            if (t instanceof ConnectorTaskException) {
                throw (ConnectorTaskException) t;
            } else {
                throw new ConnectorTaskException(t);
            }
        }
    }

    public void halt() throws ConnectorTaskException, InterruptedException {
        updateCurrentState(DeployedState.STOPPING);

        if (MapUtils.isNotEmpty(queueThreads)) {
            for (Thread thread : queueThreads.values()) {
                thread.interrupt();
            }
        }

        try {
            onHalt();
        } finally {
            if (MapUtils.isNotEmpty(queueThreads)) {
                try {
                    for (Thread thread : queueThreads.values()) {
                        thread.join();
                    }

                    queueThreads.clear();
                } finally {
                    // Invalidate the queue's buffer when the queue is stopped to prevent the buffer becoming 
                    // unsynchronized with the data store.
                    queue.invalidate(false, true);
                }
            }

            channel.getEventDispatcher().dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
            updateCurrentState(DeployedState.STOPPED);
        }
    }

    private MessageContent getSentContent(ConnectorMessage message, ConnectorProperties connectorProperties) {
        String content = channel.getSerializer().serialize(connectorProperties);
        return new MessageContent(message.getChannelId(), message.getMessageId(), message.getMetaDataId(), ContentType.SENT, content, null, false);
    }

    /**
     * Process a transformed message. Attempt to send the message unless the destination connector
     * is configured to immediately queue messages.
     * 
     * @return The status of the message at the end of processing. If the message was placed in the
     *         destination connector queue, then QUEUED is returned.
     * @throws InterruptedException
     */
    public void process(DonkeyDao dao, ConnectorMessage message, Status previousStatus) throws InterruptedException {
        ConnectorProperties connectorProperties = null;
        boolean attemptSend = !isQueueEnabled() || (destinationConnectorProperties.isSendFirst() && queue.size() == 0 && !isForceQueue());

        ThreadUtils.checkInterruptedStatus();

        // have the connector generate the connector envelope and store it in the message
        connectorProperties = ((DestinationConnectorPropertiesInterface) getConnectorProperties()).clone();
        replaceConnectorProperties(connectorProperties, message);
        // Cache the replaced connector properties here so that the queue can use it later
        message.setSentProperties(connectorProperties);

        if (storageSettings.isStoreSent()) {
            ThreadUtils.checkInterruptedStatus();

            MessageContent sentContent = getSentContent(message, connectorProperties);
            message.setSent(sentContent);

            if (sentContent != null) {
                ThreadUtils.checkInterruptedStatus();
                dao.insertMessageContent(sentContent);
            }
        }

        // we need to get the connector envelope if we will be attempting to send the message     
        if (attemptSend) {
            int retryCount = (destinationConnectorProperties == null) ? 0 : destinationConnectorProperties.getRetryCount();
            int sendAttempts = 0;
            Response response = null;
            Status responseStatus = null;

            do {
                // Check to see if the connector has been interrupted before each send attempt
                ThreadUtils.checkInterruptedStatus();

                // pause for the given retry interval if this is not the first send attempt
                if (sendAttempts > 0) {
                    Thread.sleep(destinationConnectorProperties.getRetryIntervalMillis());
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

            if (message.getStatus() == Status.QUEUED) {
                attemptedFirst.set(true);
            }
        } else {
            message.getResponseMap().put("d" + String.valueOf(getMetaDataId()), new Response(Status.QUEUED, "", QUEUED_RESPONSE));

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
        Serializer serializer = channel.getSerializer();
        Response response = serializer.deserialize(message.getResponse().getContent(), Response.class);

        // ResponseTransformerExecutor could be null if the ResponseTransformer was removed before recovering
        if (responseTransformerExecutor != null) {
            try {
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
                logger.error("Error executing response transformer for channel " + channel.getName() + " (" + channel.getChannelId() + ") on destination " + destinationName + ".", e);
                response.setStatus(Status.ERROR);
                response.setError(e.getFormattedError());
                message.setProcessingError(message.getProcessingError() != null ? message.getProcessingError() + System.getProperty("line.separator") + System.getProperty("line.separator") + e.getFormattedError() : e.getFormattedError());
                dao.updateErrors(message);
                return;
            }

            message.getResponseMap().put("d" + String.valueOf(getMetaDataId()), response);

            // Set the destination connector's custom column map
            boolean wasEmpty = message.getMetaDataMap().isEmpty();
            channel.getSourceConnector().getMetaDataReplacer().setMetaDataMap(message, channel.getMetaDataColumns());

            // Store the custom columns
            if (storageSettings.isStoreCustomMetaData() && !message.getMetaDataMap().isEmpty()) {
                ThreadUtils.checkInterruptedStatus();
                if (wasEmpty) {
                    dao.insertMetaData(message, channel.getMetaDataColumns());
                } else {
                    dao.storeMetaData(message, channel.getMetaDataColumns());
                }
            }

            if (storageSettings.isStoreMaps()) {
                dao.updateMaps(message);
            }
        }

        afterResponse(dao, message, response, message.getStatus());
    }

    @Override
    public void run() {
        DonkeyDao dao = null;
        Serializer serializer = channel.getSerializer();
        ConnectorMessage connectorMessage = null;
        int retryIntervalMillis = destinationConnectorProperties.getRetryIntervalMillis();
        Long lastMessageId = null;
        boolean canAcquire = true;
        queue.registerThreadId();

        do {
            try {
                if (canAcquire) {
                    connectorMessage = queue.acquire();
                }

                if (connectorMessage != null) {
                    boolean exceptionCaught = false;

                    try {
                        /*
                         * If the last message id is equal to the current message id, then the
                         * message was not successfully send and is being retried, so wait the retry
                         * interval.
                         * 
                         * If the last message id is greater than the current message id, then some
                         * message was not successful, message rotation is on, and the queue is back
                         * to the oldest message, so wait the retry interval.
                         */
                        if (attemptedFirst.getAndSet(false) || (lastMessageId != null && lastMessageId >= connectorMessage.getMessageId())) {
                            Thread.sleep(retryIntervalMillis);
                        }

                        lastMessageId = connectorMessage.getMessageId();

                        dao = daoFactory.getDao();
                        Status previousStatus = connectorMessage.getStatus();

                        Class<?> connectorPropertiesClass = getConnectorProperties().getClass();
                        Class<?> serializedPropertiesClass = null;

                        ConnectorProperties connectorProperties = null;

                        // Generate the template if necessary
                        if (destinationConnectorProperties.isRegenerateTemplate()) {
                            ThreadUtils.checkInterruptedStatus();
                            connectorProperties = ((DestinationConnectorPropertiesInterface) getConnectorProperties()).clone();

                            serializedPropertiesClass = serializer.getClass(connectorMessage.getSent().getContent());

                            // If the serialized properties do not match, don't update the properties.
                            if (serializedPropertiesClass == connectorPropertiesClass) {
                                replaceConnectorProperties(connectorProperties, connectorMessage);
                                MessageContent sentContent = getSentContent(connectorMessage, connectorProperties);
                                connectorMessage.setSent(sentContent);

                                if (sentContent != null && storageSettings.isStoreSent()) {
                                    ThreadUtils.checkInterruptedStatus();
                                    dao.storeMessageContent(sentContent);
                                }
                            }
                        } else {
                            // Attempt to get the sent properties from the in-memory cache. If it doesn't exist, deserialize from the actual sent content.
                            connectorProperties = connectorMessage.getSentProperties();
                            if (connectorProperties == null) {
                                connectorProperties = serializer.deserialize(connectorMessage.getSent().getContent(), ConnectorProperties.class);
                                connectorMessage.setSentProperties(connectorProperties);
                            }

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
                        } else {
                            connectorMessage.setStatus(Status.ERROR);
                            connectorMessage.setProcessingError("Mismatched connector properties detected in queued message. The connector type may have changed since the message was queued.\nFOUND: " + serializedPropertiesClass.getSimpleName() + "\nEXPECTED: " + connectorPropertiesClass.getSimpleName());

                            dao.updateStatus(connectorMessage, previousStatus);
                            dao.updateErrors(connectorMessage);
                        }

                        ThreadUtils.checkInterruptedStatus();
                        dao.commit(storageSettings.isDurable());

                        // Only actually attempt to remove content if the status is SENT
                        if (connectorMessage.getStatus().isCompleted()) {
                            try {
                                channel.removeContent(dao, null, lastMessageId, true, true);
                            } catch (RuntimeException e) {
                                /*
                                 * The connector message itself processed successfully, only the
                                 * remove content operation failed. In this case just give up and
                                 * log an error.
                                 */
                                logger.error("Error removing content for message " + lastMessageId + " for channel " + channel.getName() + " (" + channel.getChannelId() + ") on destination " + destinationName + ". This error is expected if the message was manually removed from the queue.", e);
                            }
                        }
                    } catch (RuntimeException e) {
                        logger.error("Error processing queued " + (connectorMessage != null ? connectorMessage.toString() : "message (null)") + " for channel " + channel.getName() + " (" + channel.getChannelId() + ") on destination " + destinationName + ". This error is expected if the message was manually removed from the queue.", e);
                        /*
                         * Invalidate the queue's buffer if any errors occurred. If the message
                         * being processed by the queue was deleted, this will prevent the queue
                         * from trying to process that message repeatedly. Since multiple
                         * queues/threads may need to do this as well, we do not reset the queue's
                         * maps of checked in or deleted messages.
                         */
                        exceptionCaught = true;
                    } finally {
                        if (dao != null) {
                            dao.close();
                        }

                        /*
                         * We always want to release the message if it's done (obviously).
                         */
                        if (exceptionCaught) {
                            /*
                             * If an runtime exception was caught, we can't guarantee whether that
                             * message was deleted or is still in the database. When it is released,
                             * the message will be removed from the in-memory queue. However we need
                             * to invalidate the queue before allowing any other threads to be able
                             * to access it in case the message is still in the database.
                             */
                            canAcquire = true;
                            synchronized (queue) {
                                queue.release(connectorMessage, true);
                                queue.invalidate(true, false);
                            }
                        } else if (connectorMessage.getStatus() != Status.QUEUED) {
                            canAcquire = true;
                            queue.release(connectorMessage, true);
                        } else if (destinationConnectorProperties.isRotate()) {
                            canAcquire = true;
                            queue.release(connectorMessage, false);
                        } else {
                            /*
                             * If the message is still queued, no exception occurred, and queue
                             * rotation is disabled, we still want to force the queue to re-acquire
                             * a message if it has been marked as deleted by another process.
                             */
                            canAcquire = queue.releaseIfDeleted(connectorMessage);
                        }
                    }
                } else {
                    /*
                     * This is necessary because there is no blocking peek. If the queue is empty,
                     * wait some time to free up the cpu.
                     */
                    Thread.sleep(Constants.DESTINATION_QUEUE_EMPTY_SLEEP_TIME);
                }
            } catch (InterruptedException e) {
                // Stop this thread if it was halted
                return;
            } catch (Exception e) {
                logger.warn("Error in queue thread for channel " + channel.getName() + " (" + channel.getChannelId() + ") on destination " + destinationName + ".\n" + ExceptionUtils.getStackTrace(e));
                try {
                    Thread.sleep(retryIntervalMillis);

                    /*
                     * Since the thread already slept for the retry interval, set lastMessageId to
                     * null to prevent sleeping again.
                     */
                    lastMessageId = null;
                } catch (InterruptedException e1) {
                    // Stop this thread if it was halted
                    return;
                }
            }
        } while (getCurrentState() == DeployedState.STARTED || getCurrentState() == DeployedState.STARTING);
    }

    private Response handleSend(ConnectorProperties connectorProperties, ConnectorMessage message) throws InterruptedException {
        message.setSendDate(Calendar.getInstance());
        Response response = send(connectorProperties, message);
        if (response.isValidate() && response.getStatus() == Status.SENT) {
            response = responseValidator.validate(response, message);

            if (response.getStatus() != Status.SENT) {
                channel.getEventDispatcher().dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), message.getMessageId(), ErrorEventType.RESPONSE_VALIDATION, getDestinationName(), connectorProperties.getName(), response.getStatusMessage(), null));
            }
        }
        message.setResponseDate(Calendar.getInstance());

        return response;
    }

    private void afterSend(DonkeyDao dao, ConnectorMessage message, Response response, Status previousStatus) throws InterruptedException {
        Serializer serializer = channel.getSerializer();

        dao.updateSendAttempts(message);

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
         * If the response transformer (and serializer) will run, change the current status to
         * PENDING so it can be recovered. Still call runResponseTransformer so that
         * transformWithoutSerializing can still run
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
            logger.error("Error executing response transformer for channel " + channel.getName() + " (" + channel.getChannelId() + ") on destination " + destinationName + ".", e);
            response.setStatus(Status.ERROR);
            response.setError(e.getFormattedError());
            message.setStatus(response.getStatus());
            message.setProcessingError(message.getProcessingError() != null ? message.getProcessingError() + System.getProperty("line.separator") + System.getProperty("line.separator") + e.getFormattedError() : e.getFormattedError());
            dao.updateStatus(message, previousStatus);
            dao.updateErrors(message);
            return;
        }

        message.getResponseMap().put("d" + String.valueOf(getMetaDataId()), response);

        // Set the destination connector's custom column map
        boolean wasEmpty = message.getMetaDataMap().isEmpty();
        channel.getSourceConnector().getMetaDataReplacer().setMetaDataMap(message, channel.getMetaDataColumns());

        // Store the custom columns
        if (storageSettings.isStoreCustomMetaData() && !message.getMetaDataMap().isEmpty()) {
            ThreadUtils.checkInterruptedStatus();
            if (wasEmpty) {
                dao.insertMetaData(message, channel.getMetaDataColumns());
            } else {
                dao.storeMetaData(message, channel.getMetaDataColumns());
            }
        }

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
