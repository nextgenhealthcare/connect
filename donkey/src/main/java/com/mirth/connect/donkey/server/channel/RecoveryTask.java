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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.collections.CollectionUtils;
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
        ThreadUtils.checkInterruptedStatus();
        DonkeyDao dao = channel.getDaoFactory().getDao();
        StorageSettings storageSettings = channel.getStorageSettings();

        try {
            // step 1: recover messages for each destination (RECEIVED or PENDING on destination)
            for (DestinationChain chain : channel.getDestinationChains()) {
                List<Integer> chainMetaDataIds = chain.getMetaDataIds();

                for (int i = 0; i < chainMetaDataIds.size(); i++) {
                    Integer metaDataId = chainMetaDataIds.get(i);

                    if (storageSettings.isMessageRecoveryEnabled()) {
                        ThreadUtils.checkInterruptedStatus();
                        // Get connector messages for this server id that need to be recovered.
                        List<ConnectorMessage> recoveredConnectorMessages = dao.getUnfinishedConnectorMessages(channel.getChannelId(), channel.getServerId(), metaDataId, Status.RECEIVED);
                        ThreadUtils.checkInterruptedStatus();
                        recoveredConnectorMessages.addAll(dao.getUnfinishedConnectorMessages(channel.getChannelId(), channel.getServerId(), metaDataId, Status.PENDING));

                        for (ConnectorMessage recoveredConnectorMessage : recoveredConnectorMessages) {
                            long messageId = recoveredConnectorMessage.getMessageId();

                            try {
                                // Get existing connector messages for this message regardless of server id, because we don't want to process that connector again regardless.
                                List<ConnectorMessage> existingConnectorMessages = dao.getConnectorMessages(channel.getChannelId(), messageId, new HashSet<Integer>(chainMetaDataIds), false);
                                Set<Integer> existingMetaDataIds = new HashSet<Integer>();
                                for (ConnectorMessage connectorMessage : existingConnectorMessages) {
                                    existingMetaDataIds.add(connectorMessage.getMetaDataId());
                                }

                                // get the list of destination meta data ids to send to
                                Collection<Integer> metaDataIds = null;

                                if (recoveredConnectorMessage.getSourceMap().containsKey(Constants.DESTINATION_SET_KEY)) {
                                    metaDataIds = (Collection<Integer>) recoveredConnectorMessage.getSourceMap().get(Constants.DESTINATION_SET_KEY);
                                }

                                List<Integer> enabledMetaDataIds = new ArrayList<Integer>();

                                // The order of the enabledMetaDataId list needs to be based on the chain order.
                                // We do not use ListUtils here because there is no official guarantee of order.
                                for (Integer id : chainMetaDataIds) {
                                    if (CollectionUtils.isEmpty(metaDataIds) || metaDataIds.contains(id)) {
                                        // Don't add the ID to the enabled list if it already exists in the database
                                        // This doesn't apply to the current metadata ID, which will always be there
                                        if (!existingMetaDataIds.contains(id) || id == metaDataId) {
                                            enabledMetaDataIds.add(id);
                                        }
                                    }
                                }

                                if (!enabledMetaDataIds.contains(metaDataId)) {
                                    enabledMetaDataIds.add(metaDataId);
                                }

                                chain.setEnabledMetaDataIds(enabledMetaDataIds);
                                chain.setMessage(recoveredConnectorMessage);
                                chain.call();
                            } catch (InterruptedException e) {
                                throw e;
                            } catch (Exception e) {
                                logger.error("Channel " + channel.getName() + "(" + channel.getChannelId() + ") failed to recover message ID " + messageId + ", connector ID " + metaDataId + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }

            ThreadUtils.checkInterruptedStatus();

            // step 2: recover any messages that are not marked as processed and all connector messages are either FILTERED, TRANSFORMED, SENT, QUEUED, or ERROR.
            List<Message> unfinishedMessages = dao.getUnfinishedMessages(channel.getChannelId(), channel.getServerId());
            dao.close();

            for (Message message : unfinishedMessages) {
                try {
                    ConnectorMessage sourceMessage = message.getConnectorMessages().get(0);
                    boolean finished = true;

                    // merge responses from all of the destinations into the source connector's response map
                    for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
                        Status status = connectorMessage.getStatus();

                        if (status == Status.RECEIVED || status == Status.PENDING) {
                            finished = false;
                            break;
                        }

                        if (connectorMessage.getMetaDataId() != 0) {
                            sourceMessage.getResponseMap().putAll(connectorMessage.getResponseMap());
                        }
                    }

                    if (finished) {
                        ThreadUtils.checkInterruptedStatus();
                        ResponseSelector responseSelector = channel.getResponseSelector();
                        channel.finishMessage(message, !responseSelector.canRespond());

                        if (responseSelector.canRespond()) {
                            Response response = null;

                            /*
                             * only put a response in the dispatchResult if a response was not
                             * already stored in the source message (which happens when the source
                             * queue is enabled)
                             */
                            if (sourceMessage.getResponse() == null) {
                                response = responseSelector.getResponse(sourceMessage, message);
                            }

                            DispatchResult dispatchResult = new DispatchResult(message.getMessageId(), message, response, true, false);
                            channel.getSourceConnector().handleRecoveredResponse(dispatchResult);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Channel " + channel.getName() + "(" + channel.getChannelId() + ") failed to recover message ID " + message.getMessageId() + ": " + e.getMessage());
                }
            }

            // step 3: If source queuing is disabled, recover messages for each source (RECEIVED) and flush out the source queue.
            if (channel.getSourceConnector().isRespondAfterProcessing() && storageSettings.isMessageRecoveryEnabled()) {
                channel.processSourceQueue(0);
            }

            return null;
        } finally {
            if (!dao.isClosed()) {
                dao.close();
            }
        }
    }
}
