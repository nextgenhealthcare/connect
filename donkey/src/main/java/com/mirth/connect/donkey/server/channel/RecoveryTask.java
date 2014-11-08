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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.util.ThreadUtils;

public class RecoveryTask implements Callable<Void> {
    private Channel channel;
    private Logger logger = Logger.getLogger(getClass());

    public RecoveryTask(Channel channel) {
        this.channel = channel;
    }

    @Override
    public Void call() throws Exception {
        StorageSettings storageSettings = channel.getStorageSettings();
        Long maxMessageId = null;
        // The number of messages that were attempted to be recovered
        long attemptedMessages = 0L;
        // The number of messages that were successfully recovered
        long recoveredMessages = 0L;

        // The buffer size for each sub-task
        int sourceBufferSize = 1;
        int unfinishedBufferSize = 10;
        int pendingBufferSize = 10;
        // The minimum message Id that can be retrieved for the next query.
        long sourceMinMessageId = 0L;
        long unfinishedMinMessageId = 0L;
        long pendingMinMessageId = 0L;
        // The completed status of each sub-task
        boolean sourceComplete = false;
        boolean unfinishedComplete = false;
        boolean pendingComplete = false;
        // The queue buffer for each sub-task
        LinkedList<ConnectorMessage> sourceConnectorMessages = new LinkedList<ConnectorMessage>();
        LinkedList<Message> unfinishedMessages = new LinkedList<Message>();
        LinkedList<Message> pendingMessages = new LinkedList<Message>();

        do {
            ThreadUtils.checkInterruptedStatus();
            DonkeyDao dao = channel.getDaoFactory().getDao();

            try {
                if (maxMessageId == null) {
                    // Cache the max messageId of the channel to be used in the query below
                    maxMessageId = dao.getMaxMessageId(channel.getChannelId());
                }

                if (!sourceComplete && sourceConnectorMessages.isEmpty()) {
                    // Fill the buffer
                    sourceConnectorMessages.addAll(dao.getConnectorMessages(channel.getChannelId(), channel.getServerId(), 0, Status.RECEIVED, 0, sourceBufferSize, sourceMinMessageId, maxMessageId));

                    // Mark the sub-task as completed if no messages were retrieved by the query to prevent the query from running again
                    if (sourceConnectorMessages.isEmpty()) {
                        sourceComplete = true;
                    } else {
                        /*
                         * If the source queue is on, these messages are usually ignored. Therefore
                         * we only retrieve one of these messages until we know for sure that we'll
                         * need to recover them.
                         */
                        sourceBufferSize = 100;
                    }
                }

                if (!unfinishedComplete && unfinishedMessages.isEmpty()) {
                    // Fill the buffer
                    unfinishedMessages.addAll(dao.getUnfinishedMessages(channel.getChannelId(), channel.getServerId(), unfinishedBufferSize, unfinishedMinMessageId));

                    // Mark the sub-task as completed if no messages were retrieved by the query to prevent the query from running again
                    if (unfinishedMessages.isEmpty()) {
                        unfinishedComplete = true;
                    }
                }

                if (!pendingComplete && pendingMessages.isEmpty()) {
                    // Fill the buffer
                    pendingMessages.addAll(dao.getPendingConnectorMessages(channel.getChannelId(), channel.getServerId(), pendingBufferSize, pendingMinMessageId));

                    // Mark the sub-task as completed if no messages were retrieved by the query to prevent the query from running again
                    if (pendingMessages.isEmpty()) {
                        pendingComplete = true;
                    }
                }
            } finally {
                dao.close();
            }

            // Retrieve the first message of each sub-task
            ConnectorMessage sourceConnectorMessage = sourceConnectorMessages.peekFirst();
            Message unfinishedMessage = unfinishedMessages.peekFirst();
            Message pendingMessage = pendingMessages.peekFirst();

            if (!storageSettings.isMessageRecoveryEnabled()) {
                sourceComplete = true;
                unfinishedComplete = true;
                pendingComplete = true;
                if (unfinishedMessage != null || pendingMessage != null || (sourceConnectorMessage != null && channel.getSourceConnector().isRespondAfterProcessing())) {
                    logger.info("Incomplete messages found for channel " + channel.getName() + " (" + channel.getChannelId() + ") but message storage settings do not support recovery. Skipping recovery task.");
                }
            } else {
                Long messageId = null;

                try {
                    /*
                     * Perform a 3-way merge. The sub-task that has the lowest messageId will be
                     * executed first. However it is possible for the unfinishedMessage and
                     * pendingMessage to have the same messageId. In these cases the unfinished
                     * sub-task should be executed and the pending sub-task should be ignored
                     */
                    if (sourceConnectorMessage != null && (unfinishedMessage == null || sourceConnectorMessage.getMessageId() < unfinishedMessage.getMessageId()) && (pendingMessage == null || sourceConnectorMessage.getMessageId() < pendingMessage.getMessageId())) {
                        if (!channel.getSourceConnector().isRespondAfterProcessing() && unfinishedComplete && pendingComplete) {
                            /*
                             * If the other two sub-tasks are completed already and the source queue
                             * is enabled for this channel, then there is no need to continue
                             * recovering source RECEIVED messages because they will be picked up by
                             * the source queue.
                             */
                            sourceComplete = true;
                        } else {
                            // Store the messageId so we can log it out if an exception occurs
                            messageId = sourceConnectorMessage.getMessageId();
                            // Remove the message from the buffer and update the minMessageId
                            sourceMinMessageId = sourceConnectorMessages.pollFirst().getMessageId() + 1;

                            if (attemptedMessages++ == 0) {
                                logger.info("Starting message recovery for channel " + channel.getName() + " (" + channel.getChannelId() + "). Incomplete messages found.");
                            }

                            // Execute the recovery process for this message
                            channel.process(sourceConnectorMessage, true);
                            // Use this to decrement the queue size
                            channel.getSourceQueue().decrementSize();
                            // Increment the number of successfully recovered messages
                            recoveredMessages++;
                        }
                    } else if (unfinishedMessage != null && (pendingMessage == null || unfinishedMessage.getMessageId() <= pendingMessage.getMessageId())) {
                        // Store the messageId so we can log it out if an exception occurs
                        messageId = unfinishedMessage.getMessageId();
                        // Remove the message from the buffer and update the minMessageId
                        unfinishedMinMessageId = unfinishedMessages.pollFirst().getMessageId() + 1;

                        // If the unfinishedMessage and pendingMessage have the same messageId, remove the pendingMessage from the buffer
                        if (pendingMessage != null && unfinishedMessage.getMessageId() == pendingMessage.getMessageId()) {
                            pendingMinMessageId = pendingMessages.pollFirst().getMessageId() + 1;
                            pendingMessage = pendingMessages.peekFirst();
                        }

                        if (attemptedMessages++ == 0) {
                            logger.info("Starting message recovery for channel " + channel.getName() + " (" + channel.getChannelId() + "). Incomplete messages found.");
                        }

                        // Execute the recovery process for this message
                        recoverUnfinishedMessage(unfinishedMessage);
                        // Increment the number of successfully recovered messages
                        recoveredMessages++;
                    } else if (pendingMessage != null) {
                        // Store the messageId so we can log it out if an exception occurs
                        messageId = pendingMessage.getMessageId();
                        // Remove the message from the buffer and update the minMessageId
                        pendingMinMessageId = pendingMessages.pollFirst().getMessageId() + 1;

                        if (attemptedMessages++ == 0) {
                            logger.info("Starting message recovery for channel " + channel.getName() + " (" + channel.getChannelId() + "). Incomplete messages found.");
                        }

                        // Execute the recovery process for this message
                        recoverPendingMessage(pendingMessage);
                        // Increment the number of successfully recovered messages
                        recoveredMessages++;
                    }
                } catch (InterruptedException e) {
                    // This should only occur if a halt was requested so stop the entire recovery task
                    throw e;
                } catch (Exception e) {
                    /*
                     * If an exception occurs we skip the message and log an error. This is to
                     * prevent one bad exception or message from locking the entire channel.
                     * 
                     * If a non-Exception gets thrown (OutofMemoryError, etc) then it will
                     * intentionally not be caught here and the recovery task will be stopped.
                     */
                    logger.error("Failed to recover message " + messageId + " for channel " + channel.getName() + " (" + channel.getChannelId() + "): \n" + ExceptionUtils.getStackTrace(e));
                }
            }
        } while (!unfinishedComplete || !pendingComplete || !sourceComplete);

        if (attemptedMessages > 0) {
            logger.info("Completed message recovery for channel " + channel.getName() + " (" + channel.getChannelId() + "). Successfully recovered " + recoveredMessages + " out of " + attemptedMessages + " messages.");
        }

        return null;
    }

    private void recoverUnfinishedMessage(Message unfinishedMessage) throws InterruptedException {
        ConnectorMessage sourceMessage = unfinishedMessage.getConnectorMessages().get(0);
        // get the list of destination meta data ids to send to
        Collection<Integer> metaDataIds = null;

        if (sourceMessage.getSourceMap().containsKey(Constants.DESTINATION_SET_KEY)) {
            metaDataIds = (Collection<Integer>) sourceMessage.getSourceMap().get(Constants.DESTINATION_SET_KEY);
        }

        // merge responses from all of the destinations into the source connector's response map
        for (ConnectorMessage connectorMessage : unfinishedMessage.getConnectorMessages().values().toArray(new ConnectorMessage[unfinishedMessage.getConnectorMessages().size()])) {
            Integer metaDataId = connectorMessage.getMetaDataId();
            Status status = connectorMessage.getStatus();

            if (metaDataId != 0) {
                if (status == Status.RECEIVED || status == Status.PENDING) {
                    for (DestinationChain chain : channel.getDestinationChains()) {
                        List<Integer> chainMetaDataIds = chain.getMetaDataIds();
                        if (chainMetaDataIds.contains(metaDataId)) {
                            try {
                                List<Integer> enabledMetaDataIds = new ArrayList<Integer>();
                                // The order of the enabledMetaDataId list needs to be based on the chain order.
                                // We do not use ListUtils here because there is no official guarantee of order.
                                for (Integer id : chainMetaDataIds) {
                                    if (metaDataIds == null || metaDataIds.contains(id)) {
                                        // Don't add the ID to the enabled list if it already exists in the database
                                        // This doesn't apply to the current metadata ID, which will always be there
                                        if (!unfinishedMessage.getConnectorMessages().containsKey(id) || id == metaDataId) {
                                            enabledMetaDataIds.add(id);
                                        }
                                    }
                                }

                                if (!enabledMetaDataIds.contains(metaDataId)) {
                                    enabledMetaDataIds.add(metaDataId);
                                }

                                chain.setEnabledMetaDataIds(enabledMetaDataIds);
                                chain.setMessage(connectorMessage);
                                List<ConnectorMessage> recoveredConnectorMessages = chain.call();

                                /*
                                 * Check for null here in case DestinationChain.call() returned
                                 * null, which indicates that the chain did not process and should
                                 * be skipped. This would only happen in very rare circumstances,
                                 * possibly if a message is sent to the chain and the destination
                                 * connector that the message belongs to has been removed or
                                 * disabled.
                                 */
                                if (recoveredConnectorMessages != null) {
                                    for (ConnectorMessage recoveredConnectorMessage : recoveredConnectorMessages) {
                                        unfinishedMessage.getConnectorMessages().put(recoveredConnectorMessage.getMetaDataId(), recoveredConnectorMessage);
                                        sourceMessage.getResponseMap().putAll(recoveredConnectorMessage.getResponseMap());
                                    }
                                }

                                break;
                            } finally {
                                chain.getEnabledMetaDataIds().clear();
                                chain.getEnabledMetaDataIds().addAll(chainMetaDataIds);
                            }
                        }
                    }
                } else {
                    sourceMessage.getResponseMap().putAll(connectorMessage.getResponseMap());
                }
            }
        }

        ResponseSelector responseSelector = channel.getResponseSelector();
        channel.finishMessage(unfinishedMessage, !responseSelector.canRespond());

        if (responseSelector.canRespond()) {
            Response response = null;

            /*
             * only put a response in the dispatchResult if a response was not already stored in the
             * source message (which happens when the source queue is enabled)
             */
            if (sourceMessage.getResponse() == null) {
                response = responseSelector.getResponse(sourceMessage, unfinishedMessage);
            }

            DispatchResult dispatchResult = new DispatchResult(unfinishedMessage.getMessageId(), unfinishedMessage, response, true, false);
            channel.getSourceConnector().handleRecoveredResponse(dispatchResult);
        }
    }

    private void recoverPendingMessage(Message pendingMessage) throws InterruptedException {
        for (ConnectorMessage pendingConnectorMessage : pendingMessage.getConnectorMessages().values()) {
            Integer metaDataId = pendingConnectorMessage.getMetaDataId();
            for (DestinationChain chain : channel.getDestinationChains()) {
                List<Integer> chainMetaDataIds = chain.getMetaDataIds();
                if (chainMetaDataIds.contains(metaDataId)) {
                    try {
                        List<Integer> enabledMetaDataIds = new ArrayList<Integer>();
                        enabledMetaDataIds.add(metaDataId);

                        chain.setEnabledMetaDataIds(enabledMetaDataIds);
                        chain.setMessage(pendingConnectorMessage);
                        chain.call();

                        break;
                    } finally {
                        chain.getEnabledMetaDataIds().clear();
                        chain.getEnabledMetaDataIds().addAll(chainMetaDataIds);
                    }
                }
            }
        }
    }
}
