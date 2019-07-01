/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.attachments.identity;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.model.message.attachment.AttachmentException;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.util.Base64Util;
import com.mirth.connect.donkey.util.StringUtil;
import com.mirth.connect.server.util.ServerUUIDGenerator;

public class IdentityAttachmentHandler implements AttachmentHandler {

    private IdentityAttachmentHandlerProvider provider;
    private Attachment attachment;
    private String finalMessage;

    public IdentityAttachmentHandler(IdentityAttachmentHandlerProvider provider) {
        this.provider = provider;
    }

    @Override
    public void initialize(RawMessage message, Channel channel) throws AttachmentException {
        try {
            String uuid = ServerUUIDGenerator.getUUID();
            String mimeType = provider.getReplacer().replaceValues(provider.getMimeType(), channel.getChannelId(), channel.getName(), message.getSourceMap());
            if (StringUtils.isBlank(mimeType)) {
                mimeType = "text/plain";
            }

            byte[] attachmentData;
            if (message.isBinary()) {
                attachmentData = Base64Util.encodeBase64(message.getRawBytes());
            } else {
                attachmentData = StringUtil.getBytesUncheckedChunked(message.getRawData(), Constants.ATTACHMENT_CHARSET);
            }
            message.clearMessage();

            if (ArrayUtils.isNotEmpty(attachmentData)) {
                attachment = new Attachment(uuid, attachmentData, mimeType);
                finalMessage = attachment.getAttachmentId();
            } else {
                finalMessage = "";
            }
        } catch (Throwable t) {
            throw new AttachmentException(t);
        }
    }

    @Override
    public Attachment nextAttachment() throws AttachmentException {
        Attachment attachment = this.attachment;
        this.attachment = null;
        return attachment;
    }

    @Override
    public String shutdown() throws AttachmentException {
        return finalMessage;
    }
}