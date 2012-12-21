/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.Encryptor;
import com.mirth.connect.donkey.server.controllers.MessageController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.util.ThreadUtils;

public class RecoveryTask implements Callable<List<Message>> {
    private Channel channel;
    private Encryptor encryptor;

    public RecoveryTask(Channel channel, Encryptor encryptor) {
        this.channel = channel;
        this.encryptor = encryptor;
    }

    @Override
    public List<Message> call() throws Exception {
        ThreadUtils.checkInterruptedStatus();
        DonkeyDao dao = channel.getDaoFactory().getDao();
        MessageController messageController = MessageController.getInstance();
        StorageSettings storageSettings = channel.getStorageSettings();

        try {
            // step 1: recover messages for each destination (RECEIVED or PENDING on destination)
            for (DestinationChain chain : channel.getDestinationChains()) {
                for (Entry<Integer, DestinationConnector> destinationConnectorEntry : chain.getDestinationConnectors().entrySet()) {
                    Integer metaDataId = destinationConnectorEntry.getKey();

                    if (storageSettings.isMessageRecoveryEnabled()) {
                        ThreadUtils.checkInterruptedStatus();
                        List<ConnectorMessage> recoveredConnectorMessages = dao.getConnectorMessages(channel.getChannelId(), metaDataId, Status.RECEIVED);
                        ThreadUtils.checkInterruptedStatus();
                        recoveredConnectorMessages.addAll(dao.getConnectorMessages(channel.getChannelId(), metaDataId, Status.PENDING));

                        for (ConnectorMessage recoveredConnectorMessage : recoveredConnectorMessages) {
                            long messageId = recoveredConnectorMessage.getMessageId();

                            messageController.decryptConnectorMessage(recoveredConnectorMessage, encryptor);

                            // get the list of destination meta data ids to send to
                            List<Integer> metaDataIds = null;

                            if (recoveredConnectorMessage.getChannelMap().containsKey(Constants.DESTINATION_META_DATA_IDS_KEY)) {
                                metaDataIds = (List<Integer>) recoveredConnectorMessage.getChannelMap().get(Constants.DESTINATION_META_DATA_IDS_KEY);
                            }

                            if (metaDataIds != null && metaDataIds.size() > 0) {
                                // The order of the enabledMetaDataId list needs to be based on the chain order.
                                // We do not use ListUtils here because there is no official guarantee of order.
                                List<Integer> enabledMetaDataIds = new ArrayList<Integer>();
                                for (Integer id : chain.getMetaDataIds()) {
                                    if (metaDataIds.contains(id)) {
                                        enabledMetaDataIds.add(id);
                                    }
                                }
                                chain.setEnabledMetaDataIds(enabledMetaDataIds);
                            }

                            chain.setMessage(recoveredConnectorMessage);

                            try {
                                chain.call();
                            } catch (InterruptedException e) {
                                throw e;
                            } catch (Exception e) {
                                System.err.println("Failed to recover message " + messageId + "-" + metaDataId);
                            }
                        }
                    }
                }
            }

            // step 2: recover messages for each source (RECEIVED)
            if (channel.getSourceConnector().isRespondAfterProcessing() && storageSettings.isMessageRecoveryEnabled()) {
                channel.processSourceQueue(0);
            }

            ThreadUtils.checkInterruptedStatus();

            // step 3: recover any messages that are not marked as finished
            List<Message> unfinishedMessages = dao.getUnfinishedMessages(channel.getChannelId(), channel.getServerId());
            dao.close();

            for (Message message : unfinishedMessages) {
                messageController.decryptMessage(message, encryptor);
                ConnectorMessage sourceMessage = message.getConnectorMessages().get(0);
                boolean finished = true;

                //TODO will sourceMessage be null for any reason?
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
                        boolean removeContent = false;
                        boolean removeAttachments = false;
                        boolean messageCompleted = MessageController.getInstance().isMessageCompleted(message);

                        if (messageCompleted) {
                            removeContent = (storageSettings.isRemoveContentOnCompletion());
                            removeAttachments = (storageSettings.isRemoveAttachmentsOnCompletion());
                        }

                        Response response = null;

                        /*
                         * only put a response in the dispatchResult if a response was not already
                         * stored in the source message (which happens when the source queue is
                         * enabled)
                         */
                        if (sourceMessage.getResponse() == null) {
                            response = responseSelector.getResponse(sourceMessage, message);
                        }

                        DispatchResult dispatchResult = new DispatchResult(message.getMessageId(), message, response, true, removeContent, removeAttachments, false);
                        channel.getSourceConnector().handleRecoveredResponse(dispatchResult);
                    }
                }
            }

            return unfinishedMessages;
        } finally {
            if (!dao.isClosed()) {
                dao.close();
            }
        }
    }
}
