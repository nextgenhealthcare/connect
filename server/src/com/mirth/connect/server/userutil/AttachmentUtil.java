/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.List;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.ImmutableConnectorMessage;
import com.mirth.connect.donkey.model.message.XmlSerializerException;
import com.mirth.connect.donkey.model.message.attachment.Attachment;

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
        logger.error("The getMessageAttachments(messageObject) method is deprecated and will soon be removed. Please use getAttachments() instead.");
        return com.mirth.connect.server.util.AttachmentUtil.getMessageAttachments(messageObject.getImmutableConnectorMessage());
    }
    
    @Deprecated
    // TODO: Remove in 3.1
    public static List<Attachment> getMessageAttachments(ImmutableConnectorMessage message) throws XmlSerializerException {
        logger.error("The getMessageAttachments(connectorMessage) method is deprecated and will soon be removed. Please use getAttachments() instead.");
        return com.mirth.connect.server.util.AttachmentUtil.getMessageAttachments(message);
    }
}
