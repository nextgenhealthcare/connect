/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.util.ThreadUtils;

public class DestinationChain implements Callable<List<ConnectorMessage>> {

    private DestinationChainProvider chainProvider;
    private ConnectorMessage message;
    private List<Integer> enabledMetaDataIds = new ArrayList<Integer>();
    private Logger logger = Logger.getLogger(getClass());
    private String name;

    public DestinationChain(DestinationChainProvider chainProvider) {
        this.chainProvider = chainProvider;
        enabledMetaDataIds = new ArrayList<Integer>(chainProvider.getMetaDataIds());
        name = "Destination Chain Thread on " + chainProvider.getChannelId();
    }

    public void setMessage(ConnectorMessage message) {
        this.message = message;
    }

    public List<Integer> getEnabledMetaDataIds() {
        return enabledMetaDataIds;
    }

    public void setEnabledMetaDataIds(List<Integer> enabledMetaDataIds) {
        this.enabledMetaDataIds = enabledMetaDataIds;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<ConnectorMessage> call() throws InterruptedException {
        String originalThreadName = Thread.currentThread().getName();
        try {
            Thread.currentThread().setName(name + " < " + originalThreadName);
            return doCall();
        } finally {
            Thread.currentThread().setName(originalThreadName);
        }
    }

    private List<ConnectorMessage> doCall() throws InterruptedException {
        List<ConnectorMessage> messages = new ArrayList<ConnectorMessage>();
        ConnectorMessage message = this.message;
        int startMetaDataId = enabledMetaDataIds.indexOf(message.getMetaDataId());
        boolean stopChain = false;

        /*
         * The message that we're starting with should be associated with one of the destinations in
         * this chain, if it's not, we can't proceed.
         */
        if (startMetaDataId == -1) {
            logger.error("The message's metadata ID for channel " + chainProvider.getChannelId() + " is not in the destination chain's list of enabled metadata IDs");
            return null;
        }

        // loop through each metaDataId in the chain, beginning with startMetaDataId
        for (int i = startMetaDataId; i < enabledMetaDataIds.size() && !stopChain; i++) {
            ThreadUtils.checkInterruptedStatus();
            Integer metaDataId = enabledMetaDataIds.get(i);
            Integer nextMetaDataId = (enabledMetaDataIds.size() > (i + 1)) ? enabledMetaDataIds.get(i + 1) : null;
            ConnectorMessage nextMessage = null;
            DestinationConnector destinationConnector = chainProvider.getDestinationConnectors().get(metaDataId);

            /*
             * TRANSACTION: Process Destination - Insert the custom metadata column data - store the
             * transformed content - store the encoded content - store the sent content (done prior
             * to sending since the sent content would be lost if the message gets queued) - store
             * the raw response content - update the message status to either PENDING or QUEUED - if
             * there is a next destination in the chain, create it's message (done in the next
             * transaction if a response transformer is used)
             */
            DonkeyDao dao = chainProvider.getDaoFactory().getDao();

            try {
                Status previousStatus = message.getStatus();

                try {
                    switch (message.getStatus()) {
                        case RECEIVED:
                            /*
                             * Only transform the message if we're going to be dispatching it in the
                             * main processing thread, or if the queue thread will not be handling
                             * transformation
                             */
                            if (destinationConnector.willAttemptSend() || !destinationConnector.includeFilterTransformerInQueue()) {
                                destinationConnector.transform(dao, message, previousStatus, true);

                                // If the message status is QUEUED, send it to the destination connector
                                if (message.getStatus() == Status.QUEUED) {
                                    String originalThreadName = Thread.currentThread().getName();
                                    try {
                                        Thread.currentThread().setName(destinationConnector.getConnectorProperties().getName() + " Process Thread on " + destinationConnector.getChannel().getName() + " (" + chainProvider.getChannelId() + "), " + destinationConnector.getDestinationName() + " (" + metaDataId + ")");
                                        destinationConnector.process(dao, message, previousStatus);
                                    } finally {
                                        Thread.currentThread().setName(originalThreadName);
                                    }
                                } else if (message.getStatus() == Status.ERROR && message.getSent() == null) {
                                    // If an error occurred in the filter/transformer, don't proceed with the rest of the chain
                                    stopChain = true;
                                }
                            } else {
                                destinationConnector.updateQueuedStatus(dao, message, previousStatus);
                            }
                            break;

                        case PENDING:
                            chainProvider.getDestinationConnectors().get(metaDataId).processPendingConnectorMessage(dao, message);
                            break;

                        case SENT:
                            break;

                        default:
                            // the status should never be anything but one of the above statuses, but in case it's not, log an error
                            logger.error("Received a message with an invalid status in channel " + chainProvider.getChannelId() + ".");
                            break;
                    }
                } catch (RuntimeException e) { // TODO: remove this catch since we can't determine an error code
                    // if an error occurred in processing the message through the current destination, then update the message status to ERROR and continue processing through the chain
                    logger.error("Error processing destination " + chainProvider.getDestinationConnectors().get(metaDataId).getDestinationName() + " for channel " + chainProvider.getChannelId() + ".", e);
                    stopChain = true;
                    dao.rollback();
                    message.setStatus(Status.ERROR);
                    message.setProcessingError(e.toString());
                    dao.updateStatus(message, previousStatus);
                    // Insert errors if necessary
                    if (StringUtils.isNotBlank(message.getProcessingError())) {
                        dao.updateErrors(message);
                    }
                }

                // now that we're finished processing the current message, we can create the next message in the chain
                if (nextMetaDataId != null && !stopChain) {
                    nextMessage = new ConnectorMessage(message.getChannelId(), message.getChannelName(), message.getMessageId(), nextMetaDataId, message.getServerId(), Calendar.getInstance(), Status.RECEIVED);

                    DestinationConnector nextDestinationConnector = chainProvider.getDestinationConnectors().get(nextMetaDataId);
                    nextMessage.setConnectorName(nextDestinationConnector.getDestinationName());
                    nextMessage.setChainId(chainProvider.getChainId());
                    nextMessage.setOrderId(nextDestinationConnector.getOrderId());

                    // We don't create a new map here because the source map is read-only and thus won't ever be changed
                    nextMessage.setSourceMap(message.getSourceMap());
                    nextMessage.setChannelMap(new HashMap<String, Object>(message.getChannelMap()));
                    nextMessage.setResponseMap(new HashMap<String, Object>(message.getResponseMap()));
                    nextMessage.setRaw(new MessageContent(message.getChannelId(), message.getMessageId(), nextMetaDataId, ContentType.RAW, message.getRaw().getContent(), nextDestinationConnector.getInboundDataType().getType(), message.getRaw().isEncrypted()));

                    ThreadUtils.checkInterruptedStatus();
                    dao.insertConnectorMessage(nextMessage, chainProvider.getStorageSettings().isStoreMaps(), true);
                }

                ThreadUtils.checkInterruptedStatus();

                if (message.getStatus() != Status.QUEUED) {
                    dao.commit(chainProvider.getStorageSettings().isDurable());
                } else {
                    // Block other threads from reading from or modifying the destination queue until both the current commit and queue addition finishes
                    // Otherwise the same message could be sent multiple times.
                    synchronized (destinationConnector.getQueue()) {
                        dao.commit(chainProvider.getStorageSettings().isDurable());

                        if (message.getStatus() == Status.QUEUED) {
                            destinationConnector.getQueue().add(message);
                        }
                    }
                }

                messages.add(message);
            } catch (RuntimeException e) {
                // An exception caught at this point either occurred when attempting to handle an exception in the above try/catch, or when attempting to create the next destination's message, the thread cannot continue running
                logger.error("Error processing destination " + chainProvider.getDestinationConnectors().get(metaDataId).getDestinationName() + " for channel " + chainProvider.getChannelId() + ".", e);
                throw e;
            } finally {
                dao.close();
            }

            // Set the next message in the loop
            if (nextMetaDataId != null) {
                message = nextMessage;
            }
        }

        return messages;
    }
}