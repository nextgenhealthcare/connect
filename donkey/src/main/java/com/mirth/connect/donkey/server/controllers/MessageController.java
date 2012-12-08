/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.activation.UnsupportedDataTypeException;

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.Encryptor;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.util.StringUtil;

public class MessageController {
    private static MessageController instance;

    public static MessageController getInstance() {
        synchronized (MessageController.class) {
            if (instance == null) {
                instance = new MessageController();
            }

            return instance;
        }
    }

    private MessageController() {}

    public synchronized long getNextMessageId(String channelId) {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            return dao.getNextMessageId(channelId);
        } finally {
            dao.close();
        }
    }

    public Message createNewMessage(String channelId, String serverId) {
        long messageId = getNextMessageId(channelId);

        Calendar dateCreated = Calendar.getInstance();

        Message message = new Message();
        message.setMessageId(messageId);
        message.setChannelId(channelId);
        message.setServerId(serverId);
        message.setDateCreated(dateCreated);

        return message;
    }

    public void importMessage(String channelId, Message message) throws DonkeyException {
        Channel channel = Donkey.getInstance().getDeployedChannels().get(channelId);
        
        if (channel == null) {
            throw new DonkeyException("Failed to import message, channel ID " + channelId + " is not currently deployed");
        } else {
            channel.importMessage(message);
        }
    }

    public Attachment createAttachment(Object data, String type) throws UnsupportedDataTypeException {
        byte[] byteData;

        if (data instanceof byte[]) {
            byteData = (byte[]) data;
        } else if (data instanceof String) {
            byteData = StringUtil.getBytesUncheckedChunked((String) data, Constants.ATTACHMENT_CHARSET);
        } else {
            throw new UnsupportedDataTypeException("Attachment can be of type String or byte[]");
        }

        Attachment attachment = new Attachment();
        attachment.setId(UUID.randomUUID().toString());
        attachment.setContent(byteData);
        attachment.setType(type);
        return attachment;
    }

    public void insertAttachment(Attachment attachment, String channelId, Long messageId) {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            dao.insertMessageAttachment(channelId, messageId, attachment);

            dao.commit();
        } finally {
            dao.close();
        }
    }

    public boolean isMessageCompleted(Message message) {
        return isMessageCompleted(message.getConnectorMessages());
    }

    public boolean isMessageCompleted(Map<Integer, ConnectorMessage> connectorMessages) {
        if (connectorMessages.size() == 0 || (connectorMessages.size() == 1 && connectorMessages.containsKey(0))) {
            return false;
        }

        for (Entry<Integer, ConnectorMessage> connectorMessageEntry : connectorMessages.entrySet()) {
            Integer metaDataId = connectorMessageEntry.getKey();
            ConnectorMessage connectorMessage = connectorMessageEntry.getValue();

            if (metaDataId != 0 && !connectorMessage.getStatus().isCompleted()) {
                return false;
            }
        }

        return true;
    }

    public void deleteMessages(String channelId, Map<Long, Set<Integer>> messages, boolean deleteStatistics) {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            for (Entry<Long, Set<Integer>> messageEntry : messages.entrySet()) {
                Long messageId = messageEntry.getKey();
                Set<Integer> metaDataIds = messageEntry.getValue();

                if (metaDataIds == null) {
                    dao.deleteMessage(channelId, messageId, deleteStatistics);
                } else {
                    dao.deleteConnectorMessages(channelId, messageId, new ArrayList<Integer>(metaDataIds), deleteStatistics);
                }
            }

            dao.commit();
        } finally {
            dao.close();
        }
    }
    
    public void decryptMessage(Message message, Encryptor encryptor) {
        for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
            decryptConnectorMessage(connectorMessage, encryptor);
        }
    }
    
    public void decryptConnectorMessage(ConnectorMessage connectorMessage, Encryptor encryptor) {
        if (connectorMessage != null) {
            decryptMessageContent(connectorMessage.getRaw(), encryptor);
            decryptMessageContent(connectorMessage.getProcessedRaw(), encryptor);
            decryptMessageContent(connectorMessage.getTransformed(), encryptor);
            decryptMessageContent(connectorMessage.getEncoded(), encryptor);
            decryptMessageContent(connectorMessage.getSent(), encryptor);
            decryptMessageContent(connectorMessage.getResponse(), encryptor);
            decryptMessageContent(connectorMessage.getProcessedResponse(), encryptor);
        }
    }
    
    public void decryptMessageContent(MessageContent content, Encryptor encryptor) {
        if (content != null && content.getContent() == null) {
            String encryptedContent = content.getEncryptedContent();
            
            if (encryptedContent != null) {
                content.setContent(encryptor.decrypt(encryptedContent));
            }
        }
    }
}
