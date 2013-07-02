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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.activation.UnsupportedDataTypeException;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.Donkey;
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
}
