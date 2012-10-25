/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message.attachment;

import java.io.Serializable;

import com.mirth.connect.donkey.server.channel.Channel;

public abstract class AttachmentHandler implements Serializable {
    public abstract void initialize(String message, Channel channel) throws Exception;

    public abstract void initialize(byte[] bytes, Channel channel) throws Exception;

    public abstract Attachment nextAttachment() throws Exception;

    public abstract String shutdown() throws Exception;

    public abstract void setProperties(AttachmentHandlerProperties attachmentProperties);
}
