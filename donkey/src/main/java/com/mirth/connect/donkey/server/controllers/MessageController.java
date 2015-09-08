/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.controllers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.activation.UnsupportedDataTypeException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.Status;
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

    private Donkey donkey = Donkey.getInstance();

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
        DonkeyDao dao = donkey.getDaoFactory().getDao();

        try {
            dao.insertMessageAttachment(channelId, messageId, attachment);

            dao.commit();
        } finally {
            dao.close();
        }
    }

    public boolean isMessageCompleted(Message message) {
        if (MapUtils.isEmpty(message.getConnectorMessages())) {
            return false;
        }

        for (Entry<Integer, ConnectorMessage> connectorMessageEntry : message.getConnectorMessages().entrySet()) {
            ConnectorMessage connectorMessage = connectorMessageEntry.getValue();

            if (!connectorMessage.getStatus().isCompleted()) {
                return false;
            }
        }

        return true;
    }

    public boolean isMessageCompleted(Set<Status> statuses) {
        if (CollectionUtils.isEmpty(statuses)) {
            return false;
        }

        for (Status status : statuses) {
            if (!status.isCompleted()) {
                return false;
            }
        }

        return true;
    }

    public void deleteMessages(String channelId, Map<Long, Set<Integer>> messages) {
        DonkeyDao dao = donkey.getDaoFactory().getDao();

        try {
            for (Entry<Long, Set<Integer>> messageEntry : messages.entrySet()) {
                Long messageId = messageEntry.getKey();
                Set<Integer> metaDataIds = messageEntry.getValue();

                if (metaDataIds == null) {
                    dao.deleteMessage(channelId, messageId);
                } else {
                    dao.deleteConnectorMessages(channelId, messageId, metaDataIds);
                }
            }

            dao.commit();
        } finally {
            dao.close();
        }
    }
}
