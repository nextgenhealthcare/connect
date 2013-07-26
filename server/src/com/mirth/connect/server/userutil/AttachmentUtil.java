/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.ArrayList;
import java.util.List;

import javax.activation.UnsupportedDataTypeException;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.ImmutableConnectorMessage;
import com.mirth.connect.donkey.model.message.XmlSerializerException;
import com.mirth.connect.donkey.server.controllers.MessageController;

public class AttachmentUtil {
    private static Logger logger = Logger.getLogger(AttachmentUtil.class);

    public static byte[] reAttachMessage(String raw, ImmutableConnectorMessage connectorMessage, String charsetEncoding, boolean binary) {
        return com.mirth.connect.server.util.AttachmentUtil.reAttachMessage(raw, connectorMessage, charsetEncoding, binary);
    }

    @Deprecated
    // TODO: Remove in 3.1
    public static String reAttachMessage(MessageObject messageObject) {
        logger.error("The reAttachMessage(messageObject) method is deprecated and will soon be removed. Please use reAttachMessage(connectorMessage) instead.");
        return com.mirth.connect.server.util.AttachmentUtil.reAttachMessage(messageObject.getImmutableConnectorMessage());
    }

    public static String reAttachMessage(ImmutableConnectorMessage message) {
        return com.mirth.connect.server.util.AttachmentUtil.reAttachMessage(message);
    }

    @Deprecated
    // TODO: Remove in 3.1
    public static String reAttachRawMessage(MessageObject messageObject) {
        logger.error("The reAttachRawMessage(messageObject) method is deprecated and will soon be removed. Please use reAttachMessage(raw, connectorMessage) instead.");
        return com.mirth.connect.server.util.AttachmentUtil.reAttachMessage(messageObject.getRawData(), messageObject.getImmutableConnectorMessage());
    }

    @Deprecated
    // TODO: Remove in 3.1
    public static String reAttachRawMessage(ImmutableConnectorMessage message) {
        logger.error("The reAttachRawMessage(connectorMessage) method is deprecated and will soon be removed. Please use reAttachMessage(raw, connectorMessage) instead.");
        return com.mirth.connect.server.util.AttachmentUtil.reAttachMessage(message.getRawData(), message);
    }

    public static String reAttachMessage(String raw, ImmutableConnectorMessage message) {
        return com.mirth.connect.server.util.AttachmentUtil.reAttachMessage(raw, message);
    }

    @Deprecated
    // TODO: Remove in 3.1
    public static List<Attachment> getMessageAttachments(MessageObject messageObject) throws XmlSerializerException {
        logger.error("The getMessageAttachments(messageObject) method is deprecated and will soon be removed. Please use getMessageAttachments(connectorMessage) instead.");
        return convertList(com.mirth.connect.server.util.AttachmentUtil.getMessageAttachments(messageObject.getImmutableConnectorMessage()));
    }

    public static List<Attachment> getMessageAttachments(ImmutableConnectorMessage connectorMessage) throws XmlSerializerException {
        return convertList(com.mirth.connect.server.util.AttachmentUtil.getMessageAttachments(connectorMessage));
    }

    public static Attachment addAttachment(List<Attachment> attachments, Object data, String type) throws UnsupportedDataTypeException {
        Attachment userAttachment = convertAttachment(MessageController.getInstance().createAttachment(data, type));
        attachments.add(userAttachment);
        return userAttachment;
    }

    public static Attachment createAttachment(ImmutableConnectorMessage connectorMessage, Object data, String type) throws UnsupportedDataTypeException {
        com.mirth.connect.donkey.model.message.attachment.Attachment attachment = MessageController.getInstance().createAttachment(data, type);
        MessageController.getInstance().insertAttachment(attachment, connectorMessage.getChannelId(), connectorMessage.getMessageId());
        return convertAttachment(attachment);
    }

    private static List<Attachment> convertList(List<com.mirth.connect.donkey.model.message.attachment.Attachment> attachments) {
        List<Attachment> list = new ArrayList<Attachment>();
        for (com.mirth.connect.donkey.model.message.attachment.Attachment attachment : attachments) {
            list.add(convertAttachment(attachment));
        }
        return list;
    }

    private static Attachment convertAttachment(com.mirth.connect.donkey.model.message.attachment.Attachment attachment) {
        return new Attachment(attachment.getId(), attachment.getContent(), attachment.getType());
    }
}
