/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.attachments.identity;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.server.attachments.MirthAttachmentHandlerProvider;
import com.mirth.connect.server.controllers.MessageController;
import com.mirth.connect.server.util.TemplateValueReplacer;

public class IdentityAttachmentHandlerProvider extends MirthAttachmentHandlerProvider {

    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private String mimeType;

    public IdentityAttachmentHandlerProvider(MessageController messageController) {
        super(messageController);
    }

    TemplateValueReplacer getReplacer() {
        return replacer;
    }

    String getMimeType() {
        return mimeType;
    }

    @Override
    public void setProperties(Channel channel, AttachmentHandlerProperties attachmentProperties) {
        mimeType = attachmentProperties.getProperties().get("identity.mimetype");
        if (StringUtils.isBlank(mimeType)) {
            mimeType = "text/plain";
        }
    }

    @Override
    public boolean canExtractAttachments() {
        return true;
    }

    @Override
    public byte[] replaceOutboundAttachment(byte[] content) throws Exception {
        return content;
    }

    @Override
    public AttachmentHandler getHandler() {
        return new IdentityAttachmentHandler(this);
    }
}