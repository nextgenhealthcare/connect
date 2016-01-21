/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message.attachment;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.channel.Channel;

public interface AttachmentHandlerProvider {

    public void setProperties(Channel channel, AttachmentHandlerProperties attachmentProperties);

    public byte[] reAttachMessage(String raw, ConnectorMessage connectorMessage, String charsetEncoding, boolean binary);

    public String reAttachMessage(ConnectorMessage message);

    public String reAttachMessage(String raw, ConnectorMessage message);

    public boolean canExtractAttachments();

    public byte[] replaceOutboundAttachment(byte[] content) throws Exception;

    public AttachmentHandler getHandler();
}