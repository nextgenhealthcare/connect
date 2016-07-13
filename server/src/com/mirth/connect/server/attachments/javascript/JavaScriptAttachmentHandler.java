/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.attachments.javascript;

import java.util.ArrayList;
import java.util.List;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.model.message.attachment.AttachmentException;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;

public class JavaScriptAttachmentHandler implements AttachmentHandler {

    private JavaScriptAttachmentHandlerProvider provider;
    private String newMessage;
    private List<com.mirth.connect.server.userutil.Attachment> attachments;
    private int index;

    public JavaScriptAttachmentHandler(JavaScriptAttachmentHandlerProvider provider) {
        this.provider = provider;
    }

    @Override
    public void initialize(RawMessage message, Channel channel) throws AttachmentException {
        index = 0;
        attachments = new ArrayList<com.mirth.connect.server.userutil.Attachment>();
        try {
            newMessage = JavaScriptUtil.executeAttachmentScript(provider.getContextFactory(), message, channel.getChannelId(), channel.getName(), attachments);
        } catch (Throwable t) {
            if (t instanceof JavaScriptExecutorException) {
                t = t.getCause();
            }

            throw new AttachmentException("Error running javascript attachment handler script", t);
        }
    }

    @Override
    public Attachment nextAttachment() {
        if (index < attachments.size()) {
            com.mirth.connect.server.userutil.Attachment attachment = attachments.get(index++);
            return new Attachment(attachment.getId(), attachment.getContent(), attachment.getType());
        }

        return null;
    }

    @Override
    public String shutdown() {
        String finalMessage = newMessage;

        newMessage = null;
        attachments = null;

        return finalMessage;
    }
}
