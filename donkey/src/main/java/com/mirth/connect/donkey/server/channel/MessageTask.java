/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.server.controllers.MessageController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.util.Base64Util;
import com.mirth.connect.donkey.util.ThreadUtils;

final class MessageTask implements Callable<DispatchResult> {
    private RawMessage rawMessage;
    private Channel channel;
    private StorageSettings storageSettings;
    private DonkeyDaoFactory daoFactory;
    private ResponseSelector responseSelector;
    private boolean respondAfterProcessing;
    private Logger logger = Logger.getLogger(getClass());
    private DispatchResult dispatchResult;
    private Long persistedMessageId;
    private boolean lockAcquired;

    MessageTask(RawMessage rawMessage, Channel channel) {
        this.rawMessage = rawMessage;
        this.channel = channel;
        this.storageSettings = channel.getStorageSettings();
        this.daoFactory = channel.getDaoFactory();
        this.respondAfterProcessing = channel.getSourceConnector().isRespondAfterProcessing();
        this.responseSelector = channel.getResponseSelector();
        this.lockAcquired = false;
    }

    public Long getPersistedMessageId() {
        return persistedMessageId;
    }

    public boolean isLockAcquired() {
        return lockAcquired;
    }

    @Override
    public DispatchResult call() throws Exception {
        DonkeyDao dao = null;
        try {
            if (respondAfterProcessing) {
                channel.obtainProcessLock();
                lockAcquired = true;
            }

            /*
             * TRANSACTION: Create Raw Message
             * - create a source connector message from the raw message and set
             * the
             * status as RECEIVED
             * - store attachments
             */
            dao = daoFactory.getDao();
            ConnectorMessage sourceMessage = createAndStoreSourceMessage(dao, rawMessage);
            Message processedMessage = null;
            Response response = null;
            boolean removeContent = false;
            boolean removeAttachments = false;
            ThreadUtils.checkInterruptedStatus();

            if (respondAfterProcessing) {
                dao.commit(storageSettings.isRawDurable());
                persistedMessageId = sourceMessage.getMessageId();
                dao.close();

                processedMessage = channel.process(sourceMessage, false);

                boolean messageCompleted = MessageController.getInstance().isMessageCompleted(processedMessage);
                if (messageCompleted) {
                    removeContent = (storageSettings.isRemoveContentOnCompletion());
                    removeAttachments = (storageSettings.isRemoveAttachmentsOnCompletion());
                }
            } else {
                // Block other threads from adding to the source queue until both the current commit and queue addition finishes
                synchronized (channel.getSourceQueue()) {
                    dao.commit(storageSettings.isRawDurable());
                    persistedMessageId = sourceMessage.getMessageId();
                    dao.close();
                    channel.queue(sourceMessage);
                }
            }

            if (responseSelector.canRespond()) {
                response = responseSelector.getResponse(sourceMessage, processedMessage);
            }
            dispatchResult = new DispatchResult(persistedMessageId, processedMessage, response, respondAfterProcessing, removeContent, removeAttachments, lockAcquired);

            return dispatchResult;
        } catch (RuntimeException e) {
            //TODO enable channel restart after it has been updated. Currently does not work
//            Donkey.getInstance().restartChannel(channel.getChannelId(), true);
            throw new ChannelException(true, e);
        } finally {
            if (lockAcquired && (persistedMessageId == null || Thread.currentThread().isInterrupted())) {
                // Release the process lock if an exception was thrown before a message was persisted
                // or if the thread was interrupted because no additional processing will be done.
                channel.releaseProcessLock();
                lockAcquired = false;
            }
            if (dao != null && !dao.isClosed()) {
                dao.close();
            }
        }
    }

    private ConnectorMessage createAndStoreSourceMessage(DonkeyDao dao, RawMessage rawMessage) throws InterruptedException {
        ThreadUtils.checkInterruptedStatus();
        Long messageId;
        Calendar receivedDate;
        String channelId = channel.getChannelId();

        if (rawMessage.getMessageIdToOverwrite() == null) {
            Message message = MessageController.getInstance().createNewMessage(channelId, channel.getServerId());
            messageId = message.getMessageId();
            receivedDate = message.getReceivedDate();
            dao.insertMessage(message);
        } else {
            messageId = rawMessage.getMessageIdToOverwrite();
            List<Integer> metaDataIds = new ArrayList<Integer>();
            metaDataIds.addAll(rawMessage.getDestinationMetaDataIds());
            metaDataIds.add(0);
            dao.deleteConnectorMessages(channelId, messageId, metaDataIds, true);
            dao.resetMessage(channelId, messageId);
            receivedDate = Calendar.getInstance();
        }

        ConnectorMessage sourceMessage = new ConnectorMessage(channelId, messageId, 0, channel.getServerId(), receivedDate, Status.RECEIVED);
        sourceMessage.setConnectorName(channel.getSourceConnector().getSourceName());
        sourceMessage.setChainId(0);
        sourceMessage.setOrderId(0);

        sourceMessage.setRaw(new MessageContent(channelId, messageId, 0, ContentType.RAW, null, channel.getSourceConnector().getInboundDataType().getType(), false));

        if (rawMessage.getChannelMap() != null) {
            sourceMessage.setChannelMap(rawMessage.getChannelMap());
        }

        AttachmentHandler attachmentHandler = channel.getAttachmentHandler();

        if (attachmentHandler != null) {
            ThreadUtils.checkInterruptedStatus();

            try {
                if (rawMessage.isBinary()) {
                    attachmentHandler.initialize(rawMessage.getRawBytes(), channel);
                } else {
                    attachmentHandler.initialize(rawMessage.getRawData(), channel);
                }

                // Free up the memory of the raw message since it is no longer being used
                rawMessage.clearMessage();

                Attachment attachment;
                while ((attachment = attachmentHandler.nextAttachment()) != null) {
                    ThreadUtils.checkInterruptedStatus();

                    if (storageSettings.isStoreAttachments()) {
                        dao.insertMessageAttachment(channelId, messageId, attachment);
                    }
                }

                String replacedMessage = attachmentHandler.shutdown();

                sourceMessage.getRaw().setContent(replacedMessage);
            } catch (Exception e) {
                logger.error("Error processing attachments for channel " + channelId + ". " + e.getMessage());
            }
        } else {
            if (rawMessage.isBinary()) {
                ThreadUtils.checkInterruptedStatus();

                try {
                    byte[] rawBytes = Base64Util.encodeBase64(rawMessage.getRawBytes());
                    rawMessage.clearMessage();
                    sourceMessage.getRaw().setContent(StringUtils.newStringUsAscii(rawBytes));
                } catch (IOException e) {
                    logger.error("Error processing binary data for channel " + channelId + ". " + e.getMessage());
                }

            } else {
                sourceMessage.getRaw().setContent(rawMessage.getRawData());
                rawMessage.clearMessage();
            }
        }

        ThreadUtils.checkInterruptedStatus();
        dao.insertConnectorMessage(sourceMessage, storageSettings.isStoreMaps());

        if (storageSettings.isStoreRaw()) {
            ThreadUtils.checkInterruptedStatus();
            dao.insertMessageContent(sourceMessage.getRaw());
        }

        return sourceMessage;
    }
}
